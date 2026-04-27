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

package org.jboss.tools.intellij.componentanalysis.pypi;

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.psi.TomlLiteral;

public final class PyprojectCAIntentionAction extends CAIntentionAction {

    PyprojectCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        if (version == null || !(element instanceof TomlLiteral literal)) {
            return;
        }

        String text = literal.getText();
        if (text.length() < 2 || !text.startsWith("\"") || !text.endsWith("\"")) {
            return;
        }

        String depString = text.substring(1, text.length() - 1);
        String name = PyprojectCAAnnotator.parseName(depString);
        if (name == null) {
            return;
        }

        String newText = "\"" + name + "==" + version + "\"";
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document != null) {
            int start = literal.getTextRange().getStartOffset();
            int end = literal.getTextRange().getEndOffset();
            document.replaceString(start, end, newText);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new PyprojectCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "pyproject.toml".equals(file.getName()) && element instanceof TomlLiteral;
    }
}
