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
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.serviceContainer.AlreadyDisposedException;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.api.DependencyReport;
import com.redhat.exhort.api.ProviderReport;
import com.redhat.exhort.api.Severity;
import com.redhat.exhort.api.Source;
import com.redhat.exhort.image.ImageRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
                        .map(ProviderReport::getSources)
                        .filter(Objects::nonNull)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .map(Map.Entry::getValue)
                        .map(Source::getSummary)
                        .filter(Objects::nonNull)
                        .anyMatch(s -> s.getTotal() != null && s.getTotal() > 0))
                .orElse(false);
    }

    static String generateMessage(String image, AnalysisReport report, String recommendation) {
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

        return messageBuilder.toString();
    }

    static String generateTooltip(String image, AnalysisReport report, String recommendation) {
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

    static String getRecommendation(AnalysisReport report, ImageRef imageRef) {
        return Optional.ofNullable(report.getProviders())
                .flatMap(provider -> provider.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ProviderReport::getSources)
                        .filter(Objects::nonNull)
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .map(Source::getDependencies)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(r -> r.getRef() != null)
                        .filter(r -> {
                            try {
                                return imageRef.getPackageURL().equals(r.getRef().purl());
                            } catch (MalformedPackageURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(DependencyReport::getRecommendation)
                        .filter(Objects::nonNull)
                        .findAny())
                .map(r -> new ImageRef(r.purl()).getImage().getNameWithoutTag())
                .orElse(null);
    }

    @Override
    public @Nullable Info collectInformation(@NotNull PsiFile file) {
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

                    ApplicationManager.getApplication().runReadAction(() -> {
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
                        var recommendation = getRecommendation(report, value.getImageRef());

                        var message = generateMessage(key.getImageName(), report, recommendation);
                        var tooltip = generateTooltip(key.getImageName(), report, recommendation);

                        var severity = hasIssue || recommendation == null ?
                                HighlightSeverity.ERROR :
                                HighlightSeverity.WEAK_WARNING;

                        elements.forEach(e -> {
                            if (e != null) {
                                var builder = holder
                                        .newAnnotation(severity, message)
                                        .tooltip(tooltip)
                                        .range(e)
                                        .withFix(new ImageReportIntentionAction())
                                        .withFix(new UBIIntentionAction());
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
