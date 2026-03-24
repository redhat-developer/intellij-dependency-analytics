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

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.toml.lang.psi.TomlArray;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.TRUSTIFY_DA_IGNORE;

public class PyprojectCAAnnotator extends CAAnnotator {

    private static final String PYPI = "pypi";
    private static final String PYPROJECT_TOML = "pyproject.toml";

    /** Matches PEP 508 dependency name: everything before the first version specifier or extra marker. */
    private static final Pattern PEP508_NAME_PATTERN = Pattern.compile("^([A-Za-z0-9]([A-Za-z0-9._-]*[A-Za-z0-9])?)");

    /** Matches version specifiers like ==3.6.2, >=2.0, ~=1.4, etc. */
    private static final Pattern PEP508_VERSION_PATTERN = Pattern.compile("([<>=!~]+\\s*[^,;\\s]+)");

    @Override
    protected String getInspectionShortName() {
        return PyprojectCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if (!"pyproject.toml".equals(file.getName())) {
            return Map.of();
        }

        Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
        Set<String> ignoredDeps = getIgnoredDependencies(file);

        List<TomlTable> tables = PsiTreeUtil.getChildrenOfTypeAsList(file, TomlTable.class);
        for (TomlTable table : tables) {
            TomlTableHeader header = table.getHeader();
            if (header == null) {
                continue;
            }
            String tablePath = getTablePath(header);
            if ("project".equals(tablePath)) {
                parsePep621Dependencies(table, ignoredDeps, resultMap);
            } else if ("project.optional-dependencies".equals(tablePath)) {
                parseOptionalDependencies(table, ignoredDeps, resultMap);
            } else if ("tool.poetry.dependencies".equals(tablePath)) {
                parsePoetryDependencies(table, ignoredDeps, resultMap);
            }
        }

        return resultMap;
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new PyprojectCAIntentionAction(element, source, report);
    }

