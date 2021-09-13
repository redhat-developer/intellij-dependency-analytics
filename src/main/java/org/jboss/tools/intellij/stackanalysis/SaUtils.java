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
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jboss.tools.intellij.analytics.Platform;
import org.jboss.tools.intellij.analytics.PlatformDetectionException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.GZIPInputStream;

public class SaUtils {

    /**
     * <p>Get Stack Analysis Report for given manifest file using CLI.</p>
     *
     * @param filePath Path to target manifest file
     *
     * @return A JSONObject having SA Report.
     *
     * @throws IOException In case of process failure
     * @throws InterruptedException In case of process failure
     */
    public JsonObject getReport(String filePath) throws IOException, InterruptedException {
        // Get the SA report using CLI and return JSON data
        return new Gson().fromJson(new SaProcessExecutor().performStackAnalysis(filePath), JsonObject.class);
    }


    /**
     * <p>Open a custom editor window.</p>
     *
     * <p>The custom editor window will open a file which will have browser attached to it.</p>
     *
     * @param instance An instance of FileEditorManager.
     * @param manifestDetails Manifest file details.
     *
     * @throws IOException In case of process failure
     */
    public void openCustomEditor(FileEditorManager instance, JsonObject manifestDetails) throws IOException {

        // Close custom editor if already opened in previous run,
        // if its a different manifest file with same name then dont close existing one and open a new tab.
        manifestDetails = closeCustomEditor(instance, manifestDetails);

        // Create a temp file in which is registered with SaReportEditorProvider.
        File reportFile = File.createTempFile("CRDA-", "_"+manifestDetails.get("manifestName").getAsString()+".sa");

        //Save the SA Report URL in file, which will be loaded in browser
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile)))) {
            writer.write(manifestDetails.toString());
        }

        // Create a virtual file from report file
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(reportFile.getAbsolutePath());

        // Refresh the cached file from the physical file system.
        // if Virtual file is already opened in editor window from previous run then refresh file from physical file
        // else old web page will be shown in editor window.
        if (virtualFile == null){
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
            for (VirtualFile openFile : openFiles){
                // Check if opened file extension is sa, and existing tab manifest file type is same as new (pom.xml, go.mod)
                // if not then no need to close any existing tab, just create new tab.
                if (openFile.getExtension().equals("sa")
                        && openFile.getNameWithoutExtension()
                        .replaceAll("^.*?_", "")
                        .equals(manifestDetails.get("manifestName").getAsString())){

                    // If existing tab manifest type is same as new (pom.xml == pom.xml), then check if existing tab ss for same file by comparing the paths
                    String existingFilePath = new Gson().fromJson(VfsUtilCore.loadText(openFile), JsonObject.class).get("manifestPath").getAsString();
                    String currentFilePath = manifestDetails.get("manifestPath").getAsString();

                    // If file path is same then close existing tab
                    if (currentFilePath.equals(existingFilePath)){
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


    /**
     * <p>Extract given tar.gz file.</p>
     *
     * @param cliTarBallName Tar file to be extracted.
     * @param cliBinaryName File which need to be extracted from tar
     *
     * @throws IOException In case of process failure
     */
    public void unTarBundle(final String cliTarBallName, final String cliBinaryName) throws IOException {
        // Logic to extract downloaded file into a directory

        // Get plugin directory to store extracted data
        String sandBox = Platform.pluginDirectory;

        // CLI Binary file
        final File cliBinaryDest = new File(sandBox, cliBinaryName);

        try (FileInputStream fileInputStream = new FileInputStream(sandBox + File.separator + cliTarBallName);
             GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fileInputStream));
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream)) {

            TarArchiveEntry tarEntry;
            while ((tarEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
                File outputFile = new File(sandBox + File.separator + tarEntry.getName());
                outputFile.getParentFile().mkdirs();
                try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(tarArchiveInputStream, fileOutputStream);
                }
            }
            cliBinaryDest.setExecutable(true);
        }
    }
}
