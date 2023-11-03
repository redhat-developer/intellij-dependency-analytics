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

package org.jboss.tools.intellij.componentanalysis.pypi.pro;

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.redhat.exhort.api.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.meanmail.fileTypes.RequirementsFileType;
import ru.meanmail.psi.NameReq;
import ru.meanmail.psi.RequirementsFile;
import ru.meanmail.psi.Versionspec;

public final class PipCAIntentionAction extends CAIntentionAction {
    PipCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        Versionspec versionspec = ((NameReq) element).getVersionspec();
        if (versionspec != null) {
            RequirementsFile dummyFile = (RequirementsFile) PsiFileFactory.getInstance(project).createFileFromText(
                    "dummy-requirements.txt", RequirementsFileType.INSTANCE,
                    ((NameReq) element).getName().getText() + " == " + version);
            NameReq newNameReq = (NameReq) dummyFile.getFirstChild();
            if (newNameReq.getVersionspec() != null) {
                versionspec.replace(newNameReq.getVersionspec());
            }
        }
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new PipCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "requirements.txt".equals(file.getName());
    }
}
