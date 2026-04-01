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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.LicenseUpdateIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.editor.Document;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlKeySegment;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlTableHeader;
import org.toml.lang.psi.TomlValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.DEPENDENCIES;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.TRUSTIFY_DA_IGNORE;

public class CargoCAAnnotator extends CAAnnotator {

    private static final String CARGO = "cargo";
    private static final String CARGO_TOML = "Cargo.toml";
    private static final String TARGET = "target";
    private static final String VERSION = "version";
    private static final String WORKSPACE = "workspace";

    /**
     * Placeholder value used when a dependency's version cannot be directly extracted from Cargo.toml.
     * This occurs in cases such as:
     * - workspace = true (version inherited from workspace)
     * - git, path, or registry sources without an explicit version
     * This value signals that the real version can not be resolved from Cargo.toml
     */
    private static final String UNRESOLVED_VERSION = "__UNRESOLVED__";

    @Override
    protected String getInspectionShortName() {
        return CargoCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if (!"Cargo.toml".equals(file.getName())) {
            return Map.of();
        }

        Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();

        Set<String> commentIgnoredDeps = getIgnoredDependencies(file);

        List<TomlTable> tables = PsiTreeUtil.getChildrenOfTypeAsList(file, TomlTable.class);
        for (TomlTable table : tables) {
            TomlTableHeader header = table.getHeader();
            if (isDependencyTable(header)) {
                if (isStandardTableDependency(header)) {
                    // Standard table format: [dependencies.cratename]
                    parseSingleDependencyTable(table, header, commentIgnoredDeps, resultMap);
                } else {
                    // Inline format: [dependencies] with key-value pairs
                    // or
                    // Target-specific or workspace format: [target.xxx.dependencies], [workspace.dependencies]
                    parseFlatDependencies(table, commentIgnoredDeps, resultMap);
                }
            }
        }
        return resultMap;
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new CargoCAIntentionAction(element, source, report);
    }

