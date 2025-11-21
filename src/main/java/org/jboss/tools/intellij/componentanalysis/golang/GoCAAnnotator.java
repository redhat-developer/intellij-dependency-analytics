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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.componentanalysis.CAIntentionAction;
import org.jboss.tools.intellij.componentanalysis.CAUpdateManifestIntentionAction;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.tools.intellij.componentanalysis.CAUtil.TRUSTIFY_DA_IGNORE;
import static org.jboss.tools.intellij.componentanalysis.CAUtil.EXHORT_IGNORE;

public class GoCAAnnotator extends CAAnnotator {
    public static final Pattern REQUIRE_PATTERN = Pattern.compile("^\\s*([a-zA-Z0-9._/-]+)\\s+(v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*)(?:\\s*//.*)?$");
    public static final Pattern REPLACE_PATTERN = Pattern.compile("^\\s*([a-zA-Z0-9._/-]+)\\s+(?:(v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*)\\s+)?=>\\s*([a-zA-Z0-9._/:-]+|\\.\\.?/[a-zA-Z0-9._/-]*|/[a-zA-Z0-9._/-]*)(?:\\s+(v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*))?(?:\\s*//.*)?$");

    @Override
    protected String getInspectionShortName() {
        return GoCAInspection.SHORT_NAME;
    }

    @Override
    protected Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
        if (!"go.mod".equals(file.getName())) {
            return Collections.emptyMap();
        }

        Map<Dependency, List<PsiElement>> resultMap = new HashMap<>();
        String fileText = file.getText();
        String[] lines = fileText.split("\\n");

        boolean inRequireBlock = false;
        boolean inReplaceBlock = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith("require") && isBlockStatement(line)) {
                inRequireBlock = true;
                continue;
            }
            if (line.startsWith("replace") && isBlockStatement(line)) {
                inReplaceBlock = true;
                continue;
            }
            if (line.equals(")")) {
                inRequireBlock = false;
                inReplaceBlock = false;
                continue;
            }

            // Skip if line contains trustify-da-ignore or exhortignore
            if (line.contains(TRUSTIFY_DA_IGNORE) || line.contains(EXHORT_IGNORE)) {
                continue;
            }

            PsiElement lineElement = findElementAtLine(file, i);
            if (lineElement == null) continue;

            if (inRequireBlock) {
                Matcher requireMatcher = REQUIRE_PATTERN.matcher(line);
                if (requireMatcher.matches()) {
                    String modulePath = requireMatcher.group(1);
                    String version = requireMatcher.group(2);
                    Dependency dependency = createDependency(modulePath, version);
                    resultMap.computeIfAbsent(dependency, k -> new LinkedList<>()).add(lineElement);
                }
            } else if (inReplaceBlock) {
                Matcher replaceMatcher = REPLACE_PATTERN.matcher(line);
                if (replaceMatcher.matches()) {
                    String targetPath = replaceMatcher.group(3);
                    String version = replaceMatcher.group(4) != null ? replaceMatcher.group(4) : replaceMatcher.group(2);
                    // Only analyze if target is not a local path and has a version
                    if (version != null && !targetPath.startsWith("./") && !targetPath.startsWith("../") && !targetPath.startsWith("/")) {
                        Dependency dependency = createDependency(targetPath, version);
                        resultMap.computeIfAbsent(dependency, k -> new LinkedList<>()).add(lineElement);
                    }
                }
            } else {
                // Single line require/replace
                if (line.startsWith("require ")) {
                    String requireLine = line.substring(8).trim();
                    Matcher requireMatcher = REQUIRE_PATTERN.matcher(requireLine);
                    if (requireMatcher.matches()) {
                        String modulePath = requireMatcher.group(1);
                        String version = requireMatcher.group(2);
                        Dependency dependency = createDependency(modulePath, version);
                        resultMap.computeIfAbsent(dependency, k -> new LinkedList<>()).add(lineElement);
                    }
                } else if (line.startsWith("replace ")) {
                    String replaceLine = line.substring(8).trim();
                    Matcher replaceMatcher = REPLACE_PATTERN.matcher(replaceLine);
                    if (replaceMatcher.matches()) {
                        String targetPath = replaceMatcher.group(3);
                        String version = replaceMatcher.group(4) != null ? replaceMatcher.group(4) : replaceMatcher.group(2);
                        // Only analyze if target is not a local path and has a version
                        if (version != null && !targetPath.startsWith("./") && !targetPath.startsWith("../") && !targetPath.startsWith("/")) {
                            Dependency dependency = createDependency(targetPath, version);
                            resultMap.computeIfAbsent(dependency, k -> new LinkedList<>()).add(lineElement);
                        }
                    }
                }
            }
        }

        return resultMap;
    }

    @Override
    protected CAIntentionAction createQuickFix(PsiElement element, VulnerabilitySource source, DependencyReport report) {
        return new GoCAIntentionAction(element, source, report);
    }

    @Override
    protected CAUpdateManifestIntentionAction patchManifest(PsiElement element, DependencyReport report) {
        return null;
    }

    @Override
    protected boolean isQuickFixApplicable(PsiElement element) {
        return element != null && element.getContainingFile().getName().equals("go.mod");
    }

    private PsiElement findElementAtLine(PsiFile file, int lineNumber) {
        String[] lines = file.getText().split("\\n");
        if (lineNumber >= lines.length) return null;

        int lineStartOffset = 0;
        for (int i = 0; i < lineNumber; i++) {
            lineStartOffset += lines[i].length() + 1; // +1 for newline
        }

        String line = lines[lineNumber];

        // Find version in the line using regex to get precise position
        Matcher versionMatcher = Pattern.compile("(v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*)").matcher(line);
        if (versionMatcher.find()) {
            int versionStart = lineStartOffset + versionMatcher.start();
            int versionEnd = lineStartOffset + versionMatcher.end();

            // Find the PSI element that covers the version
            PsiElement versionElement = file.findElementAt(versionStart);
            if (versionElement != null) {
                // Make sure we get an element that covers the full version
                while (versionElement != null &&
                       versionElement.getTextRange().getEndOffset() < versionEnd) {
                    versionElement = versionElement.getParent();
                }
                return versionElement;
            }
        }

        // Fallback to middle of line if version not found
        if (lineStartOffset < file.getTextLength()) {
            return file.findElementAt(lineStartOffset + line.length() / 2);
        }
        return null;
    }

    private static Dependency createDependency(String modulePath, String version) {
        String name = modulePath;
        String namespace = null;

        int lastSlash = modulePath.lastIndexOf("/");
        if (lastSlash > 0) {
            namespace = modulePath.substring(0, lastSlash);
            name = modulePath.substring(lastSlash + 1);
        } else if (lastSlash == 0) {
            name = modulePath.substring(1);
        }

        return new Dependency("golang", namespace, name, version);
    }

    /**
     * Determines if a line represents a block statement (require/replace with opening parenthesis).
     * Only considers "(" that appears at the end of the statement, ignoring any comments.
     *
     * Examples:
     * - "require (" -> true
     * - "require ( // comment" -> true
     * - "require golang.org/x/net v1.0 // comment with (" -> false
     * - "replace (" -> true
     */
    private static boolean isBlockStatement(String line) {
        int commentIndex = line.indexOf("//");
        String statementPart = commentIndex >= 0 ? line.substring(0, commentIndex).trim() : line;
        return statementPart.endsWith("(");
    }
}