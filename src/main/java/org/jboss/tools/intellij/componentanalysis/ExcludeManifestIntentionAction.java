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

package org.jboss.tools.intellij.componentanalysis;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExcludeManifestIntentionAction implements IntentionAction {

    @Override
    public @IntentionName @NotNull String getText() {
        return "Exclude this manifest from component analysis";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (file == null || file.getVirtualFile() == null) {
            return false;
        }
        
        String fileName = file.getName();
        return "pom.xml".equals(fileName) || 
               "package.json".equals(fileName) || 
               "go.mod".equals(fileName) || 
               "requirements.txt".equals(fileName) || 
               "build.gradle".equals(fileName);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return;
        }

        VirtualFile projectRoot = project.getBaseDir();
        if (projectRoot == null) {
            return;
        }

        String filePath = virtualFile.getPath();
        String projectPath = projectRoot.getPath();
        
        if (filePath.startsWith(projectPath)) {
            String relativePath = filePath.substring(projectPath.length());
            if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                relativePath = relativePath.substring(1);
            }
            
            ManifestExclusionManager.addExclusionPattern(relativePath, project);
            
            ApplicationManager.getApplication().runReadAction(() -> {
                DaemonCodeAnalyzer.getInstance(project).restart(file);
            });
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public @Nullable FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return null;
    }
}