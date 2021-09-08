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

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.jboss.tools.intellij.analytics.PlatformDetectionException;
import org.jetbrains.annotations.NotNull;


public class SaReportEditorProvider implements FileEditorProvider, DumbAware {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        // If file name is matching with Report file name then allow it to open in custom editor.
        return virtualFile.getExtension().equals("sa");
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        try {
            // Create custom editor having browser attached to it.
            return new SaReportEditor(virtualFile);
        } catch (Exception e) {
            throw new PlatformDetectionException("Can not open editor.");
        }
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return "reportFileEditor";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        // Hide default Text tab and show tab having browser component
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
