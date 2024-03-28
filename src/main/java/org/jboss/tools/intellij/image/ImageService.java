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
import com.intellij.docker.dockerFile.DockerFileType;
import com.intellij.docker.dockerFile.parser.psi.DockerFileArgCommand;
import com.intellij.docker.dockerFile.parser.psi.DockerFileFromCommand;
import com.intellij.docker.dockerFile.parser.psi.DockerFileRegularOption;
import com.intellij.docker.dockerFile.parser.psi.DockerFileStringLiteral;
import com.intellij.docker.dockerFile.parser.psi.DockerFileVariableRefFull;
import com.intellij.docker.dockerFile.parser.psi.DockerFileVariableRefSimple;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jboss.tools.intellij.report.AnalyticsReportUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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

    static void handleArgCommand(DockerFileArgCommand command, Map<String, String> variables) {
        var declaration = command.getArgDeclaration();
        if (declaration == null) {
            return;
        }

        var name = declaration.getDeclaredName();
        var value = declaration.getAnyValue();
        if (value == null) {
            return;
        }

        String varValue = value.getText();
        varValue = handleStringLiteral(varValue, value.getStringLiteralList(), variables);
        varValue = handleVariableRefFull(varValue, value.getVariableRefFullList(), variables);
        varValue = handleVariableRefSimple(varValue, value.getVariableRefSimpleList(), variables);

        if (varValue != null) {
            variables.put(name.getText(), varValue);
        }
    }

    static String handleStringLiteral(String value, List<DockerFileStringLiteral> literals, Map<String, String> variables) {
        var v = value;
        for (var literal : literals) {
            var text = literal.getText();
            text = handleVariableRefFull(text, literal.getVariableRefFullList(), variables);
            text = handleVariableRefSimple(text, literal.getVariableRefSimpleList(), variables);
            text = text.trim();
            if (text.startsWith("\"")) {
                text = text.substring(1);
            }
            if (text.endsWith("\"")) {
                text = text.substring(0, text.length() - 1);
            }
            v = v.replaceFirst(Pattern.quote(literal.getText()), text);
        }
        return v;
    }

    static String handleVariableRefFull(String value, List<DockerFileVariableRefFull> refs, Map<String, String> variables) {
        var v = value;
        for (var ref : refs) {
            if (ref.getVariableRefPostProcessing() != null) {
                var refPost = ref.getVariableRefPostProcessing();

                var text = ref.getText();
                text = handleStringLiteral(text, refPost.getStringLiteralList(), variables);
                text = handleVariableRefFull(text, refPost.getVariableRefFullList(), variables);
                text = handleVariableRefSimple(text, refPost.getVariableRefSimpleList(), variables);
                v = v.replaceFirst(Pattern.quote(ref.getText()), text);
            } else {
                var varName = ref.getReferencedName();
                if (varName != null && variables.containsKey(varName.getText())) {
                    v = v.replaceFirst(Pattern.quote(ref.getText()), variables.get(varName.getText()));
                }
            }
        }
        return v;
    }

    static String handleVariableRefSimple(String value, List<DockerFileVariableRefSimple> refs, Map<String, String> variables) {
        var v = value;
        for (var ref : refs) {
            var varName = ref.getReferencedName();
            if (varName != null && variables.containsKey(varName.getText())) {
                var refValue = variables.get(varName.getText());
                v = v.replaceFirst(Pattern.quote(ref.getText()), refValue);
            }
        }
        return v;
    }

    static BaseImage handleFromCommand(DockerFileFromCommand command, Map<String, String> variables, Set<String> aliases) {
        var text = command.getText();
        text = text.substring(text.indexOf("FROM") + 4);
        for (var option : command.getRegularOptionList()) {
            text = text.replaceFirst(Pattern.quote(option.getText()), "");
        }
        if (command.getFromStageDeclaration() != null) {
            text = text.substring(0, text.lastIndexOf(command.getFromStageDeclaration().getText()));
        }
        var names = command.getNameChainList();
        var first = names.get(0).getText();
        text = text.substring(text.indexOf(first));
        if (names.size() > 1) {
            var last = names.get(names.size() - 1).getText();
            text = text.substring(0, text.lastIndexOf(last) + last.length());
        }

        var image = text;
        for (var name : names) {
            var t = name.getText();
            t = handleVariableRefFull(t, name.getVariableRefFullList(), variables);
            t = handleVariableRefSimple(t, name.getVariableRefSimpleList(), variables);
            image = image.replaceFirst(Pattern.quote(name.getText()), t);
        }
        image = image.trim();
        if (aliases.contains(image)) {
            return null;
        }

        var options = command.getRegularOptionList();
        var platform = options.stream()
                .filter(option -> "platform".equalsIgnoreCase(option.getOptionName().getText()))
                .map(DockerFileRegularOption::getRegularValue)
                .map(value -> {
                    var v = value.getText();
                    v = handleStringLiteral(v, value.getStringLiteralList(), variables);
                    v = handleVariableRefFull(v, value.getVariableRefFullList(), variables);
                    v = handleVariableRefSimple(v, value.getVariableRefSimpleList(), variables);
                    return v;
                })
                .findAny()
                .orElse(null);

        var stage = command.getFromStageDeclaration();
        if (stage != null) {
            var alias = stage.getDeclaredName().getText();
            aliases.add(alias);
        }

        return new BaseImage(image, platform);
    }

    Map<BaseImage, List<PsiElement>> getBaseImages(PsiFile file) {
        LOGGER.info("Get base images");

        Map<BaseImage, List<PsiElement>> infoMap = new HashMap<>();
        Map<String, String> variables = new HashMap<>();
        Set<String> aliases = new HashSet<>();
        aliases.add("scratch");

        Arrays.stream(file.getChildren())
                .forEachOrdered(element -> {
                    if (element instanceof DockerFileFromCommand) {
                        var baseImage = handleFromCommand((DockerFileFromCommand) element, variables, aliases);
                        if (baseImage != null) {
                            infoMap.computeIfAbsent(baseImage, k -> new LinkedList<>()).add(element);
                        }
                    } else if (element instanceof DockerFileArgCommand) {
                        handleArgCommand((DockerFileArgCommand) element, variables);
                    }
                });

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
        if (DockerFileType.DOCKER_FILE_TYPE.equals(dockerfile.getFileType())) {
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
            var imageService = ImageService.getInstance();
            var images = imageService.getBaseImages(dockerfile);

            if (images != null && !images.isEmpty()) {
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
