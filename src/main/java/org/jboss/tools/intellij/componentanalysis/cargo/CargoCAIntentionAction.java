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
import org.toml.lang.psi.TomlKeyValue;
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

        if (value instanceof TomlLiteral literal) {
            // Simple string version: serde = "1.0"

            // Use document-based replacement for TOML literals
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
        // TODO: Handle complex object format: { version = "1.0", features = [...] }
        // This would require more complex PSI manipulation for TomlInlineTable
    }

    private void updateStandardTableVersion(@NotNull Project project, PsiFile file, TomlTableHeader header, String version) {
        // For standard table format [dependencies.cratename], we need to find or create version key
        // TODO: Implement standard table version update
        // This is more complex as it requires PSI manipulation to add/modify version key in the table
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