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

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Tests for PyprojectCAAnnotator dependency extraction from pyproject.toml files.
 */
public class PyprojectCAAnnotatorTest extends BasePlatformTestCase {

    /**
     * Testable subclass that exposes the protected getDependencies method.
     */
    private static class TestablePyprojectCAAnnotator extends PyprojectCAAnnotator {
        @Override
        public Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
            return super.getDependencies(file);
        }
    }

    // ── PEP 508 utility method tests ──────────────────────────────────────────

    /** Verifies that a simple package name is extracted from a PEP 508 string. */
    @Test
    public void testExtractPep508NameSimple() {
        assertEquals("anyio", PyprojectCAAnnotator.extractPep508Name("anyio==3.6.2"));
    }

    /** Verifies name extraction from a PEP 508 string with extras. */
    @Test
    public void testExtractPep508NameWithExtras() {
        assertEquals("requests", PyprojectCAAnnotator.extractPep508Name("requests[security]>=2.25.0"));
    }

    /** Verifies name extraction from a PEP 508 string with environment markers. */
    @Test
    public void testExtractPep508NameWithMarkers() {
        assertEquals("importlib-metadata", PyprojectCAAnnotator.extractPep508Name("importlib-metadata>=4.0; python_version<'3.8'"));
    }

    /** Verifies that a bare package name without version is extracted. */
    @Test
    public void testExtractPep508NameBare() {
        assertEquals("setuptools", PyprojectCAAnnotator.extractPep508Name("setuptools"));
    }

    /** Verifies null is returned for null input. */
    @Test
    public void testExtractPep508NameNull() {
        assertNull(PyprojectCAAnnotator.extractPep508Name(null));
    }

    /** Verifies null is returned for empty string. */
    @Test
    public void testExtractPep508NameEmpty() {
        assertNull(PyprojectCAAnnotator.extractPep508Name(""));
    }

    /** Verifies that a pinned version specifier is extracted. */
    @Test
    public void testExtractPep508VersionPinned() {
        assertEquals("==3.6.2", PyprojectCAAnnotator.extractPep508Version("anyio==3.6.2"));
    }

    /** Verifies that a range version specifier is extracted. */
    @Test
    public void testExtractPep508VersionRange() {
        assertEquals(">=2.25.0", PyprojectCAAnnotator.extractPep508Version("requests>=2.25.0"));
    }

    /** Verifies that compound version specifiers are joined with commas. */
    @Test
    public void testExtractPep508VersionCompound() {
        assertEquals(">=1.0,<2.0", PyprojectCAAnnotator.extractPep508Version("flask>=1.0,<2.0"));
    }

    /** Verifies that version extraction ignores environment markers after semicolon. */
    @Test
    public void testExtractPep508VersionWithMarkers() {
        assertEquals(">=4.0", PyprojectCAAnnotator.extractPep508Version("importlib-metadata>=4.0; python_version<'3.8'"));
    }

    /** Verifies that version extraction ignores extras brackets. */
    @Test
    public void testExtractPep508VersionWithExtras() {
        assertEquals(">=2.25.0", PyprojectCAAnnotator.extractPep508Version("requests[security]>=2.25.0"));
    }

    /** Verifies null is returned when no version specifier is present. */
    @Test
    public void testExtractPep508VersionNone() {
        assertNull(PyprojectCAAnnotator.extractPep508Version("setuptools"));
    }

    /** Verifies that extras brackets are extracted from a PEP 508 string. */
    @Test
    public void testExtractPep508Extras() {
        assertEquals("[security]", PyprojectCAAnnotator.extractPep508Extras("requests[security]>=2.25.0"));
    }

    /** Verifies null is returned when no extras are present. */
    @Test
    public void testExtractPep508ExtrasNone() {
        assertNull(PyprojectCAAnnotator.extractPep508Extras("anyio==3.6.2"));
    }

    /** Verifies that environment markers are extracted from a PEP 508 string. */
    @Test
    public void testExtractPep508Markers() {
        assertEquals("; python_version<'3.8'", PyprojectCAAnnotator.extractPep508Markers("importlib-metadata>=4.0; python_version<'3.8'"));
    }

    /** Verifies null is returned when no markers are present. */
    @Test
    public void testExtractPep508MarkersNone() {
        assertNull(PyprojectCAAnnotator.extractPep508Markers("anyio==3.6.2"));
    }

    /** Verifies that double-quoted strings are unquoted correctly. */
    @Test
    public void testUnquoteDoubleQuotes() {
        assertEquals("anyio==3.6.2", PyprojectCAAnnotator.unquote("\"anyio==3.6.2\""));
    }

    /** Verifies that single-quoted strings are unquoted correctly. */
    @Test
    public void testUnquoteSingleQuotes() {
        assertEquals("anyio==3.6.2", PyprojectCAAnnotator.unquote("'anyio==3.6.2'"));
    }

    /** Verifies that unquoted strings are returned as-is. */
    @Test
    public void testUnquoteNoQuotes() {
        assertEquals("anyio", PyprojectCAAnnotator.unquote("anyio"));
    }

    // ── getDependencies tests (PEP 621) ────────────────────────────────────────

    /** Verifies that PEP 621 [project.dependencies] array is parsed correctly. */
    @Test
    public void testPep621Dependencies() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                    "flask>=2.0",
                    "requests[security]>=2.25.0; sys_platform == 'win32'",
                ]
                """;

        // Given a pyproject.toml with PEP 621 dependencies
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then all 3 dependencies should be found
        assertEquals("Should find 3 PEP 621 dependencies", 3, deps.size());
        assertTrue("Should contain anyio", containsDependency(deps, "anyio", "==3.6.2"));
        assertTrue("Should contain flask", containsDependency(deps, "flask", ">=2.0"));
        assertTrue("Should contain requests", containsDependency(deps, "requests", ">=2.25.0"));
    }

    /** Verifies that PEP 621 [project.optional-dependencies] groups are parsed. */
    @Test
    public void testPep621OptionalDependencies() {
        String content = """
                [project.optional-dependencies]
                dev = [
                    "pytest>=7.0",
                    "black>=23.0",
                ]
                security = [
                    "certifi>=2023.7",
                ]
                """;

        // Given a pyproject.toml with optional dependency groups
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then all 3 optional dependencies should be found across groups
        assertEquals("Should find 3 optional dependencies across groups", 3, deps.size());
        assertTrue("Should contain pytest", containsDependency(deps, "pytest", ">=7.0"));
        assertTrue("Should contain black", containsDependency(deps, "black", ">=23.0"));
        assertTrue("Should contain certifi", containsDependency(deps, "certifi", ">=2023.7"));
    }

    // ── getDependencies tests (Poetry) ─────────────────────────────────────────

    /** Verifies that Poetry simple string dependencies are parsed correctly. */
    @Test
    public void testPoetrySimpleStringDependencies() {
        String content = """
                [tool.poetry.dependencies]
                python = "^3.8"
                anyio = "^3.6.2"
                flask = ">=2.0"
                """;

        // Given a pyproject.toml with Poetry simple string dependencies
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then python should be skipped and the other 2 should be found
        assertEquals("Should find 2 dependencies (python is skipped)", 2, deps.size());
        assertTrue("Should contain anyio", containsDependency(deps, "anyio", "^3.6.2"));
        assertTrue("Should contain flask", containsDependency(deps, "flask", ">=2.0"));
    }

    /** Verifies that Poetry inline-table dependencies are parsed correctly. */
    @Test
    public void testPoetryInlineTableDependencies() {
        String content = """
                [tool.poetry.dependencies]
                python = "^3.8"
                requests = {version = "^2.28.0", optional = true}
                uvicorn = {version = ">=0.18", extras = ["standard"]}
                """;

        // Given a pyproject.toml with Poetry inline-table dependencies
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then both inline-table dependencies should be found with their versions
        assertEquals("Should find 2 dependencies", 2, deps.size());
        assertTrue("Should contain requests", containsDependency(deps, "requests", "^2.28.0"));
        assertTrue("Should contain uvicorn", containsDependency(deps, "uvicorn", ">=0.18"));
    }

    // ── Mixed format tests ─────────────────────────────────────────────────────

    /** Verifies that PEP 621 and Poetry dependencies in the same file are parsed independently. */
    @Test
    public void testMixedPep621AndPoetry() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                ]

                [tool.poetry.dependencies]
                flask = "^2.0"
                """;

        // Given a pyproject.toml with both PEP 621 and Poetry sections
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then dependencies from both formats should be found
        assertEquals("Should find 2 dependencies from both formats", 2, deps.size());
        assertTrue("Should contain anyio from PEP 621", containsDependency(deps, "anyio", "==3.6.2"));
        assertTrue("Should contain flask from Poetry", containsDependency(deps, "flask", "^2.0"));
    }

    // ── File name gating tests ──────────────────────────────────────────────────

    /** Verifies that non-pyproject.toml TOML files are ignored. */
    @Test
    public void testNonPyprojectTomlFileReturnsEmpty() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                ]
                """;

        // Given a TOML file that is not named pyproject.toml
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("Cargo.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then no dependencies should be found
        assertTrue("Should return empty map for non-pyproject.toml", deps.isEmpty());
    }

    /** Verifies that a pyproject.toml with no dependency sections returns an empty map. */
    @Test
    public void testPyprojectWithNoDependencies() {
        String content = """
                [project]
                name = "my-app"
                version = "1.0.0"
                """;

        // Given a pyproject.toml with no dependency arrays or tables
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        PsiFile file = myFixture.configureByText("pyproject.toml", content);

        // When extracting dependencies
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);

        // Then no dependencies should be found
        assertTrue("Should return empty map when no dependency sections exist", deps.isEmpty());
    }

    // ── SaAction / SaUtils recognition tests ────────────────────────────────────

    /** Verifies that SaAction.supportedManifestFiles contains pyproject.toml. */
    @Test
    public void testSaActionRecognizesPyprojectToml() throws Exception {
        // Given the SaAction class
        Field field = org.jboss.tools.intellij.stackanalysis.SaAction.class
                .getDeclaredField("supportedManifestFiles");
        field.setAccessible(true);

        // When reading the supported manifest files list
        @SuppressWarnings("unchecked")
        List<String> manifests = (List<String>) field.get(null);

        // Then pyproject.toml should be included
        assertTrue("SaAction should recognize pyproject.toml", manifests.contains("pyproject.toml"));
    }

    /** Verifies that SaUtils.determinePackageManagerName maps pyproject.toml to python. */
    @Test
    public void testSaUtilsMapsPyprojectTomlToPython() throws Exception {
        // Given the SaUtils class
        org.jboss.tools.intellij.stackanalysis.SaUtils saUtils =
                new org.jboss.tools.intellij.stackanalysis.SaUtils();
        Method method = org.jboss.tools.intellij.stackanalysis.SaUtils.class
                .getDeclaredMethod("determinePackageManagerName", String.class);
        method.setAccessible(true);

        // When determining the package manager for pyproject.toml
        String result = (String) method.invoke(saUtils, "pyproject.toml");

        // Then it should map to python
        assertEquals("pyproject.toml should map to python", "python", result);
    }

    // ── Helper methods ──────────────────────────────────────────────────────────

    private boolean containsDependency(Map<Dependency, List<PsiElement>> dependencies,
                                       String expectedName, String expectedVersion) {
        return dependencies.keySet().stream()
                .anyMatch(dep -> "pypi".equals(dep.getType())
                        && expectedName.equals(dep.getName())
                        && expectedVersion.equals(dep.getVersion()));
    }
}
