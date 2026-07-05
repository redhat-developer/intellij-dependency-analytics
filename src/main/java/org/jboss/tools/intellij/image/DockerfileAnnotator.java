/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.image;

import com.github.packageurl.MalformedPackageURLException;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.serviceContainer.AlreadyDisposedException;
import com.intellij.ui.JBColor;
import io.github.guacsec.trustifyda.api.v5.AnalysisReport;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import io.github.guacsec.trustifyda.api.v5.ProviderReport;
import io.github.guacsec.trustifyda.api.v5.RecommendationSource;
import io.github.guacsec.trustifyda.api.v5.Severity;
import io.github.guacsec.trustifyda.api.v5.Source;
import io.github.guacsec.trustifyda.image.ImageRef;
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import org.jboss.tools.intellij.settings.ApiSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.guacsec.trustifyda.api.PackageRef;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DockerfileAnnotator extends ExternalAnnotator<DockerfileAnnotator.Info, Map<BaseImage, DockerfileAnnotator.Result>> {

    private static final Logger LOG = Logger.getInstance(DockerfileAnnotator.class);

    @Nullable
    static Severity getSeverity(Source source) {
        Severity severity = null;
        if (source.getSummary() != null) {
            if (source.getSummary().getCritical() != null && source.getSummary().getCritical() > 0) {
                severity = Severity.CRITICAL;
            } else if (source.getSummary().getHigh() != null && source.getSummary().getHigh() > 0) {
                severity = Severity.HIGH;
            } else if (source.getSummary().getMedium() != null && source.getSummary().getMedium() > 0) {
                severity = Severity.MEDIUM;
            } else if (source.getSummary().getLow() != null && source.getSummary().getLow() > 0) {
                severity = Severity.LOW;
            } else if (source.getSummary().getUnknown() != null && source.getSummary().getUnknown() > 0) {
                severity = Severity.UNKNOWN;
            }
        }
        return severity;
    }

    static InspectionProfileEntry getInspection(@NotNull PsiElement context) {
        final var key = HighlightDisplayKey.find(DockerfileInspection.SHORT_NAME);
        if (key == null) {
            return null;
        }

        final var profile = InspectionProjectProfileManager.getInstance(context.getProject()).getCurrentProfile();
        if (!profile.isToolEnabled(key, context)) {
            return null;
        }
        return profile.getUnwrappedTool(DockerfileInspection.SHORT_NAME, context);
    }

    static boolean isReportAvailable(AnalysisReport report) {
        return Optional.ofNullable(report.getProviders())
                .map(providers -> providers
                        .values()
                        .stream()
                        .anyMatch(provider -> hasVulnerabilities(provider) || hasProviderRecommendations(provider)))
                .orElse(false);
    }

    private static boolean hasVulnerabilities(ProviderReport provider) {
        return Optional.ofNullable(provider.getSources())
                .map(sources -> sources.values().stream()
                        .filter(Objects::nonNull)
                        .map(Source::getSummary)
                        .filter(Objects::nonNull)
                        .anyMatch(s -> s.getTotal() != null && s.getTotal() > 0))
                .orElse(false);
    }

    private static boolean hasProviderRecommendations(ProviderReport provider) {
        return Optional.ofNullable(provider.getRecommendations())
                .map(recs -> recs.values().stream()
                        .filter(Objects::nonNull)
                        .map(RecommendationSource::getDependencies)
                        .filter(Objects::nonNull)
                        .anyMatch(deps -> !deps.isEmpty()))
                .orElse(false);
    }

    static String generateMessage(String image, AnalysisReport report, String recommendation,
                                   String hardenedRecommendation) {
        var messageBuilder = new StringBuilder(image);

        Optional.ofNullable(report.getProviders())
                .ifPresent(provider ->
                        provider.forEach((providerName, providerReport) -> {
                            if (providerReport.getSources() != null) {
                                providerReport.getSources().forEach((sourceName, source) -> {
                                    if (source.getSummary() != null && source.getSummary().getTotal() != null
                                            && source.getSummary().getTotal() > 0) {
                                        var issueNum = source.getSummary().getTotal();
                                        Severity severity = getSeverity(source);

                                        messageBuilder.append(System.lineSeparator());

                                        if (providerName.equals(sourceName)) {
                                            messageBuilder.append(providerName)
                                                    .append(" vulnerability info: ");
                                        } else {
                                            messageBuilder.append(sourceName)
                                                    .append(" (")
                                                    .append(providerName)
                                                    .append(") vulnerability info: ");
                                        }

                                        messageBuilder.append("Known security vulnerabilities: ")
                                                .append(issueNum);

                                        if (severity != null) {
                                            messageBuilder.append(", Highest severity: ")
                                                    .append(severity.getValue());
                                        }
                                    }
                                });
                            }
                        }));

        if (recommendation != null) {
            messageBuilder.append(System.lineSeparator())
                    .append("Replace your image with RedHat UBI: ")
                    .append(recommendation);
        }
        if (hardenedRecommendation != null) {
            messageBuilder.append(System.lineSeparator())
                    .append("A Red Hat Hardened Image is available: ")
                    .append(hardenedRecommendation);
        }

        return messageBuilder.toString();
    }

    static String generateTooltip(String image, AnalysisReport report, String recommendation,
                                    String hardenedRecommendation) {
        var tooltipBuilder = new StringBuilder("<html>").append("<p>").append(image).append("</p>");

        Optional.ofNullable(report.getProviders())
                .ifPresent(provider ->
                        provider.forEach((providerName, providerReport) -> {
                            if (providerReport.getSources() != null) {
                                providerReport.getSources().forEach((sourceName, source) -> {
                                    if (source.getSummary() != null && source.getSummary().getTotal() != null
                                            && source.getSummary().getTotal() > 0) {
                                        int issueNum = source.getSummary().getTotal();
                                        Severity severity = getSeverity(source);

                                        tooltipBuilder.append("<p/>");

                                        if (providerName.equals(sourceName)) {
                                            tooltipBuilder.append("<p>")
                                                    .append(providerName)
                                                    .append(" vulnerability info:</p>");
                                        } else {
                                            tooltipBuilder.append("<p>")
                                                    .append(sourceName)
                                                    .append(" (")
                                                    .append(providerName)
                                                    .append(") vulnerability info:</p>");
                                        }

                                        tooltipBuilder.append("<p>Known security vulnerabilities: ")
                                                .append(issueNum)
                                                .append("</p>");

                                        if (severity != null) {
                                            tooltipBuilder.append("<p>Highest severity: ")
                                                    .append(severity.getValue())
                                                    .append("</p>");
                                        }
                                    }
                                });
                            }
                        }));

        if (recommendation != null) {
            tooltipBuilder.append("<p/>")
                    .append("<p>Replace your image with RedHat UBI: ")
                    .append(recommendation)
                    .append("</p>");
        }
        if (hardenedRecommendation != null) {
            tooltipBuilder.append("<p/>")
                    .append("<p>A Red Hat Hardened Image is available: ")
                    .append(hardenedRecommendation)
                    .append("</p>");
        }

        return tooltipBuilder.toString();
    }

    static boolean hasIssue(AnalysisReport report) {
        return Optional.ofNullable(report.getProviders())
                .map(provider -> provider.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ProviderReport::getSources)
                        .filter(Objects::nonNull)
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .map(Source::getDependencies)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(DependencyReport::getIssues)
                        .filter(Objects::nonNull)
                        .anyMatch(i -> !i.isEmpty()))
                .orElse(false);
    }

    /** Returns the UBI image recommendation (from source-level dependencies), or null if none. */
    static String getRecommendation(AnalysisReport report, ImageRef imageRef) {
        var deps = Optional.ofNullable(report.getProviders())
                .stream()
                .flatMap(provider -> provider.values().stream())
                .filter(Objects::nonNull)
                .map(ProviderReport::getSources)
                .filter(Objects::nonNull)
                .map(Map::values)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Source::getDependencies)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
        return findMatchingRecommendation(deps, imageRef, DependencyReport::getRef, DependencyReport::getRecommendation);
    }

    /** Returns the hardened image recommendation (from provider-level recommendations), or null if none. */
    static String getHardenedRecommendation(AnalysisReport report, ImageRef imageRef) {
        var deps = Optional.ofNullable(report.getProviders())
                .stream()
                .flatMap(provider -> provider.values().stream())
                .filter(Objects::nonNull)
                .map(ProviderReport::getRecommendations)
                .filter(Objects::nonNull)
                .flatMap(recs -> recs.entrySet().stream())
                .filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getValue().getDependencies())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
        return findMatchingRecommendation(deps, imageRef,
                io.github.guacsec.trustifyda.api.v5.RecommendationReport::getRef,
                io.github.guacsec.trustifyda.api.v5.RecommendationReport::getRecommendation);
    }

    private static <T> String findMatchingRecommendation(Stream<T> items, ImageRef imageRef,
                                                          Function<T, PackageRef> refExtractor,
                                                          Function<T, PackageRef> recommendationExtractor) {
        return items
                .filter(r -> refExtractor.apply(r) != null)
                .filter(r -> {
                    try {
                        return imageRef.getPackageURL().equals(refExtractor.apply(r).purl());
                    } catch (MalformedPackageURLException e) {
                        LOG.warn("Skipping recommendation with malformed PURL", e);
                        return false;
                    }
                })
                .map(recommendationExtractor)
                .filter(Objects::nonNull)
                .findAny()
                .map(DockerfileAnnotator::toImageName)
                .orElse(null);
    }

    private static String toImageName(io.github.guacsec.trustifyda.api.PackageRef ref) {
        try {
            var purl = ref.purl();
            var qualifiers = purl.getQualifiers();
            if (qualifiers != null && qualifiers.containsKey(ImageRef.REPOSITORY_QUALIFIER)) {
                String repoUrl = qualifiers.get(ImageRef.REPOSITORY_QUALIFIER);
                String decoded = fullyDecode(repoUrl);
                if (!decoded.equals(repoUrl)) {
                    var decodedQualifiers = new java.util.TreeMap<>(qualifiers);
                    decodedQualifiers.put(ImageRef.REPOSITORY_QUALIFIER, decoded);
                    purl = new com.github.packageurl.PackageURL(
                            purl.getType(), purl.getNamespace(), purl.getName(),
                            purl.getVersion(), decodedQualifiers, purl.getSubpath());
                }
            }
            return new ImageRef(purl).getImage().getNameWithoutTag();
        } catch (IllegalArgumentException | com.github.packageurl.MalformedPackageURLException e) {
            LOG.warn("Failed to parse recommendation image from PURL: " + ref.ref(), e);
            return null;
        }
    }

    /** Repeatedly URL-decodes until the value stabilizes (handles double/triple encoding). */
    private static String fullyDecode(String value) {
        String previous = value;
        for (int i = 0; i < 5; i++) {
            String decoded = java.net.URLDecoder.decode(previous, java.nio.charset.StandardCharsets.UTF_8);
            if (decoded.equals(previous)) {
                break;
            }
            previous = decoded;
        }
        return previous;
    }

    @NotNull
    private static HighlightSeverity getHighlightSeverity(AnalysisReport report, String recommendation,
                                                           String hardenedRecommendation, boolean hasIssue,
                                                           @NotNull PsiElement context) {
        // Recommendation-only (no vulnerabilities): use INFORMATION severity (blue)
        if (!hasIssue) {
            boolean hasAnyRecommendation = recommendation != null || hardenedRecommendation != null;
            if (hasAnyRecommendation) {
                return HighlightSeverity.INFORMATION;
            }
        }

        // Get the configured severity from the inspection settings
        final InspectionProfileEntry inspection = getInspection(context);
        if (inspection != null) {
            final var profile = InspectionProjectProfileManager.getInstance(context.getProject()).getCurrentProfile();
            final HighlightDisplayKey key = HighlightDisplayKey.find(DockerfileInspection.SHORT_NAME);
            if (key != null) {
                HighlightDisplayLevel level = profile.getErrorLevel(key, context);
                return level.getSeverity();
            }
        }

        // Fallback to original logic if inspection settings can't be determined
        return hasIssue || recommendation == null ?
                HighlightSeverity.ERROR :
                HighlightSeverity.WEAK_WARNING;
    }

    @Override
    public @Nullable Info collectInformation(@NotNull PsiFile file) {
        // Only process Dockerfile files
        if (!DockerfileFileType.isDockerfile(file)) {
            return null;
        }

        var inspection = getInspection(file);
        if (inspection == null) {
            return null;
        }

        var imageService = ImageService.getInstance();
        var infoMap = imageService.getBaseImages(file);

        return new Info(file, infoMap);
    }

    @Override
    public @Nullable Map<BaseImage, Result> doAnnotate(Info info) {
        if (info != null && info.getFile() != null
                && info.getImages() != null && !info.getImages().isEmpty()) {
            LOG.info("Generate vulnerability report");
            var imageCacheService = ImageCacheService.getInstance(info.getFile().getProject());

            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                var images = imageCacheService.getImagesWithoutReport(info.getImages().keySet());

                if (!images.isEmpty()) {
                    var imageService = ImageService.getInstance();

                    var updated = imageService.performAnalysis(new HashSet<>(images), info.getFile().getProject());

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (updated) {
                            LOG.info("Refresh vulnerabilities");
                            try {
                                var project = info.getFile().getProject();
                                DaemonCodeAnalyzer.getInstance(project).restart();
                            } catch (AlreadyDisposedException ex) {
                                LOG.warn("DaemonCodeAnalyzer disposed, invalidate cache.", ex);
                                imageCacheService.deleteReports(info.getImages().keySet());
                                imageCacheService.deleteImages(info.getImages().keySet());
                            }
                        }
                    });
                }
            });

            LOG.info("Get vulnerability report from cache");
            var reports = imageCacheService.getReports(info.getImages().keySet());
            var images = imageCacheService.getImages(info.getImages().keySet());

            if (reports != null && !reports.isEmpty()) {
                return info.getImages()
                        .entrySet()
                        .stream()
                        .filter(e -> reports.containsKey(e.getKey()))
                        .filter(e -> images.containsKey(e.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new Result(info.getImages().get(e.getKey()), reports.get(e.getKey()), images.get(e.getKey())),
                                (o1, o2) -> o1));
            }
        }
        return null;
    }

    @Override
    public void apply(@NotNull PsiFile file, Map<BaseImage, Result> annotationResult, @NotNull AnnotationHolder holder) {
        LOG.info("Annotate base images");
        annotationResult.forEach((key, value) -> {
            if (value != null) {
                var report = value.getReport();
                var elements = value.getElements();
                if (report != null && report.getProviders() != null
                        && elements != null && !elements.isEmpty()) {
                    if (isReportAvailable(report)) {
                        var hasIssue = hasIssue(report);
                        boolean recommendationsEnabled = ApiSettingsState.getInstance().recommendationsEnabled;
                        var recommendation = recommendationsEnabled
                                ? getRecommendation(report, value.getImageRef()) : null;
                        var hardenedRecommendation = recommendationsEnabled
                                ? getHardenedRecommendation(report, value.getImageRef()) : null;

                        var message = generateMessage(key.getImageName(), report,
                                recommendation, hardenedRecommendation);
                        var tooltip = generateTooltip(key.getImageName(), report,
                                recommendation, hardenedRecommendation);

                        elements.forEach(e -> {
                            var severity = getHighlightSeverity(report, recommendation, hardenedRecommendation, hasIssue, e);
                            if (e != null) {
                                var builder = holder
                                        .newAnnotation(severity, message)
                                        .tooltip(tooltip)
                                        .range(e);
                                if (severity == HighlightSeverity.INFORMATION) {
                                    var attrs = new TextAttributes();
                                    attrs.setEffectType(EffectType.WAVE_UNDERSCORE);
                                    attrs.setEffectColor(JBColor.BLUE);
                                    builder = builder.enforcedTextAttributes(attrs);
                                }
                                builder = builder.withFix(new ImageReportIntentionAction());
                                builder = builder.withFix(new UBIIntentionAction());
                                if (hardenedRecommendation != null) {
                                    builder = builder.withFix(new HardenedImageIntentionAction());
                                }
                                builder.create();
                            }
                        });
                    }
                }
            }
        });
    }

    static class Info {
        PsiFile file;
        Map<BaseImage, List<PsiElement>> images;

        public Info(PsiFile file, Map<BaseImage, List<PsiElement>> images) {
            this.file = file;
            this.images = images;
        }

        public PsiFile getFile() {
            return file;
        }

        public Map<BaseImage, List<PsiElement>> getImages() {
            return images;
        }
    }

    static class Result {
        List<PsiElement> elements;
        AnalysisReport report;

        ImageRef imageRef;

        public Result(List<PsiElement> elements, AnalysisReport report, ImageRef imageRef) {
            this.elements = elements;
            this.report = report;
            this.imageRef = imageRef;
        }

        public List<PsiElement> getElements() {
            return elements;
        }

        public AnalysisReport getReport() {
            return report;
        }

        public ImageRef getImageRef() {
            return imageRef;
        }
    }
}
