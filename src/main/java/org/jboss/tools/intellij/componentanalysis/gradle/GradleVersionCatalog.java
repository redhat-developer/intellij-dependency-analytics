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

package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.toml.lang.psi.TomlInlineTable;
import org.toml.lang.psi.TomlKey;
import org.toml.lang.psi.TomlKeySegment;
import org.toml.lang.psi.TomlKeyValue;
import org.toml.lang.psi.TomlLiteral;
import org.toml.lang.psi.TomlTable;
import org.toml.lang.psi.TomlValue;

import java.util.List;

/**
 * Resolves Gradle version-catalog accessors (e.g. {@code libs.commons.text} used in a
 * {@code build.gradle.kts}) to concrete Maven coordinates declared in the default
 * {@code gradle/libs.versions.toml} catalog.
 *
 * <p>Only the default {@code libs} catalog is handled; custom-named catalogs are uncommon
 * and out of scope.
 */
public final class GradleVersionCatalog {

    /**
     * A resolved catalog library.
     *
     * @param versionElement the TOML literal holding the version (either a {@code "g:a:v"}
     *                        coordinate string or a bare version string). May be {@code null}
     *                        when the version cannot be located, in which case no quick-fix
     *                        version bump is possible.
     */
    public record Entry(String group, String artifact, String version,
                        @Nullable TomlValue versionElement, PsiFile tomlFile) {}

    private GradleVersionCatalog() {}

    /**
     * @param segments accessor path after the {@code libs} root, e.g. {@code [commons, text]}
     *                 for {@code libs.commons.text}
     */
    public static @Nullable Entry resolve(PsiFile ktsFile, List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }
        // versions/bundles/plugins accessors are not libraries
        String first = segments.get(0);
        if (first.equals("versions") || first.equals("bundles") || first.equals("plugins")) {
            return null;
        }

        PsiFile toml = findCatalog(ktsFile);
        if (toml == null) {
            return null;
        }

