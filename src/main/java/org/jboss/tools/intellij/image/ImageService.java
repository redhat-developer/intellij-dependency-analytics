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

import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import org.jboss.tools.intellij.image.build.psi.DockerfileArgDeclaration;
import org.jboss.tools.intellij.image.build.psi.DockerfileArgInstruction;
import org.jboss.tools.intellij.image.build.psi.DockerfileAsClause;
import org.jboss.tools.intellij.image.build.psi.DockerfileFromInstruction;
import org.jboss.tools.intellij.image.build.psi.DockerfileImageName;
import org.jboss.tools.intellij.image.build.psi.DockerfilePlatformOption;
import org.jboss.tools.intellij.image.build.psi.DockerfilePlatformValue;
import org.jboss.tools.intellij.report.AnalyticsReportUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(Service.Level.APP)
public final class ImageService {

    private static final Logger LOGGER = Logger.getInstance(ImageService.class);

    static ImageService getInstance() {
        return ApplicationManager.getApplication().getService(ImageService.class);
    }

    static void handleArgInstruction(DockerfileArgInstruction argInstruction, Map<String, String> variables) {
        DockerfileArgDeclaration declaration = argInstruction.getArgDeclaration();
        if (declaration == null) {
            return;
        }

        String varName = declaration.getText().split("=")[0].trim();
        String varValue = "";
        if (declaration.getText().contains("=")) {
            varValue = declaration.getText().substring(declaration.getText().indexOf("=") + 1).trim();
            varValue = expandVariables(varValue, variables);
        }

        variables.put(varName, varValue);
    }

    static String expandVariables(String value, Map<String, String> variables) {
        String result = value;

        // Handle ${VAR} format
        Pattern fullVarPattern = Pattern.compile("\\$\\{([^}]+)\\}");
        java.util.regex.Matcher fullMatcher = fullVarPattern.matcher(result);
        while (fullMatcher.find()) {
            String varName = fullMatcher.group(1);
            if (variables.containsKey(varName)) {
                result = result.replace(fullMatcher.group(0), variables.get(varName));
            }
        }

        // Handle $VAR format
        Pattern simpleVarPattern = Pattern.compile("\\$([a-zA-Z_][a-zA-Z0-9_]*)");
        java.util.regex.Matcher simpleMatcher = simpleVarPattern.matcher(result);
        while (simpleMatcher.find()) {
            String varName = simpleMatcher.group(1);
            if (variables.containsKey(varName)) {
                result = result.replace(simpleMatcher.group(0), variables.get(varName));
            }
        }

        return result;
    }

    static BaseImage handleFromInstruction(DockerfileFromInstruction fromInstruction, Map<String, String> variables, Set<String> aliases) {
        DockerfileImageName imageName = fromInstruction.getImageName();
        if (imageName == null) {
            return null;
        }

        String imageText = imageName.getText();
        imageText = expandVariables(imageText, variables);

        if (aliases.contains(imageText)) {
            return null;
        }

        String platform = extractPlatform(fromInstruction.getPlatformOption(), variables);

        DockerfileAsClause asClause = fromInstruction.getAsClause();
        if (asClause != null) {
            String asText = asClause.getText().trim();
            if (asText.startsWith("AS ")) {
                String alias = asText.substring(3).trim(); // Remove "AS " prefix
                aliases.add(alias);
            }
        }

        return new BaseImage(imageText, platform);
    }

    static String extractPlatform(DockerfilePlatformOption platformOption, Map<String, String> variables) {
        if (platformOption == null) {
            return null;
        }

        DockerfilePlatformValue platformValue = platformOption.getPlatformValue();
        if (platformValue == null) {
            return null;
        }

        String platform = platformValue.getText();
        return expandVariables(platform, variables);
    }

