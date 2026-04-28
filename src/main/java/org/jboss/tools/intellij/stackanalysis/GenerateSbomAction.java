/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.stackanalysis;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import org.jboss.tools.intellij.exhort.ApiService;

public class GenerateSbomAction extends AnAction {
    private static final Logger logger = Logger.getInstance(GenerateSbomAction.class);

    private static final List<String> supportedManifestFiles = Arrays.asList(
            "pom.xml",
            "package.json",
            "go.mod",
            "requirements.txt",
            "build.gradle",
            "build.gradle.kts",
            "Cargo.toml",
            "pyproject.toml"
    );

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        VirtualFile manifestFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (manifestFile == null) {
            return;
        }

        FileSaverDescriptor descriptor = new FileSaverDescriptor(
                "Save SBOM",
                "Choose location to save the CycloneDX SBOM",
                "json"
        );
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);

        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(
                manifestFile.getParent().getPath()
        );
        VirtualFileWrapper fileWrapper = dialog.save(baseDir, "bom.json");
        if (fileWrapper == null) {
            return;
        }

        Path savePath = fileWrapper.getFile().toPath();

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                ApiService apiService = ApplicationManager.getApplication().getService(ApiService.class);
                String sbomJson = apiService.generateSbom(
                        manifestFile.getName(),
                        manifestFile.getPath()
                );

                Files.writeString(savePath, sbomJson, StandardCharsets.UTF_8);

                ApplicationManager.getApplication().invokeLater(() ->
                        NotificationGroupManager.getInstance()
                                .getNotificationGroup("Red Hat Dependency Analytics")
                                .createNotification(
                                        "SBOM saved to " + savePath,
                                        NotificationType.INFORMATION
                                )
                                .notify(project)
                );
            } catch (Exception ex) {
                logger.error(ex);
                ApplicationManager.getApplication().invokeLater(() ->
                        Messages.showErrorDialog(
                                project,
                                "SBOM generation failed: " + ex.getLocalizedMessage(),
                                "Error"
                        )
                );
            }
        });
    }

    @Override
    public void update(AnActionEvent event) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            event.getPresentation().setEnabledAndVisible(
                    supportedManifestFiles.contains(psiFile.getName())
            );
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }
    }
}
