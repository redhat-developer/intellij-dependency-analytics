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
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/** Intention action that directs users to the Red Hat Hardened Container Images catalog. */
public class HardenedImageIntentionAction implements IntentionAction {

    public static final String HARDENED_IMAGE_LINK = "https://catalog.redhat.com/software/containers/search?gs&q=hardened";

    @Override
    public @IntentionName @NotNull String getText() {
        return "Switch to a Red Hat Hardened Image for enhanced security";
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
        BrowserUtil.browse(URI.create(HARDENED_IMAGE_LINK));
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
