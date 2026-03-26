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
package org.jboss.tools.intellij.report;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class AnalyticsReportUtils {

    private static final Logger LOG = Logger.getInstance(AnalyticsReportUtils.class);

    /**
     * <p>Open a custom editor window.</p>
     *
     * <p>The custom editor window will open a file which will have browser attached to it.</p>
     *
     * <p>Must be called on the EDT. The slow VFS operation ({@code refreshAndFindFileByPath})
     * is dispatched internally to a background thread to avoid blocking the EDT.</p>
     *
     * @param instance        An instance of FileEditorManager.
     * @param manifestDetails Manifest file details.
     * @throws IOException In case of process failure
     */
    public void openCustomEditor(FileEditorManager instance, JsonObject manifestDetails) throws IOException {

        // Close custom editor if already opened in previous run,
        // if it's a different manifest file with same name then don't close existing one and open a new tab.
        // NOTE: must run before file creation so that showParent flag is included in the written content.
        manifestDetails = closeCustomEditor(instance, manifestDetails);

        // Create a temp file in which is registered with AnalyticsReportEditorProvider.
        // Standard Java file I/O — not a VFS operation, safe on EDT.
        File reportFile = File.createTempFile("exhort-", "_" + manifestDetails.get("manifestName").getAsString() + ".ar");

        //Save the Analytics Report URL in file, which will be loaded in browser
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile)))) {
            writer.write(manifestDetails.toString());
        }

        // Save permanent copy of report to user-configured directory if specified
        String htmlFilePath = manifestDetails.get("report_link").getAsString();
        if (htmlFilePath.startsWith("file://")) {
            String actualFilePath = htmlFilePath.substring(7); // Remove "file://" prefix
            ReportFileManager.saveReportCopy(
                actualFilePath,
                manifestDetails.get("manifestName").getAsString()
            );
        }

        // refreshAndFindFileByPath is a slow VFS operation prohibited on EDT.
        // Dispatch to a background thread; once the VirtualFile is obtained, switch back to EDT to open it.
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(reportFile.getAbsolutePath());
            if (virtualFile == null) {
                LOG.error("Dependency Analytics Report file is not created.");
                return;
            }
            // refreshAndFindFileByPath already performed a synchronous refresh and fully
            // registered the file with the VFS — no second refresh is needed.
            // Switch to EDT to open the editor tab.
            ApplicationManager.getApplication().invokeLater(() -> instance.openFile(virtualFile, true, false));
        });
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
                // Check if opened file extension is ar, and existing tab manifest file type is same as new (pom.xml, go.mod)
                // if not then no need to close any existing tab, just create new tab.
                if ("ar".equals(openFile.getExtension())
                        && openFile.getNameWithoutExtension()
                        .replaceAll("^.*?_", "")
                        .equals(manifestDetails.get("manifestName").getAsString())) {

                    // If existing tab manifest type is same as new (pom.xml == pom.xml), then check if existing tab ss for same file by comparing the paths
                    String existingFilePath = new Gson().fromJson(VfsUtilCore.loadText(openFile), JsonObject.class).get("manifestPath").getAsString();
                    String currentFilePath = manifestDetails.get("manifestPath").getAsString();

                    // If file path is same then close existing tab
                    if (currentFilePath.equals(existingFilePath)) {
                        // Close the Report file in workspace,
                        // dispose method of AnalyticsReportEditor will delete file from filesystem as well.
                        instance.closeFile(openFile);

                        // Refresh the project from physical filesystem.
                        openFile.refresh(true, true);
                        break;
                    } else {
                        // If paths are not same it means file types are same, but they are in different locations
                        // In that case show parent directory to distinguish between tabs
                        // Ex Dependency Analytics Report for requirements.txt and Dependency Analytics Report for test/requirements.txt
                        manifestDetails.addProperty("showParent", true);
                    }
                }
            }
        }
        return manifestDetails;
    }
}
