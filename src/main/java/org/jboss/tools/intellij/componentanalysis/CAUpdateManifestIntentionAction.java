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

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.redhat.exhort.api.DependencyReport;
import org.jetbrains.annotations.NotNull;

public abstract class CAUpdateManifestIntentionAction implements IntentionAction {

    protected @SafeFieldForPreview PsiElement element;
    protected @SafeFieldForPreview DependencyReport dependency;


    protected CAUpdateManifestIntentionAction(PsiElement element, DependencyReport dependency) {
        this.element = element;
        this.dependency = dependency;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return this.getTextImpl();
    }

    protected abstract String getTextImpl();

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "RHDA";
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        this.updateManifest(project, editor, file, this.dependency);
    }

    protected abstract void updateManifest(Project project, Editor editor, PsiFile file, DependencyReport dependency);

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    
}
