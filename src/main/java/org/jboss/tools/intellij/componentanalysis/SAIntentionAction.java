/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jboss.tools.intellij.stackanalysis.SaUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SAIntentionAction implements IntentionAction {
    @Override
    public @IntentionName @NotNull String getText() {
        return "Detailed Vulnerability Report";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (file == null) {
            return false;
        }
        return "pom.xml".equals(file.getName())
                || "package.json".equals(file.getName())
                || "go.mod".equals(file.getName())
                || "requirements.txt".equals(file.getName());
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        SaUtils saUtils = new SaUtils();
        VirtualFile vf = file.getVirtualFile();

        if (vf != null) {
            JsonObject manifestDetails = saUtils.performSA(vf);
            if (manifestDetails != null) {
                try {
                    saUtils.openCustomEditor(FileEditorManager.getInstance(project), manifestDetails);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
