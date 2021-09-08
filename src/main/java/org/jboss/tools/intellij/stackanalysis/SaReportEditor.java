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
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;


public class SaReportEditor extends UserDataHolderBase implements FileEditor, DumbAware {
    private final SaReport saReport;
    private final VirtualFile virtualFile;

    public SaReportEditor(VirtualFile virtualFile) throws IOException {
        // Virtual file having SA Report attached.
        this.virtualFile = virtualFile;

        // SA Report (JCEF Browser)
        this.saReport = new SaReport(new Gson()
                .fromJson(VfsUtilCore.loadText(virtualFile), JsonObject.class)
                .get("report_link").getAsString());
    }

    @Override
    public @NotNull JComponent getComponent() {
        return this.saReport.getContent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return getComponent();
    }

    @Override
    public @NotNull String getName() { return ""; }

    @Override
    public void setState(@NotNull FileEditorState state) { }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {}

    @Override
    public void deselectNotify() {}

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        // Delete the Report file from filesystem when custom editor window is closed.
        File file = new File(this.virtualFile.getPath());
        file.delete();
    }

    @NotNull
    @Override
    public VirtualFile getFile() { return this.virtualFile; }
}
