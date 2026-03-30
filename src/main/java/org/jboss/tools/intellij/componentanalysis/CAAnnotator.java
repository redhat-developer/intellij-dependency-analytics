/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionProfile;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.serviceContainer.AlreadyDisposedException;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import io.github.guacsec.trustifyda.api.v5.LicenseIdentifier;
import io.github.guacsec.trustifyda.license.LicenseCheck.IncompatibleDependency;
import io.github.guacsec.trustifyda.license.LicenseCheck.LicenseSummary;
import io.github.guacsec.trustifyda.license.LicenseCheck.ProjectLicenseSummary;
import org.jboss.tools.intellij.settings.ApiSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;



public abstract class CAAnnotator extends ExternalAnnotator<CAAnnotator.Info, CAAnnotator.AnnotationData> {

    private static final Logger LOG = Logger.getInstance(CAAnnotator.class);

    @Override
    public @Nullable Info collectInformation(@NotNull PsiFile file) {
        final InspectionProfileEntry inspection = this.getInspection(file, this.getInspectionShortName());
        if (inspection == null) {
            return null;
        }

        if (ManifestExclusionManager.isManifestExcluded(file.getVirtualFile(), file.getProject())) {
            LOG.debug("Skipping analysis for excluded manifest: " + file.getName());
            return null;
        }

        LOG.info("Get dependencies");
        return new Info(file, this.getDependencies(file));
    }

    @Override
    public @Nullable AnnotationData doAnnotate(Info info) {
        if (info != null && info.getFile() != null
                && info.getDependencies() != null && !info.getDependencies().isEmpty()) {
            String path = info.getFile().getVirtualFile().getPath();
            Set<Dependency> dependencies = info.getDependencies().keySet();
            if (CAService.dependenciesModified(path, dependencies)) {
                LOG.info("Generate vulnerability report");
                Project project = info.getFile().getProject();
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                        boolean updated = CAService.performAnalysis(
                                getPackageManager(info.getFile().getName()),
                                info.getFile().getVirtualFile().getName(),
                                info.getFile().getVirtualFile().getPath(),
                                dependencies,
                                info.getFile());

                        ApplicationManager.getApplication().invokeLater(() -> {
                            if (updated) {
                                LOG.info("Refresh dependencies");
                                try {
                                    DaemonCodeAnalyzer.getInstance(project).restart(info.getFile());
                                } catch (AlreadyDisposedException ex) {
                                    LOG.warn("DaemonCodeAnalyzer disposed, invalidate cache: " + path, ex);
                                    CAService.deleteReports(path);
                                }
                            }
                        });
                    });

            }