    Map<BaseImage, List<PsiElement>> getBaseImages(PsiFile file) {
        LOGGER.info("Get base images");

        Map<BaseImage, List<PsiElement>> infoMap = new HashMap<>();
        Map<String, String> variables = new HashMap<>();
        Set<String> aliases = new HashSet<>();
        aliases.add("scratch");

        Collection<DockerfileFromInstruction> fromInstructions = PsiTreeUtil.findChildrenOfType(file, DockerfileFromInstruction.class);
        Collection<DockerfileArgInstruction> argInstructions = PsiTreeUtil.findChildrenOfType(file, DockerfileArgInstruction.class);

        for (DockerfileArgInstruction argInstruction : argInstructions) {
            handleArgInstruction(argInstruction, variables);
        }

        for (DockerfileFromInstruction fromInstruction : fromInstructions) {
            BaseImage baseImage = handleFromInstruction(fromInstruction, variables, aliases);
            if (baseImage != null) {
                infoMap.computeIfAbsent(baseImage, k -> new LinkedList<>()).add(fromInstruction);
            }
        }

        return infoMap;
    }

    boolean performAnalysis(final Set<BaseImage> baseImages, Project project) {
        var imageCache = ImageCacheService.getInstance(project);

        if (imageCache.getReports(baseImages).size() != baseImages.size()) {
            var apiService = ApiService.getInstance();

            var images = apiService.getImageRefs(baseImages);
            var results = apiService.getImageAnalysis(new HashSet<>(images.values()));

            if (results != null) {
                var reports = images
                        .entrySet()
                        .stream()
                        .filter(e -> results.containsKey(e.getValue()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> results.get(e.getValue())
                        ));

                imageCache.deleteReports(baseImages);
                imageCache.deleteImages(baseImages);

                if (!reports.isEmpty()) {
                    imageCache.cacheReports(reports);
                    imageCache.cacheImages(images);
                }
                return true;
            }
        }
        return false;
    }

    Path generateAnalysisHtmlReport(final Set<BaseImage> baseImages) {
        var service = ApiService.getInstance();
        var imageRefs = service.getImageRefs(baseImages);
        return service.getImageAnalysisReport(new HashSet<>(imageRefs.values()));
    }

    JsonObject performImageAnalysis(List<BaseImage> images, VirtualFile dockerfile) {
        if (DockerfileFileType.INSTANCE.equals(dockerfile.getFileType())) {
            var reportLink = generateAnalysisHtmlReport(new HashSet<>(images)).toUri().toString();

            var manifestDetails = new JsonObject();
            manifestDetails.addProperty("showParent", false);
            manifestDetails.addProperty("manifestName", dockerfile.getName());
            manifestDetails.addProperty("manifestPath", dockerfile.getPath());
            manifestDetails.addProperty("manifestFileParent", dockerfile.getParent().getName());
            manifestDetails.addProperty("report_link", reportLink);
            manifestDetails.addProperty("manifestNameWithoutExtension", dockerfile.getNameWithoutExtension());

            return manifestDetails;
        }
        return null;
    }

    void openAnalysisHtmlReport(Project project, PsiFile dockerfile) {
        if (dockerfile != null && project != null) {
            var imageService = getInstance();
            var images = imageService.getBaseImages(dockerfile);

            if (!images.isEmpty()) {
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        var manifestDetails = imageService.performImageAnalysis(
                                new ArrayList<>(images.keySet()),
                                dockerfile.getVirtualFile());

                        if (manifestDetails != null) {
                            ApplicationManager.getApplication().invokeLater(() -> {
                                try {
                                    var analyticsReportUtils = new AnalyticsReportUtils();
                                    analyticsReportUtils.openCustomEditor(FileEditorManager.getInstance(project), manifestDetails);
                                } catch (Exception e) {
                                    LOGGER.error(e);
                                    Messages.showErrorDialog(project,
                                            "Can't open report: " + e.getLocalizedMessage(),
                                            "Error");
                                }
                            });
                        } else {
                            ApplicationManager.getApplication().invokeLater(() ->
                                    Messages.showErrorDialog(project,
                                            "Report generation failed",
                                            "Error"));
                        }
                    } catch (RuntimeException ex) {
                        LOGGER.error(ex);
                        ApplicationManager.getApplication().invokeLater(() ->
                                Messages.showErrorDialog(project,
                                        "Report generation failed: " + ex.getLocalizedMessage(),
                                        "Error"));
                    }
                });
            }
        }
    }
}