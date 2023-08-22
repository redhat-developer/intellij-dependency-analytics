/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.stackanalysis;


import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import org.jboss.tools.intellij.exhort.ApiService;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SaAction extends AnAction {
    private static final Logger logger = Logger.getInstance(SaAction.class);

    private static final List<String> supportedManifestFiles = Arrays.asList(
            "pom.xml",
            "package.json"
//            Disable support for go and python
//            , "go.mod", "requirements.txt", "requirements-dev.txt"
    );

    public SaAction() {

    }

    /**
     * <p>Intellij Plugin Action implementation for triggering SA.</p>
     * <p>
     * Analysis will be performed on the file for which Action is triggered and Report will be shown in editor workspace.
     *
     * @param event An instance of AnActionEvent.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        try {
            SaUtils saUtils = new SaUtils();
            VirtualFile manifestFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);

            // Get SA report for given manifest file.
            String reportLink;
            if ("pom.xml".equals(manifestFile.getName()) || "package.json".equals(manifestFile.getName()) ) {
                ApiService apiService = ServiceManager.getService(ApiService.class);
                reportLink = apiService.getStackAnalysis(
                  determinePackageManagerName(manifestFile.getName()),
                  manifestFile.getName(),
                  manifestFile.getPath()
                ).toUri().toString();

                // Manifest file details to be saved in temp file which will be used while opening Report tab
                JsonObject manifestDetails = new JsonObject();
                manifestDetails.addProperty("showParent", false);
                manifestDetails.addProperty("manifestName", manifestFile.getName());
                manifestDetails.addProperty("manifestPath", manifestFile.getPath());
                manifestDetails.addProperty("manifestFileParent", manifestFile.getParent().getName());
                manifestDetails.addProperty("report_link", reportLink);
                manifestDetails.addProperty("manifestNameWithoutExtension", manifestFile.getNameWithoutExtension());

                // Open custom editor window which will load SA Report in browser attached to it.
                saUtils.openCustomEditor(FileEditorManager.getInstance(event.getProject()), manifestDetails);
            }
        } catch (Exception e) {
            logger.warn(e);
            Messages.showErrorDialog(event.getProject(),
                    "Can't run report generation " + e.getLocalizedMessage(),
                    "Error");
        }
    }

    private String determinePackageManagerName(String name) {
        String packageManager;
        switch(name)
        {
            case "pom.xml":
                packageManager = "maven";
                break;
            case "package.json":
                packageManager =  "npm";
                break;
            default:
                throw new IllegalArgumentException("package manager not implemented");
        }
        return packageManager;
    }


    /**
     * <p>Updates the state of the action, Action is show if this method returns true.</p>
     *
     * @param event An instance of AnActionEvent.
     */
    @Override
    public void update(AnActionEvent event) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        // Check if file where context menu is opened is type of supported extension.
        // If yes then show the action for SA in menu
        if (psiFile != null) {
            event.getPresentation().setEnabledAndVisible(supportedManifestFiles
                    .contains(psiFile.getName()));
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }
    }
}