            LOG.info("Get vulnerability report from cache");
            Map<Dependency, Map<VulnerabilitySource, DependencyReport>> reports = CAService.getReports(path);
            Map<Dependency, Result> dependencyResultMap = this.matchDependencies(info.getDependencies(), reports);
            return new AnnotationData(dependencyResultMap, info.getDependencies());
        }

        return null;
    }

    @Override
    public void apply(@NotNull PsiFile file, AnnotationData annotationData, @NotNull AnnotationHolder holder) {
        if (annotationData == null) {
            return;
        }

        // Pre-build incompatible license info keyed by Dependency (without version)
        Map<Dependency, String> licenseMessages = new HashMap<>();
        Map<Dependency, String> licenseTooltips = new HashMap<>();
        LicenseSummary licenseSummary = null;
        if (ApiSettingsState.getInstance().licenseCheckEnabled) {
            String path = file.getVirtualFile().getPath();
            licenseSummary = CAService.getLicenseSummary(path);
            if (licenseSummary != null) {
                buildIncompatibleLicenseInfo(licenseSummary, licenseMessages, licenseTooltips);
            }
        }

        LOG.info("Annotate dependencies");
        Set<Dependency> annotatedLicenseDeps = new HashSet<>();
        Map<Dependency, Result> annotationResult = annotationData.getResults();
        if (annotationResult != null) {
            annotationResult.forEach((key, value) -> {
            if (value != null) {
                Map<VulnerabilitySource, DependencyReport> reports = value.getReports();
                List<PsiElement> elements = value.getElements();
                if (reports != null && !reports.isEmpty()
                        && elements != null && !elements.isEmpty()) {
                    Optional<DependencyReport> reportOptional = reports.values().stream()
                            .filter(Objects::nonNull).findAny();

                    if (reportOptional.isPresent() && reportOptional.get().getRef() != null) {
                        String name = getDependencyString(reportOptional.get().getRef().purl());

                        StringBuilder messageBuilder = new StringBuilder(name);
                        StringBuilder tooltipBuilder = new StringBuilder("<html>").append("<p>").append(escapeHtml(name)).append("</p>");
                        Map<VulnerabilitySource, DependencyReport> quickfixes = new HashMap<>();

                        reports.forEach((source, report) -> {
                            if (report.getIssues() != null && !report.getIssues().isEmpty()) {
                                messageBuilder.append(System.lineSeparator());
                                tooltipBuilder.append("<p/>");

                                if (source.getProvider().equals(source.getSource())) {
                                    messageBuilder.append(source.getProvider())
                                            .append(" vulnerability info: ");
                                    tooltipBuilder.append("<p>")
                                            .append(escapeHtml(source.getProvider()))
                                            .append(" vulnerability info:</p>");
                                } else {
                                    messageBuilder.append(source.getSource())
                                            .append(" (")
                                            .append(source.getProvider())
                                            .append(") vulnerability info: ");
                                    tooltipBuilder.append("<p>")
                                            .append(escapeHtml(source.getSource()))
                                            .append(" (")
                                            .append(escapeHtml(source.getProvider()))
                                            .append(") vulnerability info:</p>");
                                }

                                int num = report.getIssues().size();
                                messageBuilder.append("Known security vulnerabilities: ")
                                        .append(num);
                                tooltipBuilder.append("<p>Known security vulnerabilities: ")
                                        .append(num)
                                        .append("</p>");

                                if (report.getHighestVulnerability() != null && report.getHighestVulnerability().getSeverity() != null) {
                                    String severity = report.getHighestVulnerability().getSeverity().getValue();
                                    messageBuilder.append(", Highest severity: ")
                                            .append(severity);
                                    tooltipBuilder.append("<p>Highest severity: ")
                                            .append(escapeHtml(severity))
                                            .append("</p>");
                                }
                            }

                            if (CAIntentionAction.isQuickFixAvailable(report) || !CAIntentionAction.thereAreNoIssues(report)) {
                                quickfixes.put(source, report);
                            }
                        });

                        // Merge license incompatibility info into the annotation
                        Dependency noVersionKey = new Dependency(key, false);
                        String licenseMsg = licenseMessages.get(noVersionKey);
                        if (licenseMsg != null) {
                            messageBuilder.append(System.lineSeparator()).append(licenseMsg);
                            tooltipBuilder.append("<p/>").append(licenseTooltips.get(noVersionKey));
                            annotatedLicenseDeps.add(noVersionKey);
                        }

                        elements.forEach(e -> {
                            if (e != null) {
                                if (!quickfixes.isEmpty() && this.isQuickFixApplicable(e)) {
                                    DependencyReport firstReport = quickfixes.values().iterator().next();
                                    AnnotationBuilder builder = holder
                                            .newAnnotation(getHighlightSeverity(firstReport, e), messageBuilder.toString())
                                            .tooltip(tooltipBuilder.toString())
                                            .range(e);

                                    quickfixes.forEach((source, report) -> {
                                        if(CAIntentionAction.isQuickFixAvailable(report)) {
                                            builder.withFix(this.createQuickFix(e, source, report));
                                            CAUpdateManifestIntentionAction patchManifest = this.patchManifest(file, report);
                                            if(Objects.nonNull(patchManifest)) {
                                                builder.withFix(patchManifest);
                                            }
                                        }
                                    });
                                    builder.withFix(new SAIntentionAction());
                                    builder.withFix(new ExcludeManifestIntentionAction());
                                    builder.create();
                                }
                            }
                        });
                    }
                }
            }
        });
        }

        // License annotations for deps not already annotated above
        if (licenseSummary != null) {
            applyLicenseMismatchAnnotation(file, licenseSummary, holder);
            applyIncompatibleDependencyAnnotations(annotationData.getAllDependencies(), licenseSummary, annotatedLicenseDeps, holder);
        }
    }

    private void applyLicenseMismatchAnnotation(@NotNull PsiFile file, LicenseSummary licenseSummary, @NotNull AnnotationHolder holder) {
        ProjectLicenseSummary projectLicense = licenseSummary.projectLicense();
        if (projectLicense == null || !projectLicense.mismatch()) {
            return;
        }

        PsiElement licenseElement = getLicenseFieldPsiElement(file);
        if (licenseElement == null) {
            return;
        }

        String manifestLicense = extractLicenseName(projectLicense.manifest());
        String fileLicense = extractLicenseName(projectLicense.file());
        String fileSpdxId = extractSpdxId(projectLicense.file());

        String message = "License mismatch: manifest declares \"" + manifestLicense
                + "\" but LICENSE file contains \"" + fileLicense + "\"";
        String tooltip = "<html><p>License mismatch:</p>"
                + "<p>Manifest declares: <b>" + escapeHtml(manifestLicense) + "</b></p>"
                + "<p>LICENSE file contains: <b>" + escapeHtml(fileLicense) + "</b></p></html>";

        AnnotationBuilder builder = holder
                .newAnnotation(HighlightSeverity.ERROR, message)
                .tooltip(tooltip)
                .range(licenseElement);

        // Only offer quick-fix when a real SPDX ID is available
        if (fileSpdxId != null) {
            LicenseUpdateIntentionAction fix = createLicenseUpdateFix(licenseElement, fileSpdxId);
            if (fix != null) {
                builder.withFix(fix);
            }
        }

        builder.create();
    }

    private void buildIncompatibleLicenseInfo(
            LicenseSummary licenseSummary,
            Map<Dependency, String> messages,
            Map<Dependency, String> tooltips) {
        if (licenseSummary.incompatibleDependencies() == null || licenseSummary.incompatibleDependencies().isEmpty()) {
            return;
        }

        ProjectLicenseSummary projectLicense = licenseSummary.projectLicense();
        String projectLicenseName = projectLicense != null
                ? extractLicenseName(projectLicense.manifest() != null ? projectLicense.manifest() : projectLicense.file())
                : "unknown";

        for (IncompatibleDependency incompatible : licenseSummary.incompatibleDependencies()) {
            try {
                PackageURL purl = new PackageURL(incompatible.purl());
                Dependency dep = new Dependency(purl, false);

                String licenseNames = incompatible.licenses() != null
                        ? incompatible.licenses().stream()
                            .map(LicenseIdentifier::getName)
                            .collect(Collectors.joining(", "))
                        : "unknown";

                String reason = incompatible.reason() != null
                        ? incompatible.reason()
                        : "This dependency may require relicensing if distributed.";

                messages.put(dep, buildLicenseWarningMessage(licenseNames, projectLicenseName, reason));
                tooltips.put(dep, buildLicenseWarningTooltip(licenseNames, projectLicenseName, reason));

            } catch (MalformedPackageURLException ex) {
                LOG.warn("Failed to parse PURL from incompatible dependency: " + incompatible.purl(), ex);
            }
        }
    }

    private void applyIncompatibleDependencyAnnotations(
            Map<Dependency, List<PsiElement>> allDependencies,
            LicenseSummary licenseSummary,
            Set<Dependency> alreadyAnnotated,
            @NotNull AnnotationHolder holder) {
        if (licenseSummary.incompatibleDependencies() == null || licenseSummary.incompatibleDependencies().isEmpty()) {
            return;
        }
        if (allDependencies == null || allDependencies.isEmpty()) {
            return;
        }

        ProjectLicenseSummary projectLicense = licenseSummary.projectLicense();
        String projectLicenseName = projectLicense != null
                ? extractLicenseName(projectLicense.manifest() != null ? projectLicense.manifest() : projectLicense.file())
                : "unknown";

        // Build a lookup map without version for matching
        Map<Dependency, List<PsiElement>> noVersionMap = allDependencies.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> new Dependency(e.getKey(), false),
                        e -> new ArrayList<>(e.getValue()),
                        (l1, l2) -> { l1.addAll(l2); return l1; }));

        for (IncompatibleDependency incompatible : licenseSummary.incompatibleDependencies()) {
            try {
                PackageURL purl = new PackageURL(incompatible.purl());
                Dependency dep = new Dependency(purl, false);

                // Skip deps already annotated with merged license info
                if (alreadyAnnotated.contains(dep)) {
                    continue;
                }

                List<PsiElement> elements = noVersionMap.get(dep);
                if (elements == null || elements.isEmpty()) {
                    continue;
                }

                String licenseNames = incompatible.licenses() != null
                        ? incompatible.licenses().stream()
                            .map(LicenseIdentifier::getName)
                            .collect(Collectors.joining(", "))
                        : "unknown";

                String reason = incompatible.reason() != null
                        ? incompatible.reason()
                        : "This dependency may require relicensing if distributed.";
                String message = buildLicenseWarningMessage(licenseNames, projectLicenseName, reason);
                String tooltip = buildLicenseWarningTooltip(licenseNames, projectLicenseName, reason);

                String wrappedTooltip = "<html>" + tooltip + "</html>";
                for (PsiElement element : elements) {
                    if (element != null) {
                        holder.newAnnotation(HighlightSeverity.WARNING, message)
                                .tooltip(wrappedTooltip)
                                .range(element)
                                .create();
                    }
                }
            } catch (MalformedPackageURLException ex) {
                LOG.warn("Failed to parse PURL from incompatible dependency: " + incompatible.purl(), ex);
            }
        }
    }

    private static String extractLicenseName(@Nullable JsonNode licenseDetails) {
        if (licenseDetails == null) {
            return "unknown";
        }
        // Try "expression" first (SPDX expressions like "MIT OR Apache-2.0")
        JsonNode exprNode = licenseDetails.get("expression");
        if (exprNode != null && !exprNode.isNull() && !exprNode.asText().isBlank()) {
            return exprNode.asText();
        }
        // Try "name" field for human-readable display
        JsonNode nameNode = licenseDetails.get("name");
        if (nameNode != null && !nameNode.isNull() && !nameNode.asText().isBlank()) {
            return nameNode.asText();
        }
        // Fall back to SPDX ID
        String spdxId = extractSpdxId(licenseDetails);
        return spdxId != null ? spdxId : "unknown";
    }

    private static @Nullable String extractSpdxId(@Nullable JsonNode licenseDetails) {
        if (licenseDetails == null) {
            return null;
        }
        // Try identifiers array for the SPDX ID (e.g., "Apache-2.0", "MIT")
        JsonNode identifiers = licenseDetails.get("identifiers");
        if (identifiers != null && identifiers.isArray() && !identifiers.isEmpty()) {
            JsonNode first = identifiers.get(0);
            JsonNode idNode = first.get("id");
            if (idNode != null && !idNode.isNull() && !idNode.asText().isBlank()) {
                return idNode.asText();
            }
        }
        // Fall back to SPDX expression if available
        JsonNode exprNode = licenseDetails.get("expression");
        if (exprNode != null && !exprNode.isNull() && !exprNode.asText().isBlank()) {
            return exprNode.asText();
        }
        return null;
    }

    private static String buildLicenseWarningMessage(String depLicense, String projectLicense, String reason) {
        return "License compatibility warning: "
                + "Dependency license: " + depLicense
                + ", Project license: " + projectLicense
                + ". " + reason;
    }

    private static String buildLicenseWarningTooltip(String depLicense, String projectLicense, String reason) {
        return "<p>License compatibility warning:</p>"
                + "<p>Dependency license: " + escapeHtml(depLicense) + "</p>"
                + "<p>Project license: " + escapeHtml(projectLicense) + "</p>"
                + "<p>" + escapeHtml(reason) + "</p>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    @NotNull
    private HighlightSeverity getHighlightSeverity(DependencyReport report, @NotNull PsiElement context) {
        // Recommendation-only (no vulnerabilities) should always use WEAK_WARNING
        if (CAIntentionAction.thereAreNoIssues(report) && CAIntentionAction.thereIsRecommendation(report)) {
            return HighlightSeverity.WEAK_WARNING;
        }

        // For actual vulnerabilities, use the configured severity from the inspection settings
        final InspectionProfileEntry inspection = this.getInspection(context, this.getInspectionShortName());
        if (inspection != null) {
            final InspectionProfile profile = InspectionProjectProfileManager.getInstance(context.getProject()).getCurrentProfile();
            final HighlightDisplayKey key = HighlightDisplayKey.find(this.getInspectionShortName());
            if (key != null) {
                HighlightDisplayLevel level = profile.getErrorLevel(key, context);
                return level.getSeverity();
            }
        }

        return HighlightSeverity.ERROR;
    }

    abstract protected String getInspectionShortName();

    abstract protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file);

    abstract protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report);
    abstract protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport report);
    abstract protected boolean isQuickFixApplicable(PsiElement element);

    abstract protected @Nullable PsiElement getLicenseFieldPsiElement(PsiFile file);

    abstract protected @Nullable LicenseUpdateIntentionAction createLicenseUpdateFix(PsiElement element, String newLicense);

    private Map<Dependency, Result> matchDependencies(Map<Dependency, List<PsiElement>> dependencies,
                                                      Map<Dependency, Map<VulnerabilitySource, DependencyReport>> reports) {
         if (dependencies != null && !dependencies.isEmpty()
                && reports != null && !reports.isEmpty()) {
            return dependencies.entrySet()
                    .parallelStream()
                    .filter(e -> reports.containsKey(e.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> new Result(dependencies.get(e.getKey()), reports.get(e.getKey())),
                            (o1, o2) -> o1));
        }
        return null;
    }

    private String getDependencyString(PackageURL purl) {
        String namespace = purl.getNamespace();
        String s;
        if (namespace != null) {
            s = namespace + ":" + purl.getName();
        } else {
            s = purl.getName();
        }
        String version = purl.getVersion();
        if (version != null) {
            s += "@" + version;
        }
        return s;
    }

    private InspectionProfileEntry getInspection(@NotNull PsiElement context, @NotNull String inspectionShortName) {
        final HighlightDisplayKey key = HighlightDisplayKey.find(inspectionShortName);
        if (key == null) {
            return null;
        }

        final InspectionProfile profile = InspectionProjectProfileManager.getInstance(context.getProject()).getCurrentProfile();
        if (!profile.isToolEnabled(key, context)) {
            return null;
        }
        return profile.getUnwrappedTool(inspectionShortName, context);
    }

    public static String getPackageManager(String file) {
        return switch (file) {
            case "pom.xml" -> "maven";
            case "package.json" -> "npm";
            case "go.mod" -> "go";
            case "requirements.txt" -> "python";
            case "build.gradle", "build.gradle.kts" -> "gradle";
            case "Cargo.toml" -> "cargo";
            default -> null;
        };
    }

    public static class Info {
        PsiFile file;
        Map<Dependency, List<PsiElement>> dependencies;

        public Info(PsiFile file, Map<Dependency, List<PsiElement>> dependencies) {
            this.file = file;
            this.dependencies = dependencies;
        }

        public PsiFile getFile() {
            return file;
        }

        public Map<Dependency, List<PsiElement>> getDependencies() {
            return dependencies;
        }
    }

    public static class Result {
        List<PsiElement> elements;
        Map<VulnerabilitySource, DependencyReport> reports;

        public Result(List<PsiElement> elements, Map<VulnerabilitySource, DependencyReport> reports) {
            this.elements = elements;
            this.reports = reports;
        }

        public List<PsiElement> getElements() {
            return elements;
        }

        public Map<VulnerabilitySource, DependencyReport> getReports() {
            return reports;
        }
    }

    public static class AnnotationData {
        private final Map<Dependency, Result> results;
        private final Map<Dependency, List<PsiElement>> allDependencies;

        public AnnotationData(Map<Dependency, Result> results, Map<Dependency, List<PsiElement>> allDependencies) {
            this.results = results;
            this.allDependencies = allDependencies;
        }

        public Map<Dependency, Result> getResults() {
            return results;
        }

        public Map<Dependency, List<PsiElement>> getAllDependencies() {
            return allDependencies;
        }
    }
}
