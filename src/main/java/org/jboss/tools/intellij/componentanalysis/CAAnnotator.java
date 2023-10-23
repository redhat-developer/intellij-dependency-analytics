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

import com.github.packageurl.PackageURL;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.daemon.HighlightDisplayKey;
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
import com.redhat.exhort.api.DependencyReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CAAnnotator extends ExternalAnnotator<CAAnnotator.Info, Map<Dependency, CAAnnotator.Result>> {

    private static final Logger LOG = Logger.getInstance(CAAnnotator.class);

    @Override
    public @Nullable Info collectInformation(@NotNull PsiFile file) {
        final InspectionProfileEntry inspection = this.getInspection(file, this.getInspectionShortName());
        if (inspection == null) {
            return null;
        }
        return new Info(file, this.getDependencies(file));
    }

    @Override
    public @Nullable Map<Dependency, Result> doAnnotate(Info info) {
        if (info != null && info.getFile() != null
                && info.getDependencies() != null && !info.getDependencies().isEmpty()) {
            String path = info.getFile().getVirtualFile().getPath();
            Set<Dependency> dependencies = info.getDependencies().keySet();

            if (CAService.dependenciesModified(path, dependencies)) {
                Project project = info.getFile().getProject();
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    boolean updated = CAService.performAnalysis(
                            getPackageManager(info.getFile().getName()),
                            info.getFile().getVirtualFile().getName(),
                            info.getFile().getVirtualFile().getPath(),
                            dependencies);

                    ApplicationManager.getApplication().runReadAction(() -> {
                        if (updated) {
                            try {
                                DaemonCodeAnalyzer.getInstance(project).restart();
                            } catch (AlreadyDisposedException ex) {
                                LOG.warn("DaemonCodeAnalyzer disposed, invalidate cache: " + path, ex);
                                CAService.deleteReports(path);
                            }
                        }
                    });
                });
            }

            Map<Dependency, DependencyReport> reports = CAService.getReports(path);
            return this.matchDependencies(info.getDependencies(), reports);
        }

        return null;
    }

    @Override
    public void apply(@NotNull PsiFile file, Map<Dependency, Result> annotationResult, @NotNull AnnotationHolder holder) {
        annotationResult.forEach((key, value) -> {
            if (value != null) {
                DependencyReport report = value.getReport();
                List<PsiElement> elements = value.getElements();
                if (report.getIssues() != null && !report.getIssues().isEmpty()
                        && elements != null && !elements.isEmpty()) {
                    if (report.getRef() != null) {
                        String d = getDependencyString(report.getRef().purl());
                        int num = report.getIssues().size();
                        String m = d + ", " + "Known security vulnerabilities: " + num + ", ";
                        String t = "<html>" +
                                "<p>" + d + "</p>" +
                                "<p>Known security vulnerabilities: " + num + "</p>";

                        if (report.getHighestVulnerability() != null && report.getHighestVulnerability().getSeverity() != null) {
                            String severity = report.getHighestVulnerability().getSeverity().getValue();
                            m += "Highest severity: " + severity + ", ";
                            t += "<p>Highest severity: " + severity + "</p>";
                        }

                        m += "Dependency Analytics Plugin [Powered by Snyk]";
                        t += "<p>Dependency Analytics Plugin [Powered by <a href='https://snyk.io/'>Snyk</a>]</p>" +
                                "</html>";

                        String message = m;
                        String tooltip = t;

                        elements.forEach(e -> {
                            if (e != null) {
                                AnnotationBuilder builder = holder
                                        .newAnnotation(HighlightSeverity.ERROR, message)
                                        .tooltip(tooltip)
                                        .range(e)
                                        .withFix(new CAIntentionAction());
                                builder.create();
                            }
                        });
                    }
                }
            }
        });
    }

    abstract protected String getInspectionShortName();

    abstract protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file);

    private Map<Dependency, Result> matchDependencies(Map<Dependency, List<PsiElement>> dependencies,
                                                      Map<Dependency, DependencyReport> reports) {
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

    private String getPackageManager(String file) {
        switch (file) {
            case "pom.xml":
                return "maven";
            case "package.json":
                return "npm";
            case "go.mod":
                return "go";
            case "requirements.txt":
                return "python";
            default:
                return null;
        }
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
        DependencyReport report;

        public Result(List<PsiElement> elements, DependencyReport report) {
            this.elements = elements;
            this.report = report;
        }

        public List<PsiElement> getElements() {
            return elements;
        }

        public DependencyReport getReport() {
            return report;
        }
    }
}