        String wantedAlias = normalizeAlias(String.join(".", segments));
        TomlTable libraries = findTable(toml, "libraries");
        if (libraries != null) {
            for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(libraries, TomlKeyValue.class)) {
                if (!wantedAlias.equals(normalizeAlias(unquote(kv.getKey().getText())))) {
                    continue;
                }
                return parseLibrary(toml, kv.getValue());
            }
        }
        // Sub-table syntax: [libraries.commons-text] with module/version.ref as its own table body.
        TomlTable subTable = findSubTable(toml, wantedAlias);
        if (subTable != null) {
            return parseKeyValues(toml, PsiTreeUtil.getChildrenOfTypeAsList(subTable, TomlKeyValue.class));
        }
        return null;
    }

    /** Finds a top-level {@code [libraries.<alias>]} table matching the wanted (normalized) alias. */
    private static @Nullable TomlTable findSubTable(PsiFile toml, String wantedAlias) {
        for (TomlTable table : PsiTreeUtil.getChildrenOfTypeAsList(toml, TomlTable.class)) {
            TomlKey key = table.getHeader().getKey();
            List<TomlKeySegment> segs = key == null ? List.of() : key.getSegments();
            if (segs.size() < 2 || !"libraries".equals(segs.get(0).getName())) {
                continue;
            }
            StringBuilder alias = new StringBuilder();
            for (int i = 1; i < segs.size(); i++) {
                if (i > 1) {
                    alias.append('.');
                }
                alias.append(segs.get(i).getName());
            }
            if (wantedAlias.equals(normalizeAlias(unquote(alias.toString())))) {
                return table;
            }
        }
        return null;
    }

    private static @Nullable Entry parseLibrary(PsiFile toml, @Nullable TomlValue value) {
        if (value instanceof TomlLiteral literal) {
            // commons-text = "org.apache.commons:commons-text:1.9"
            String[] parts = unquote(literal.getText()).split(":", -1);
            if (parts.length == 3 && !parts[0].isBlank() && !parts[1].isBlank() && !parts[2].isBlank()) {
                return new Entry(parts[0], parts[1], parts[2], literal, toml);
            }
            return null;
        }
        if (!(value instanceof TomlInlineTable table)) {
            return null;
        }
        return parseKeyValues(toml, PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class));
    }

    /** Parses module/group/name/version key-values shared by inline tables and [libraries.x] sub-tables. */
    private static @Nullable Entry parseKeyValues(PsiFile toml, List<TomlKeyValue> keyValues) {
        String group = null, name = null, version = null;
        TomlValue versionElement = null;
        for (TomlKeyValue kv : keyValues) {
            List<TomlKeySegment> segs = kv.getKey().getSegments();
            String key = segs.isEmpty() ? "" : segs.get(0).getName();
            TomlValue v = kv.getValue();
            switch (key == null ? "" : key) {
                case "module" -> {
                    String[] gm = unquote(text(v)).split(":", -1);
                    if (gm.length == 2) {
                        group = gm[0];
                        name = gm[1];
                    }
                }
                case "group" -> group = unquote(text(v));
                case "name" -> name = unquote(text(v));
                case "version" -> {
                    // version = "1.9"  |  version.ref = "commonsText"  |  version = { ref = "commonsText" }
                    String ref = null;
                    if (segs.size() >= 2 && "ref".equals(segs.get(1).getName())) {
                        ref = unquote(text(v));
                    } else if (v instanceof TomlInlineTable versionTable) {
                        ref = inlineValue(versionTable, "ref");
                    } else if (v instanceof TomlLiteral literal) {
                        version = unquote(literal.getText());
                        versionElement = literal;
                    }
                    if (ref != null) {
                        TomlLiteral resolved = findVersionRef(toml, ref);
                        if (resolved != null) {
                            version = unquote(resolved.getText());
                            versionElement = resolved;
                        }
                    }
                }
                default -> { /* ignore classifier, ext, etc. */ }
            }
        }

        if (group == null || name == null || version == null
                || group.isBlank() || name.isBlank() || version.isBlank()) {
            return null;
        }
        return new Entry(group, name, version, versionElement, toml);
    }

    private static @Nullable TomlLiteral findVersionRef(PsiFile toml, String ref) {
        TomlTable versions = findTable(toml, "versions");
        if (versions == null) {
            return null;
        }
        String wanted = normalizeAlias(ref);
        for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(versions, TomlKeyValue.class)) {
            if (wanted.equals(normalizeAlias(unquote(kv.getKey().getText())))
                    && kv.getValue() instanceof TomlLiteral literal) {
                return literal;
            }
        }
        return null;
    }

    private static @Nullable String inlineValue(TomlInlineTable table, String key) {
        for (TomlKeyValue kv : PsiTreeUtil.getChildrenOfTypeAsList(table, TomlKeyValue.class)) {
            if (key.equals(unquote(kv.getKey().getText())) && kv.getValue() instanceof TomlLiteral literal) {
                return unquote(literal.getText());
            }
        }
        return null;
    }

    private static @Nullable TomlTable findTable(PsiFile toml, String name) {
        for (TomlTable table : PsiTreeUtil.getChildrenOfTypeAsList(toml, TomlTable.class)) {
            TomlKey key = table.getHeader().getKey();
            if (key != null && key.getSegments().size() == 1
                    && name.equals(key.getSegments().get(0).getName())) {
                return table;
            }
        }
        return null;
    }

    private static @Nullable PsiFile findCatalog(PsiFile ktsFile) {
        VirtualFile vf = ktsFile.getVirtualFile();
        if (vf == null) {
            return null;
        }
        for (VirtualFile dir = vf.getParent(); dir != null; dir = dir.getParent()) {
            VirtualFile gradleDir = dir.findChild("gradle");
            if (gradleDir != null) {
                VirtualFile catalog = gradleDir.findChild("libs.versions.toml");
                if (catalog != null) {
                    return PsiManager.getInstance(ktsFile.getProject()).findFile(catalog);
                }
            }
        }
        return null;
    }

    private static String text(@Nullable TomlValue value) {
        return value == null ? "" : value.getText();
    }

    // Gradle treats '-', '_' and '.' as equivalent separators in catalog aliases and accessors.
    private static String normalizeAlias(String alias) {
        return alias.replace('-', '.').replace('_', '.');
    }

    private static String unquote(String text) {
        if (text.length() >= 2
                && ((text.startsWith("\"") && text.endsWith("\""))
                || (text.startsWith("'") && text.endsWith("'")))) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }
}
