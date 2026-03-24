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

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class LicenseUpdateIntentionAction implements IntentionAction {

    private final @FileModifier.SafeFieldForPreview PsiElement element;
    private final String newLicense;
    private final BiConsumer<PsiElement, String> replacer;

    public LicenseUpdateIntentionAction(PsiElement element, String newLicense, BiConsumer<PsiElement, String> replacer) {
        this.element = element;
        this.newLicense = newLicense;
        this.replacer = replacer;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return "Update manifest license to \"" + newLicense + "\" (from LICENSE file)";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return element != null && element.isValid();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        replacer.accept(element, newLicense);

        // Invalidate caches so the next annotator pass triggers a fresh API call
        if (file != null && file.getVirtualFile() != null) {
            CAService.deleteReports(file.getVirtualFile().getPath());
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public @Nullable FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        PsiElement copy = PsiTreeUtil.findSameElementInCopy(this.element, target);
        return new LicenseUpdateIntentionAction(copy, this.newLicense, this.replacer);
    }
}
