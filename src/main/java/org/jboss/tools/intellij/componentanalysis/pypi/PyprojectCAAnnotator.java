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
import org.jboss.tools.intellij.componentanalysis.LicenseUpdateIntentionAction;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.psi.TomlArray;
import org.toml.lang.psi.TomlKeySegment;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlTableHeader;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.TRUSTIFY_DA_IGNORE;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;

public class PyprojectCAAnnotator extends CAAnnotator {

    private static final String PYPROJECT_TOML = "pyproject.toml";
    private static final String PROJECT = "project";
    private static final String DEPENDENCIES = "dependencies";
    private static final String OPTIONAL_DEPENDENCIES = "optional-dependencies";

    // PEP 508: name followed by optional extras, then optional version specifier
    // Matches: "flask", "flask>=2.0", "requests[security]~=2.28", "numpy ==1.20.*"
    private static final Pattern DEP_PATTERN = Pattern.compile(
            "^([A-Za-z0-9]([A-Za-z0-9._-]*[A-Za-z0-9])?)" +  // package name
            "(\\[.*?])?" +                                       // optional extras
            "\\s*(.*)$"                                          // version specifier (rest of string)
    );

    @Override
    protected String getInspectionShortName() {
        return PyprojectCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if (!PYPROJECT_TOML.equals(file.getName())) {
            return Collections.emptyMap();
        }

        Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
        Set<String> ignoredDeps = getIgnoredDependencies(file);

        List<TomlTable> tables = PsiTreeUtil.getChildrenOfTypeAsList(file, TomlTable.class);
        for (TomlTable table : tables) {
            TomlTableHeader header = table.getHeader();
            TomlKey key = header.getKey();
            if (key == null) {
                continue;
            }

            List<TomlKeySegment> segments = key.getSegments();

            // [project] table — look for "dependencies" key
            if (segments.size() == 1 && PROJECT.equals(segments.get(0).getName())) {
                for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class)) {
                    if (DEPENDENCIES.equals(kv.getKey().getText())) {
                        parseDependencyArray(kv.getValue(), ignoredDeps, resultMap);
                    }
                }
            }

            // [project.optional-dependencies] table — all values are dependency arrays
            if (segments.size() == 2
                    && PROJECT.equals(segments.get(0).getName())
                    && OPTIONAL_DEPENDENCIES.equals(segments.get(1).getName())) {
                for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class)) {
                    parseDependencyArray(kv.getValue(), ignoredDeps, resultMap);
                }
            }
        }

        return resultMap;
    }

    private void parseDependencyArray(TomlValue value, Set<String> ignoredDeps,
                                       Map<Dependency, List<PsiElement>> resultMap) {
        if (!(value instanceof TomlArray array)) {
            return;
        }

        for (TomlValue element : array.getElements()) {
            if (!(element instanceof TomlLiteral literal)) {
                continue;
            }

            String text = literal.getText();
            if (text.length() < 2 || !text.startsWith("\"") || !text.endsWith("\"")) {
                continue;
            }

            String depString = text.substring(1, text.length() - 1);
            String name = parseName(depString);
            if (name == null || ignoredDeps.contains(name)) {
                continue;
            }

            String version = parseVersion(depString);
            Dependency dp = new Dependency("pypi", null, name, version);
            resultMap.computeIfAbsent(dp, k -> new LinkedList<>()).add(literal);
        }
    }

    static String parseName(String depString) {
        Matcher m = DEP_PATTERN.matcher(depString.trim());
        if (m.matches()) {
            return m.group(1).toLowerCase();
        }
        return null;
    }

    private static String parseVersion(String depString) {
        Matcher m = DEP_PATTERN.matcher(depString.trim());
        if (m.matches()) {
            String versionSpec = m.group(4);
            if (versionSpec != null && !versionSpec.isBlank()) {
                return versionSpec.trim();
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
                TomlLiteral literal = findLiteralOnSameLine(file, comment);
                if (literal != null) {
                    String text = literal.getText();
                    if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
                        String name = parseName(text.substring(1, text.length() - 1));
                        if (name != null) {
                            ignoredDeps.add(name);
                        }
                    }
                }
            }
        }
        return ignoredDeps;
    }

    private TomlLiteral findLiteralOnSameLine(PsiFile file, PsiComment comment) {
        Document document = PsiDocumentManager.getInstance(comment.getProject()).getDocument(file);
        if (document == null) {
            return null;
        }
        int commentLine = document.getLineNumber(comment.getTextRange().getStartOffset());
        Collection<TomlLiteral> literals = PsiTreeUtil.collectElementsOfType(file, TomlLiteral.class);
        for (TomlLiteral literal : literals) {
            int literalLine = document.getLineNumber(literal.getTextRange().getStartOffset());
            if (commentLine == literalLine) {
                return literal;
            }
        }
        return null;
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
        if (!(element instanceof TomlLiteral literal)) {
            return false;
        }
        String text = literal.getText();
        if (text.length() < 2 || !text.startsWith("\"") || !text.endsWith("\"")) {
            return false;
        }
        String depString = text.substring(1, text.length() - 1);
        String version = parseVersion(depString);
        return version != null;
    }

    @Override
    protected @Nullable PsiElement getLicenseFieldPsiElement(PsiFile file) {
        List<TomlTable> tables = PsiTreeUtil.getChildrenOfTypeAsList(file, TomlTable.class);
        for (TomlTable table : tables) {
            TomlTableHeader header = table.getHeader();
            TomlKey key = header.getKey();
            if (key == null) {
                continue;
            }
            List<TomlKeySegment> segments = key.getSegments();
            if (segments.size() == 1 && PROJECT.equals(segments.get(0).getName())) {
                for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class)) {
                    if ("license".equals(kv.getKey().getText())) {
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
}
