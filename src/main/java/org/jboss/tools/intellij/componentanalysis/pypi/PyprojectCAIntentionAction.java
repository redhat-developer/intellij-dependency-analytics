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
import com.intellij.psi.util.PsiTreeUtil;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlLiteral;

public final class PyprojectCAIntentionAction extends CAIntentionAction {

    PyprojectCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        if (version == null) {
            return;
        }

        if (element instanceof TomlLiteral literal) {
            // PEP 621: string literal in dependencies array like "anyio==3.6.2"
            String depString = PyprojectCAAnnotator.unquote(literal.getText());
            String name = PyprojectCAAnnotator.extractPep508Name(depString);
            if (name != null) {
                String extras = PyprojectCAAnnotator.extractPep508Extras(depString);
                String markers = PyprojectCAAnnotator.extractPep508Markers(depString);
                StringBuilder newDep = new StringBuilder(name);
                if (extras != null) {
                    newDep.append(extras);
                }
                newDep.append("==").append(version);
                if (markers != null) {
                    newDep.append(" ").append(markers);
                }
                replaceVersionLiteral(project, file, literal, newDep.toString());
            }
        } else if (element instanceof TomlKeyValue keyValue) {
            // Poetry: key-value pair like anyio = "^3.6.2"
            TomlLiteral valueLiteral = findVersionLiteral(keyValue);
            if (valueLiteral != null) {
                replaceVersionLiteral(project, file, valueLiteral, "==" + version);
            }
        }
    }

    private TomlLiteral findVersionLiteral(TomlKeyValue keyValue) {
        if (keyValue.getValue() instanceof TomlLiteral literal) {
            // Poetry simple string: anyio = "^3.6.2"
            return literal;
        }
        if (keyValue.getValue() instanceof TomlInlineTable inlineTable) {
            // Poetry inline table: anyio = {version = "^3.6.2"}
            for (TomlKeyValue entry : PsiTreeUtil.getChildrenOfTypeAsList(inlineTable, TomlKeyValue.class)) {
                if ("version".equals(entry.getKey().getText()) && entry.getValue() instanceof TomlLiteral) {
                    return (TomlLiteral) entry.getValue();
                }
            }
        }
        return null;
    }

    private void replaceVersionLiteral(@NotNull Project project, PsiFile file, TomlLiteral literal, String version) {
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document != null) {
            String oldText = literal.getText();
            String newVersionText;

            if (oldText.startsWith("\"") && oldText.endsWith("\"")) {
                newVersionText = "\"" + version + "\"";
            } else if (oldText.startsWith("'") && oldText.endsWith("'")) {
                newVersionText = "'" + version + "'";
            } else {
                newVersionText = "\"" + version + "\"";
            }

            int startOffset = literal.getTextRange().getStartOffset();
            int endOffset = literal.getTextRange().getEndOffset();

            document.replaceString(startOffset, endOffset, newVersionText);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new PyprojectCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "pyproject.toml".equals(file.getName())
                && (element instanceof TomlKeyValue || element instanceof TomlLiteral);
    }
}
