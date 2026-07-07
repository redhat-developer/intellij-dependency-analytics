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

package org.jboss.tools.intellij.image;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import org.jboss.tools.intellij.image.build.psi.DockerfileFromInstruction;
import org.jboss.tools.intellij.image.build.psi.DockerfileImageName;
import org.jetbrains.annotations.NotNull;

/**
 * Intention action that replaces a Dockerfile FROM line's image reference
 * with a recommended Red Hat Hardened Image.
 */
public class HardenedImageIntentionAction implements IntentionAction {

    private final String imageReference;

    public HardenedImageIntentionAction(String imageReference) {
        this.imageReference = imageReference;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Replace with Red Hat Hardened Image: " + imageReference;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return DockerfileFileType.isDockerfile(psiFile);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        DockerfileFromInstruction fromInstruction =
                PsiTreeUtil.getParentOfType(element, DockerfileFromInstruction.class, false);
        if (fromInstruction != null) {
            DockerfileImageName imageName = fromInstruction.getImageName();
            if (imageName != null) {
                Document document = editor.getDocument();
                TextRange range = imageName.getTextRange();
                document.replaceString(range.getStartOffset(), range.getEndOffset(), imageReference);
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