    @Override
    protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport report) {
        return null;
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        if (element.getContainingFile() == null || !PYPROJECT_TOML.equals(element.getContainingFile().getName())) {
            return false;
        }
        // Poetry: key-value pair like anyio = "^3.6.2"
        if (element instanceof TomlKeyValue) {
            return true;
        }
        // PEP 621: string literal in dependencies array like "anyio==3.6.2"
        return element instanceof TomlLiteral;
    }

    private void parsePep621Dependencies(TomlTable projectTable, Set<String> ignoredDeps,
                                          Map<Dependency, List<PsiElement>> resultMap) {
        List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(projectTable, TomlKeyValue.class);
        for (TomlKeyValue kv : keyValues) {
            if (!"dependencies".equals(kv.getKey().getText())) {
                continue;
            }
            TomlValue value = kv.getValue();
            if (!(value instanceof TomlArray array)) {
                continue;
            }
            for (TomlValue element : array.getElements()) {
                if (!(element instanceof TomlLiteral literal)) {
                    continue;
                }
                String depString = unquote(literal.getText());
                String name = extractPep508Name(depString);
                if (name == null || ignoredDeps.contains(name.toLowerCase())) {
                    continue;
                }
                String version = extractPep508Version(depString);
                Dependency dp = new Dependency(PYPI, null, name.toLowerCase(), version);
                resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(literal);
            }
        }
    }

    /**
     * Parses PEP 621 [project.optional-dependencies] table.
     * Format: dev = ["pytest>=7.0", "black"], security = ["certifi>=2023.7"]
     */
    private void parseOptionalDependencies(TomlTable optDepsTable, Set<String> ignoredDeps,
                                            Map<Dependency, List<PsiElement>> resultMap) {
        List<TomlKeyValue> groups = PsiTreeUtil.getChildrenOfTypeAsList(optDepsTable, TomlKeyValue.class);
        for (TomlKeyValue group : groups) {
            TomlValue value = group.getValue();
            if (!(value instanceof TomlArray array)) {
                continue;
            }
            for (TomlValue element : array.getElements()) {
                if (!(element instanceof TomlLiteral literal)) {
                    continue;
                }
                String depString = unquote(literal.getText());
                String name = extractPep508Name(depString);
                if (name == null || ignoredDeps.contains(name.toLowerCase())) {
                    continue;
                }
                String version = extractPep508Version(depString);
                Dependency dp = new Dependency(PYPI, null, name.toLowerCase(), version);
                resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(literal);
            }
        }
    }

    /**
     * Parses Poetry [tool.poetry.dependencies] table.
     * Format: anyio = "^3.6.2" or anyio = {version = "^3.6.2", optional = true}
     */
    private void parsePoetryDependencies(TomlTable poetryTable, Set<String> ignoredDeps,
                                          Map<Dependency, List<PsiElement>> resultMap) {
        List<TomlKeyValue> keyValues = PsiTreeUtil.getChildrenOfTypeAsList(poetryTable, TomlKeyValue.class);
        for (TomlKeyValue kv : keyValues) {
            String name = normalizeKeyName(kv.getKey().getText());
            if ("python".equalsIgnoreCase(name) || ignoredDeps.contains(name.toLowerCase())) {
                continue;
            }
            String version = extractPoetryVersion(kv.getValue());
            Dependency dp = new Dependency(PYPI, null, name.toLowerCase(), version);
            resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(kv);
        }
    }

    private String extractPoetryVersion(TomlValue value) {
        if (value instanceof TomlLiteral literal) {
            return unquote(literal.getText());
        }
        if (value instanceof TomlInlineTable inlineTable) {
            for (TomlKeyValue entry : PsiTreeUtil.getChildrenOfTypeAsList(inlineTable, TomlKeyValue.class)) {
                if ("version".equals(entry.getKey().getText()) && entry.getValue() instanceof TomlLiteral) {
                    return unquote(((TomlLiteral) entry.getValue()).getText());
                }
            }
        }
        return null;
    }

    private Set<String> getIgnoredDependencies(PsiFile file) {
        Set<String> ignoredDeps = new HashSet<>();
        Collection<PsiComment> comments = PsiTreeUtil.collectElementsOfType(file, PsiComment.class);
        for (PsiComment comment : comments) {
            String commentText = comment.getText();
            if (commentText.contains(TRUSTIFY_DA_IGNORE) || commentText.contains(EXHORT_IGNORE)) {
                String dependencyName = findAssociatedDependency(file, comment);
                if (dependencyName != null) {
                    ignoredDeps.add(dependencyName.toLowerCase());
                }
            }
        }
        return ignoredDeps;
    }

    private String findAssociatedDependency(PsiFile file, PsiComment comment) {
        // Check if comment is on same line as a TomlKeyValue
        TomlKeyValue keyValue = findKeyValueOnSameLine(file, comment);
        if (keyValue != null) {
            String keyName = keyValue.getKey().getText();
            // If in [tool.poetry.dependencies], the key IS the dependency name
            if (isInPoetryDependencies(keyValue)) {
                return normalizeKeyName(keyName);
            }
            // If key is "dependencies" in [project], check if on same line as an array element
            // For PEP 621 array entries, the ignore comment is typically on the same line as the string literal
        }

        // Check array elements — PEP 621 format where comment is on the same line as a literal in the array
        TomlLiteral literal = findLiteralOnSameLine(file, comment);
        if (literal != null) {
            String text = unquote(literal.getText());
            String name = extractPep508Name(text);
            if (name != null) {
                return name;
            }
        }

        return null;
    }

    private boolean isInPoetryDependencies(TomlKeyValue keyValue) {
        TomlTable parentTable = PsiTreeUtil.getParentOfType(keyValue, TomlTable.class);
        if (parentTable != null) {
            String tablePath = getTablePath(parentTable.getHeader());
            return "tool.poetry.dependencies".equals(tablePath);
        }
        return false;
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

    private TomlLiteral findLiteralOnSameLine(PsiFile file, PsiComment comment) {
        int commentLine = getLineNumber(file, comment);
        Collection<TomlLiteral> literals = PsiTreeUtil.collectElementsOfType(file, TomlLiteral.class);
        for (TomlLiteral literal : literals) {
            int literalLine = getLineNumber(file, literal);
            if (commentLine == literalLine) {
                return literal;
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

    private String getTablePath(TomlTableHeader header) {
        TomlKey key = header.getKey();
        if (key == null) {
            return "";
        }
        List<TomlKeySegment> segments = key.getSegments();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) {
                sb.append(".");
            }
            sb.append(segments.get(i).getName());
        }
        return sb.toString();
    }

    private String normalizeKeyName(String keyText) {
        if ((keyText.startsWith("\"") && keyText.endsWith("\"")) ||
                (keyText.startsWith("'") && keyText.endsWith("'"))) {
            if (keyText.length() > 2) {
                return keyText.substring(1, keyText.length() - 1);
            }
        }
        return keyText;
    }

    static String unquote(String text) {
        if (text != null && text.length() >= 2) {
            if ((text.startsWith("\"") && text.endsWith("\"")) ||
                    (text.startsWith("'") && text.endsWith("'"))) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }

    static String extractPep508Name(String depString) {
        if (depString == null || depString.isEmpty()) {
            return null;
        }
        Matcher matcher = PEP508_NAME_PATTERN.matcher(depString.trim());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    static String extractPep508Version(String depString) {
        if (depString == null || depString.isEmpty()) {
            return null;
        }
        // Remove the name part first
        Matcher nameMatcher = PEP508_NAME_PATTERN.matcher(depString.trim());
        if (!nameMatcher.find()) {
            return null;
        }
        String afterName = depString.substring(nameMatcher.end()).trim();
        // Remove extras like [security]
        if (afterName.startsWith("[")) {
            int closeBracket = afterName.indexOf(']');
            if (closeBracket >= 0) {
                afterName = afterName.substring(closeBracket + 1).trim();
            }
        }
        if (afterName.isEmpty()) {
            return null;
        }
        // Collect all version specifiers
        Matcher versionMatcher = PEP508_VERSION_PATTERN.matcher(afterName);
        StringBuilder versionSpec = new StringBuilder();
        while (versionMatcher.find()) {
            if (versionSpec.length() > 0) {
                versionSpec.append(",");
            }
            versionSpec.append(versionMatcher.group(1).trim());
        }
        return versionSpec.length() > 0 ? versionSpec.toString() : null;
    }
}
