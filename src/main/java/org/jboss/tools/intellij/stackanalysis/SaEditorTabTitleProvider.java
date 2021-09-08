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
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class SaEditorTabTitleProvider implements EditorTabTitleProvider{
    @Override
    public @NotNull String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile file) {
        // Check if file opened in Editor is SA report, if Yes then change the title of Custom Editor Tab
        // This won't rename the physical file, hence physical temp file will still remain unique.
        String tabName = file.getName();
        if ("sa".equals(file.getExtension())) {
            try {
                JsonObject manifestDetails = new Gson().fromJson(VfsUtilCore.loadText(file), JsonObject.class);
                // If a tab is already opened for same manifest type then add parent directory to distinguish between tabs
                if (manifestDetails.get("showParent").getAsBoolean()) {
                    tabName = "Dependency Analytics Report for "+manifestDetails.get("manifestFileParent").getAsString()+"/"+manifestDetails.get("manifestName").getAsString();
                } else {
                    tabName = "Dependency Analytics Report for "+manifestDetails.get("manifestName").getAsString();
                }
            } catch (IOException e) {
                tabName = "Dependency Analytics Report";
            }
        }
        return tabName;
    }
}
