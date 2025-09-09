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

package org.jboss.tools.intellij.componentanalysis.golang;

import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.exhort.api.v4.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GoCAIntentionAction extends CAIntentionAction {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\s+)(v?[0-9]+\\.[0-9]+\\.[0-9]+\\S*)");

    GoCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document == null) return;

        // Find the line containing our element
        int offset = element.getTextOffset();
        int lineNumber = document.getLineNumber(offset);
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText().substring(lineStart, lineEnd);

        // Replace the version in the line
        Matcher matcher = VERSION_PATTERN.matcher(lineText);
        if (matcher.find()) {
            String newLineText = lineText.substring(0, matcher.start(2)) + version + lineText.substring(matcher.end(2));
            document.replaceString(lineStart, lineEnd, newLineText);
        }
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new GoCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "go.mod".equals(file.getName());
    }
}