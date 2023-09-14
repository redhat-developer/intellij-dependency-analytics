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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.exhort.ApiService;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.Writer;

public class SaUtils {

    /**
     * <p>Open a custom editor window.</p>
     *
     * <p>The custom editor window will open a file which will have browser attached to it.</p>
     *
     * @param instance        An instance of FileEditorManager.
     * @param manifestDetails Manifest file details.
     * @throws IOException In case of process failure
     */
    public void openCustomEditor(FileEditorManager instance, JsonObject manifestDetails) throws IOException {

        // Close custom editor if already opened in previous run,
        // if its a different manifest file with same name then dont close existing one and open a new tab.
        manifestDetails = closeCustomEditor(instance, manifestDetails);

        // Create a temp file in which is registered with SaReportEditorProvider.
        File reportFile = File.createTempFile("exhort-", "_" + manifestDetails.get("manifestName").getAsString() + ".sa");

        //Save the SA Report URL in file, which will be loaded in browser
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile)))) {
            writer.write(manifestDetails.toString());
        }

        // Create a virtual file from report file
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(reportFile.getAbsolutePath());

        // Refresh the cached file from the physical file system.
        // if Virtual file is already opened in editor window from previous run then refresh file from physical file
        // else old web page will be shown in editor window.
        if (virtualFile == null) {
            throw new PlatformDetectionException("Dependency Analytics Report file is not created.");
        }
        virtualFile.refresh(false, true);

        // Open the virtual file in editor window, which will show the SA report.
        instance.openFile(virtualFile, true, false);
    }


    /**
     * <p>Close a custom editor window if already opened.</p>
     *
     * @param instance An instance of FileEditorManager.
     */
    private JsonObject closeCustomEditor(FileEditorManager instance, JsonObject manifestDetails) throws IOException {
        // Check if IDE is having open files in editor worker.
        if (instance.hasOpenFiles()) {
            // get all open files from editor workspace
            VirtualFile[] openFiles = instance.getOpenFiles();

            // iterate  through all files and if Report file is open then close it
            for (VirtualFile openFile : openFiles) {
                // Check if opened file extension is sa, and existing tab manifest file type is same as new (pom.xml, go.mod)
                // if not then no need to close any existing tab, just create new tab.
                if (openFile.getExtension().equals("sa")
                        && openFile.getNameWithoutExtension()
                        .replaceAll("^.*?_", "")
                        .equals(manifestDetails.get("manifestName").getAsString())) {

                    // If existing tab manifest type is same as new (pom.xml == pom.xml), then check if existing tab ss for same file by comparing the paths
                    String existingFilePath = new Gson().fromJson(VfsUtilCore.loadText(openFile), JsonObject.class).get("manifestPath").getAsString();
                    String currentFilePath = manifestDetails.get("manifestPath").getAsString();

                    // If file path is same then close existing tab
                    if (currentFilePath.equals(existingFilePath)) {
                        // Close the Report file in workspace,
                        // dispose method of SaReportEditor will delete file from filesystem as well.
                        instance.closeFile(openFile);

                        // Refresh the project from physical filesystem.
                        openFile.refresh(false, true);
                        break;
                    } else {
                        // If paths are not same it means file types are same but they are in different locations
                        // In that case show parent directory to distinguish between tabs
                        // Ex Dependency Analytics Report for requirements.txt and Dependency Analytics Report for test/requirements.txt
                        manifestDetails.addProperty("showParent", true);
                    }
                }
            }
        }
        return manifestDetails;
    }

    public JsonObject performSA(VirtualFile manifestFile) {
        // Get SA report for given manifest file.
        String reportLink;
        if ("pom.xml".equals(manifestFile.getName()) || "package.json".equals(manifestFile.getName())) {
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

            return manifestDetails;
        }
        return null;
    }

    private String determinePackageManagerName(String name) {
        String packageManager;
        switch (name) {
            case "pom.xml":
                packageManager = "maven";
                break;
            case "package.json":
                packageManager = "npm";
                break;
            default:
                throw new IllegalArgumentException("package manager not implemented");
        }
        return packageManager;
    }
}
