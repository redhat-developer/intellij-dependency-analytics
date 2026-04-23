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

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import io.github.guacsec.trustifyda.api.PackageRef;
import io.github.guacsec.trustifyda.api.v5.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.VulnerabilitySource;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Tests for PyprojectCAIntentionAction version update and availability.
 */
public class PyprojectCAIntentionActionTest extends BasePlatformTestCase {

    private static final String NEW_VERSION = "4.0.0";

    private DependencyReport createReportWithRecommendation() {
        DependencyReport report = new DependencyReport();
        report.setRecommendation(new PackageRef("pkg:pypi/test-package@" + NEW_VERSION));
        return report;
    }

    private VulnerabilitySource dummySource() {
        return new VulnerabilitySource("test-provider", "test-source");
    }

    /**
     * Testable subclass that exposes the protected getDependencies method.
     */
    private static class TestablePyprojectCAAnnotator extends PyprojectCAAnnotator {
        @Override
        public Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
            return super.getDependencies(file);
        }
    }

    private PsiElement findDependencyElement(PsiFile file, String name, String version) {
        TestablePyprojectCAAnnotator annotator = new TestablePyprojectCAAnnotator();
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);
        Dependency key = new Dependency("pypi", null, name, version);
        List<PsiElement> elements = deps.get(key);
        assertNotNull("Should find dependency: " + name, elements);
        assertFalse("Should have at least one element for: " + name, elements.isEmpty());
        return elements.get(0);
    }

    // ── PEP 621 version update tests ────────────────────────────────────────────

    /** Verifies that a simple PEP 621 pinned version is updated correctly. */
    @Test
    public void testUpdatePep621SimpleVersion() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                    "flask>=2.0",
                ]
                """;

        // Given a pyproject.toml with PEP 621 dependencies
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "anyio", "==3.6.2");

        // When applying the quick fix
        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        // Then the version should be updated and other deps unchanged
        String updatedText = file.getText();
        assertTrue("anyio version should be updated", updatedText.contains("\"anyio==" + NEW_VERSION + "\""));
        assertTrue("flask should remain unchanged", updatedText.contains("\"flask>=2.0\""));
    }

    /** Verifies that extras and environment markers are preserved during version update. */
    @Test
    public void testUpdatePep621PreservesExtrasAndMarkers() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "requests[security]>=2.25.0; sys_platform == 'win32'",
                ]
                """;

        // Given a dependency with extras and markers
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "requests", ">=2.25.0");

        // When applying the quick fix
        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        // Then extras and markers should be preserved
        String updatedText = file.getText();
        assertTrue("Should preserve extras and markers",
                updatedText.contains("requests[security]==" + NEW_VERSION + " ; sys_platform == 'win32'"));
    }

    // ── Poetry version update tests ────────────────────────────────────────────

    /** Verifies that a Poetry simple string version is updated correctly. */
    @Test
    public void testUpdatePoetrySimpleStringVersion() {
        String content = """
                [tool.poetry.dependencies]
                python = "^3.8"
                anyio = "^3.6.2"
                flask = ">=2.0"
                """;

        // Given a pyproject.toml with Poetry simple string dependencies
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "anyio", "^3.6.2");

        // When applying the quick fix
        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        // Then only the target version should be updated
        String updatedText = file.getText();
        assertTrue("anyio version should be updated",
                updatedText.contains("anyio = \"==" + NEW_VERSION + "\""));
        assertTrue("flask should remain unchanged",
                updatedText.contains("flask = \">=2.0\""));
        assertTrue("python should remain unchanged",
                updatedText.contains("python = \"^3.8\""));
    }

    /** Verifies that a Poetry inline-table version is updated correctly. */
    @Test
    public void testUpdatePoetryInlineTableVersion() {
        String content = """
                [tool.poetry.dependencies]
                python = "^3.8"
                requests = {version = "^2.28.0", optional = true}
                """;

        // Given a pyproject.toml with Poetry inline-table dependency
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "requests", "^2.28.0");

        // When applying the quick fix
        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        // Then the version inside the inline table should be updated
        String updatedText = file.getText();
        assertTrue("requests version should be updated in inline table",
                updatedText.contains("version = \"==" + NEW_VERSION + "\""));
        assertTrue("optional flag should remain unchanged",
                updatedText.contains("optional = true"));
    }

    // ── Edge case tests ─────────────────────────────────────────────────────────

    /** Verifies that a null version is a no-op. */
    @Test
    public void testUpdateWithNullVersionIsNoOp() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                ]
                """;

        // Given a dependency element
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "anyio", "==3.6.2");

        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        // When updating with null version
        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, null)
        );

        // Then the file should remain unchanged
        String updatedText = file.getText();
        assertTrue("anyio should remain unchanged when version is null",
                updatedText.contains("\"anyio==3.6.2\""));
    }

    // ── isAvailable tests ───────────────────────────────────────────────────────

    /** Verifies that the intention action is available for pyproject.toml PEP 621 dependencies. */
    @Test
    public void testIsAvailableForPyprojectToml() {
        String content = """
                [project]
                name = "my-app"
                dependencies = [
                    "anyio==3.6.2",
                ]
                """;

        // Given a pyproject.toml file
        PsiFile file = myFixture.configureByText("pyproject.toml", content);
        PsiElement element = findDependencyElement(file, "anyio", "==3.6.2");

        DependencyReport report = createReportWithRecommendation();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        // Then the action should be available
        assertTrue("Should be available for pyproject.toml",
                action.isAvailable(getProject(), null, file));
    }

    /** Verifies that the intention action is not available for non-pyproject.toml files. */
    @Test
    public void testIsNotAvailableForOtherTomlFiles() {
        String content = """
                [dependencies]
                serde = "1.0"
                """;

        // Given a non-pyproject.toml TOML file
        PsiFile file = myFixture.configureByText("Cargo.toml", content);

        DependencyReport report = createReportWithRecommendation();
        // Use a dummy element from the file since we can't use the pypi annotator on Cargo.toml
        PsiElement element = file.getFirstChild();
        PyprojectCAIntentionAction action = new PyprojectCAIntentionAction(element, dummySource(), report);

        // Then the action should not be available
        assertFalse("Should not be available for Cargo.toml",
                action.isAvailable(getProject(), null, file));
    }
}
