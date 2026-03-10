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

package org.jboss.tools.intellij.componentanalysis.cargo;

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
import com.intellij.psi.util.PsiTreeUtil;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlTableHeader;
import org.toml.lang.psi.TomlValue;

public final class CargoCAIntentionAction extends CAIntentionAction {

    CargoCAIntentionAction(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        super(element, source, report);
    }

    @Override
    protected void updateVersion(@NotNull Project project, Editor editor, PsiFile file, String version) {
        if (version == null) {
            return;
        }

        if (element instanceof TomlKeyValue) {
            updateInlineFormatVersion(project, file, (TomlKeyValue) element, version);
        } else if (element instanceof TomlTableHeader) {
            updateStandardTableVersion(project, file, (TomlTableHeader) element, version);
        }
    }

    private void updateInlineFormatVersion(@NotNull Project project, PsiFile file, TomlKeyValue keyValue, String version) {
        TomlValue value = keyValue.getValue();

        if (value instanceof TomlLiteral) {
            // Simple string version: serde = "1.0"
            replaceVersionLiteral(project, file, (TomlLiteral) value, version);
        } else if (value instanceof TomlInlineTable inlineTable) {
            // Complex object format: tokio = { version = "1.0", features = ["full"] }
            for (TomlKeyValue entry : PsiTreeUtil.getChildrenOfTypeAsList(inlineTable, TomlKeyValue.class)) {
                if ("version".equals(entry.getKey().getText()) && entry.getValue() instanceof TomlLiteral) {
                    replaceVersionLiteral(project, file, (TomlLiteral) entry.getValue(), version);
                    break;
                }
            }
        }
    }

    private void replaceVersionLiteral(@NotNull Project project, PsiFile file, TomlLiteral literal, String version) {
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document != null) {
            String oldText = literal.getText();
            String newVersionText;

            // Preserve quote style
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

    private void updateStandardTableVersion(@NotNull Project project, PsiFile file, TomlTableHeader header, String version) {
        // Standard table format: [dependencies.cratename] with version = "x.y" as a child key-value
        TomlTable table = (TomlTable) header.getParent();
        for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class)) {
            if ("version".equals(kv.getKey().getText()) && kv.getValue() instanceof TomlLiteral) {
                replaceVersionLiteral(project, file, (TomlLiteral) kv.getValue(), version);
                break;
            }
        }
    }

    @Override
    protected @Nullable FileModifier createCAIntentionActionInCopy(PsiElement element) {
        return new CargoCAIntentionAction(element, this.source, this.report);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && "Cargo.toml".equals(file.getName()) &&
               (element instanceof TomlKeyValue || element instanceof TomlTableHeader);
    }
}