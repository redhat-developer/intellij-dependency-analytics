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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.exhort.ApiService;

public class SaUtils {

    public JsonObject performSA(VirtualFile manifestFile) {
        // Get SA report for given manifest file.
        String reportLink;
        if ("pom.xml".equals(manifestFile.getName())
                || "package.json".equals(manifestFile.getName())
                || "go.mod".equals(manifestFile.getName())
                || "requirements.txt".equals(manifestFile.getName())
                || "build.gradle".equals(manifestFile.getName())) {
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
            case "go.mod":
                packageManager = "go";
                break;
            case "requirements.txt":
                packageManager = "python";
                break;
            case "build.gradle":
                packageManager = "gradle";
                break;

            default:
                throw new IllegalArgumentException("package manager not implemented");
        }
        return packageManager;
    }
}