    @Override
    protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport report) {
        return null;
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return (element instanceof TomlKeyValue || element instanceof TomlTableHeader) &&
               element.getContainingFile() != null &&
               CARGO_TOML.equals(element.getContainingFile().getName());
    }

    private Set<String> getIgnoredDependencies(PsiFile file) {
        Set<String> ignoredDeps = new HashSet<>();
        Collection<PsiComment> comments = PsiTreeUtil.collectElementsOfType(file, PsiComment.class);
        for (PsiComment comment : comments) {
            String commentText = comment.getText();
            if (commentText.contains(TRUSTIFY_DA_IGNORE) || commentText.contains(EXHORT_IGNORE)) {
                String dependencyName = findAssociatedDependency(file, comment);
                if (dependencyName != null) {
                    ignoredDeps.add(dependencyName);
                }
            }
        }
        return ignoredDeps;
    }

    private String findAssociatedDependency(PsiFile file, PsiComment comment) {
        // 1: Check if comment is on same line as a TomlKeyValue (inline dependency)
        // Handles: serde = "1.0.150" # trustify-da-ignore
        // Handles: tokio = { version = "1.0", features = ["full"] } # trustify-da-ignore
        TomlKeyValue keyValueAtSameLine = findKeyValueOnSameLine(file, comment);
        if (keyValueAtSameLine != null && isInDependencyTable(keyValueAtSameLine)) {
            return normalizeKeyName(keyValueAtSameLine.getKey().getText());
        }
        // 2: Check if comment is on same line as a TomlTable header
        // Handles: [dependencies.reqwest] # trustify-da-ignore
        TomlTable tableWithComment = findTableWithCommentOnSameLine(file, comment);
        TomlTableHeader header;
        if (tableWithComment != null) {
            header = tableWithComment.getHeader();
            if (isDependencyTable(header)) {
                return extractDependencyNameFromTableHeader(header);
            }
        }
        return null;
    }

    private TomlKeyValue findKeyValueOnSameLine(PsiFile file, PsiComment comment) {
        int commentLine = getLineNumber(file, comment);
        Collection<TomlKeyValue> allKeyValues = PsiTreeUtil.collectElementsOfType(file, TomlKeyValue.class);
        for (TomlKeyValue keyValue : allKeyValues) {
            int keyValueLine = getLineNumber(file, keyValue);
            if (commentLine == keyValueLine) {
                return keyValue;
            }
        }
        return null;
    }

    private TomlTable findTableWithCommentOnSameLine(PsiFile file, PsiComment comment) {
        // Check if comment is on the same line as a table header
        TomlTable table = PsiTreeUtil.getParentOfType(comment, TomlTable.class);
        if (table != null) {
            TomlTableHeader header = table.getHeader();
            // Check if comment and header are on the same line
            int commentLine = getLineNumber(file, comment);
            int headerLine = getLineNumber(file, header);
            if (commentLine == headerLine) {
                return table;
            }
        }
        return null;
    }

    private int getLineNumber(PsiFile file, PsiElement element) {
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
        if (document == null) {
            return -1;
        }
        int offset = element.getTextRange().getStartOffset();
        return document.getLineNumber(offset);
    }

    private boolean isInDependencyTable(TomlKeyValue keyValue) {
        TomlTable parentTable = PsiTreeUtil.getParentOfType(keyValue, TomlTable.class);
        return parentTable != null && isDependencyTable(parentTable.getHeader());
    }

    private String extractDependencyNameFromTableHeader(TomlTableHeader header) {
        TomlKey key = header.getKey();
        if (key == null) {
            return null;
        }

        List<TomlKeySegment> segments = key.getSegments();
        if (segments.size() < 2) {
            return null; // Not a nested dependency section
        }

        // Find the "dependencies" part and get the next segment as dependency name
        for (int i = 0; i < segments.size() - 1; i++) {
            if (DEPENDENCIES.equals(segments.get(i).getName())) {
                return segments.get(i + 1).getName();
            }
        }

        return null;
    }

    private String normalizeKeyName(String keyText) {
        // Remove surrounding quotes from TOML keys: "serde-json" -> serde-json
        if ((keyText.startsWith("\"") && keyText.endsWith("\"")) ||
                (keyText.startsWith("'") && keyText.endsWith("'"))) {
            if (keyText.length() > 2) {
                return keyText.substring(1, keyText.length() - 1);
            }
        }
        return keyText;
    }

    private boolean isDependencyTable(TomlTableHeader header) {
        TomlKey key = header.getKey();
        if (key == null || key.getSegments().isEmpty()) {
            return false;
        }

        var segments = key.getSegments();
        int n = segments.size();
        String last = segments.get(n - 1).getName();

        if (DEPENDENCIES.equals(last)) {
            // [dependencies] or [workspace.dependencies] or [target.xxx.dependencies]
            return n == 1 || WORKSPACE.equals(segments.get(0).getName()) || TARGET.equals(segments.get(0).getName());
        }

        // [dependencies.cratename] case
        if (n >= 2 && DEPENDENCIES.equals(segments.get(n - 2).getName())) {
            // only allow exactly one level after dependencies
            return n == 2 && DEPENDENCIES.equals(segments.get(0).getName());
        }

        return false;
    }

    private boolean isStandardTableDependency(TomlTableHeader header) {
        // Check if this is standard table format: [dependencies.cratename]
        // where first segment is "dependencies" and there are multiple segments
        TomlKey key = header.getKey();
        if (key == null) {
            return false;
        }
        List<TomlKeySegment> segments = key.getSegments();
        if (segments.size() < 2) {
            return false; // Need at least 2 segments for [dependencies.cratename]
        }
        // Standard table format: first segment is "dependencies"
        return DEPENDENCIES.equals(segments.get(0).getName());
    }

    private void parseSingleDependencyTable(TomlTable table, TomlTableHeader header, Set<String> ignoredDeps, Map<Dependency, List<PsiElement>> resultMap) {
        /*
          Standard table format:
          [dependencies.cratename]
          version = "0.11"
          features = ["json", "blocking"]
         */

        String crateName = extractDependencyNameFromTableHeader(header);
        if (crateName == null || ignoredDeps.contains(crateName)) {
            return;
        }

        String version = UNRESOLVED_VERSION;
        List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class);
        for (TomlKeyValue keyValue : keyValues) {
            String keyName = normalizeKeyName(keyValue.getKey().getText());
            if (VERSION.equals(keyName)) {
                version = extractVersionFromTomlValue(keyValue.getValue());
                break;
            }
        }

        Dependency dp = new Dependency(CARGO, null, crateName, version);
        // Use the table header as the PSI element for standard table format
        resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(header);
    }

    private String extractVersionFromTomlValue(TomlValue tomlValue) {
        if (tomlValue == null) {
            return UNRESOLVED_VERSION;
        }

        if (tomlValue instanceof TomlLiteral literal) {
            String text = literal.getText();
            if (text.startsWith("\"") && text.endsWith("\"") && text.length() > 2) {
                return text.substring(1, text.length() - 1);
            }
            return UNRESOLVED_VERSION;
        }

        // Handle complex object: tokio = { version = "1.0", features = ["full"] }
        if (tomlValue instanceof TomlInlineTable inlineTable) {
            List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(inlineTable, TomlKeyValue.class);

            for (TomlKeyValue keyValue : keyValues) {
                String keyName = normalizeKeyName(keyValue.getKey().getText());
                if (VERSION.equals(keyName)) {
                    return extractVersionFromTomlValue(keyValue.getValue());
                }
            }

            // If no version field, check for workspace, git, or path dependencies
            // These have no explicit version and need resolution
            for (TomlKeyValue keyValue : keyValues) {
                String keyName = normalizeKeyName(keyValue.getKey().getText());
                if (WORKSPACE.equals(keyName) || "git".equals(keyName) || "path".equals(keyName) || "registry".equals(keyName)) {
                    return UNRESOLVED_VERSION;
                }
            }
        }

        return UNRESOLVED_VERSION;
    }

    @Override
    protected @Nullable PsiElement getLicenseFieldPsiElement(PsiFile file) {
        // Find [package] table, then "license" key-value
        List<TomlTable> tables = PsiTreeUtil.getChildrenOfTypeAsList(file, TomlTable.class);
        for (TomlTable table : tables) {
            TomlTableHeader header = table.getHeader();
            TomlKey key = header.getKey();
            if (key == null) continue;
            List<TomlKeySegment> segments = key.getSegments();
            if (segments.size() == 1 && "package".equals(segments.get(0).getName())) {
                List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class);
                for (TomlKeyValue kv : keyValues) {
                    if ("license".equals(normalizeKeyName(kv.getKey().getText()))) {
                        TomlValue value = kv.getValue();
                        if (value instanceof TomlLiteral) {
                            return value;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected @Nullable LicenseUpdateIntentionAction createLicenseUpdateFix(PsiElement element, String newLicense) {
        if (!(element instanceof TomlLiteral)) {
            return null;
        }
        return new LicenseUpdateIntentionAction(element, newLicense, (el, license) -> {
            // Replace the TOML literal text (including quotes), matching CargoCAIntentionAction pattern
            Document doc = PsiDocumentManager.getInstance(el.getProject())
                    .getDocument(el.getContainingFile());
            if (doc != null) {
                int start = el.getTextRange().getStartOffset();
                int end = el.getTextRange().getEndOffset();
                doc.replaceString(start, end, "\"" + license + "\"");
                PsiDocumentManager.getInstance(el.getProject()).commitDocument(doc);
            }
        });
    }

    private void parseFlatDependencies(TomlTable table, Set<String> ignoredDeps, Map<Dependency, List<PsiElement>> resultMap) {
        // Parse inline format: [dependencies] with key-value pairs
        // serde = "1.0"
        // tokio = { version = "1.0", features = ["full"] }
        List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class);
        for (TomlKeyValue keyValue : keyValues) {
            String crateName = normalizeKeyName(keyValue.getKey().getText());

            if (ignoredDeps.contains(crateName)) {
                continue;
            }

            String version = extractVersionFromTomlValue(keyValue.getValue());
            Dependency dp = new Dependency(CARGO, null, crateName, version);
            resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(keyValue);
        }
    }
}
