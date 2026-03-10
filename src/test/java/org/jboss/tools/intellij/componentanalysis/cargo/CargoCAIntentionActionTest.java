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
 * Tests for CargoCAIntentionAction version update functionality.
 */
public class CargoCAIntentionActionTest extends BasePlatformTestCase {

    private static final String NEW_VERSION = "2.0.0";

    private DependencyReport createReportWithRecommendation() {
        DependencyReport report = new DependencyReport();
        report.setRecommendation(new PackageRef("pkg:cargo/test-crate@" + CargoCAIntentionActionTest.NEW_VERSION));
        return report;
    }

    private VulnerabilitySource dummySource() {
        return new VulnerabilitySource("test-provider", "test-source");
    }

    private PsiElement findDependencyElement(PsiFile file, String name, String version) {
        CargoCAAnnotator annotator = new CargoCAAnnotator();
        Map<Dependency, List<PsiElement>> deps = annotator.getDependencies(file);
        Dependency key = new Dependency("cargo", null, name, version);
        List<PsiElement> elements = deps.get(key);
        assertNotNull("Should find dependency: " + name, elements);
        assertFalse("Should have at least one element for: " + name, elements.isEmpty());
        return elements.get(0);
    }

    /**
     * Test updating a simple string version: serde = "1.0.150"
     */
    @Test
    public void testUpdateSimpleStringVersion() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                serde = "1.0.150"
                regex = "1.7.0"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement serdeElement = findDependencyElement(file, "serde", "1.0.150");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(serdeElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("serde version should be updated to " + NEW_VERSION,
                updatedText.contains("serde = \"" + NEW_VERSION + "\""));
        assertTrue("regex should remain unchanged",
                updatedText.contains("regex = \"1.7.0\""));
    }

    /**
     * Test updating a complex object format version: tokio = { version = "1.0", features = ["full"] }
     */
    @Test
    public void testUpdateComplexObjectVersion() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                tokio = { version = "1.0", features = ["full"] }
                serde = "1.0.150"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement tokioElement = findDependencyElement(file, "tokio", "1.0");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(tokioElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("tokio version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("tokio features should remain unchanged",
                updatedText.contains("features = [\"full\"]"));
        assertTrue("serde should remain unchanged",
                updatedText.contains("serde = \"1.0.150\""));
    }

    /**
     * Test updating a standard table format version:
     * [dependencies.reqwest]
     * version = "0.11"
     * features = ["json", "blocking"]
     */
    @Test
    public void testUpdateStandardTableVersion() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies.reqwest]
                version = "0.11"
                features = ["json", "blocking"]

                [dependencies]
                serde = "1.0.150"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement reqwestElement = findDependencyElement(file, "reqwest", "0.11");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(reqwestElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("reqwest version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("reqwest features should remain unchanged",
                updatedText.contains("features = [\"json\", \"blocking\"]"));
        assertTrue("serde should remain unchanged",
                updatedText.contains("serde = \"1.0.150\""));
    }

    /**
     * Test updating a simple string version with version requirement operator: serde = "=1.0.150"
     */
    @Test
    public void testUpdateSimpleStringVersionWithOperator() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                serde = "=1.0.150"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement serdeElement = findDependencyElement(file, "serde", "=1.0.150");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(serdeElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("serde version should be updated to " + NEW_VERSION,
                updatedText.contains("serde = \"" + NEW_VERSION + "\""));
    }

    /**
     * Test updating complex object format where version is not the first key:
     * clap = { features = ["derive"], version = "4.0", default-features = false }
     */
    @Test
    public void testUpdateComplexObjectVersionNotFirstKey() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                clap = { features = ["derive"], version = "4.0", default-features = false }
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement clapElement = findDependencyElement(file, "clap", "4.0");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(clapElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("clap version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("clap features should remain unchanged",
                updatedText.contains("features = [\"derive\"]"));
    }

    /**
     * Test updating complex object format with version requirement operator:
     * serde = { version = ">=1.0, <2.0", features = ["derive"] }
     */
    @Test
    public void testUpdateComplexObjectVersionWithOperator() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                serde = { version = ">=1.0, <2.0", features = ["derive"] }
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement serdeElement = findDependencyElement(file, "serde", ">=1.0, <2.0");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(serdeElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("serde version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("serde features should remain unchanged",
                updatedText.contains("features = [\"derive\"]"));
    }

    /**
     * Test updating standard table format with version requirement operator:
     * [dependencies.serde-json-wasm]
     * version = "=1.0.0"
     */
    @Test
    public void testUpdateStandardTableVersionWithOperator() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies.serde-json-wasm]
                version = "=1.0.0"
                features = ["std"]
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement element = findDependencyElement(file, "serde-json-wasm", "=1.0.0");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("serde-json-wasm version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("serde-json-wasm features should remain unchanged",
                updatedText.contains("features = [\"std\"]"));
    }

    /**
     * Test that null version is a no-op (does not modify the file).
     */
    @Test
    public void testUpdateWithNullVersionIsNoOp() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [dependencies]
                serde = "1.0.150"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement serdeElement = findDependencyElement(file, "serde", "1.0.150");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(serdeElement, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, null)
        );

        String updatedText = file.getText();
        assertTrue("serde should remain unchanged when version is null",
                updatedText.contains("serde = \"1.0.150\""));
    }

    /**
     * Test updating in target-specific dependencies section:
     * [target.'cfg(unix)'.dependencies]
     * openssl = "1.1"
     */
    @Test
    public void testUpdateTargetSpecificDependency() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [target.'cfg(unix)'.dependencies]
                openssl = "1.1"
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement element = findDependencyElement(file, "openssl", "1.1");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("openssl version should be updated to " + NEW_VERSION,
                updatedText.contains("openssl = \"" + NEW_VERSION + "\""));
    }

    /**
     * Test updating target-specific dependency in complex object format:
     * [target.'cfg(windows)'.dependencies]
     * winapi = { version = "0.3", features = ["winuser"] }
     */
    @Test
    public void testUpdateTargetSpecificComplexObjectDependency() {
        String cargoTomlContent = """
                [package]
                name = "test-crate"
                version = "0.1.0"

                [target.'cfg(windows)'.dependencies]
                winapi = { version = "0.3", features = ["winuser"] }
                """;

        PsiFile file = myFixture.configureByText("Cargo.toml", cargoTomlContent);
        PsiElement element = findDependencyElement(file, "winapi", "0.3");

        DependencyReport report = createReportWithRecommendation();
        CargoCAIntentionAction action = new CargoCAIntentionAction(element, dummySource(), report);

        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                action.updateVersion(getProject(), null, file, NEW_VERSION)
        );

        String updatedText = file.getText();
        assertTrue("winapi version should be updated to " + NEW_VERSION,
                updatedText.contains("version = \"" + NEW_VERSION + "\""));
        assertTrue("winapi features should remain unchanged",
                updatedText.contains("features = [\"winuser\"]"));
    }
}
