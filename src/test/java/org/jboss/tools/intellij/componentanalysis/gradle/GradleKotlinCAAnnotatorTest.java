package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.componentanalysis.Dependency;

public class GradleKotlinCAAnnotatorTest extends BasePlatformTestCase {
    public void testLiteralDependenciesInKotlinBuild() {
        PsiFile file = myFixture.configureByText("build.gradle.kts", """
                dependencies {
                    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
                    testImplementation("org.apache.commons:commons-text:1.9")
                    implementation("org.example:dynamic:$version")
                }
                """);

        var dependencies = new GradleKotlinCAAnnotator().getDependencies(file);

        assertEquals("kotlin", file.getLanguage().getID());
        myFixture.enableInspections(new GradleKotlinCAInspection());
        assertNotNull(HighlightDisplayKey.find(GradleKotlinCAInspection.SHORT_NAME));
        assertNotNull(new GradleKotlinCAAnnotator().collectInformation(file));
        assertEquals(2, dependencies.size());
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.logging.log4j", "log4j-core", "2.14.1")));
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")));

        var literal = dependencies.get(new Dependency("maven", "org.apache.logging.log4j", "log4j-core", "2.14.1")).get(0);
        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                new GradleCAIntentionAction(literal, null, null)
                        .updateVersion(getProject(), myFixture.getEditor(), file, "2.17.1"));
        PsiDocumentManager.getInstance(getProject()).commitDocument(myFixture.getEditor().getDocument());
        assertTrue(myFixture.getEditor().getDocument().getText().contains("log4j-core:2.17.1"));
    }

    public void testIgnoredDependenciesAreSkipped() {
        PsiFile file = myFixture.configureByText("build.gradle.kts", """
                dependencies {
                    implementation("org.apache.logging.log4j:log4j-core:2.14.1") // trustify-da-ignore
                    implementation("org.springframework:spring-webmvc:5.3.17") // exhortignore
                    implementation("org.apache.commons:commons-text:1.9")
                }
                """);

        var dependencies = new GradleKotlinCAAnnotator().getDependencies(file);

        assertEquals(1, dependencies.size());
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")));
        assertFalse(dependencies.containsKey(new Dependency("maven", "org.apache.logging.log4j", "log4j-core", "2.14.1")));
        assertFalse(dependencies.containsKey(new Dependency("maven", "org.springframework", "spring-webmvc", "5.3.17")));
    }

    public void testNamedArgumentDependencies() {
        PsiFile file = myFixture.configureByText("build.gradle.kts", """
                dependencies {
                    implementation(group = "org.apache.commons", name = "commons-text", version = "1.9")
                    implementation(group = "org.example", name = "dynamic", version = "$v")
                }
                """);

        var dependencies = new GradleKotlinCAAnnotator().getDependencies(file);

        assertEquals(1, dependencies.size());
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")));

        var literal = dependencies.get(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")).get(0);
        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                new GradleCAIntentionAction(literal, null, null)
                        .updateVersion(getProject(), myFixture.getEditor(), file, "1.10.0"));
        PsiDocumentManager.getInstance(getProject()).commitDocument(myFixture.getEditor().getDocument());
        assertTrue(myFixture.getEditor().getDocument().getText().contains("version = \"1.10.0\""));
    }

    public void testVersionCatalogDependencies() {
        myFixture.addFileToProject("gradle/libs.versions.toml", """
                [versions]
                commonsText = "1.9"

                [libraries]
                commons-text = { module = "org.apache.commons:commons-text", version.ref = "commonsText" }
                log4j-core = "org.apache.logging.log4j:log4j-core:2.14.1"
                """);
        PsiFile file = myFixture.configureByText("build.gradle.kts", """
                dependencies {
                    implementation(libs.commons.text)
                    implementation(libs.log4j.core)
                }
                """);

        var dependencies = new GradleKotlinCAAnnotator().getDependencies(file);

        assertEquals(2, dependencies.size());
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")));
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.logging.log4j", "log4j-core", "2.14.1")));

        // Quick-fix bumps the version inside libs.versions.toml (via version.ref).
        var accessor = dependencies.get(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")).get(0);
        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                new GradleCAIntentionAction(accessor, null, null)
                        .updateVersion(getProject(), myFixture.getEditor(), file, "1.10.0"));

        PsiFile toml = GradleVersionCatalog.resolve(file, java.util.List.of("commons", "text")).tomlFile();
        var tomlDoc = PsiDocumentManager.getInstance(getProject()).getDocument(toml);
        PsiDocumentManager.getInstance(getProject()).commitDocument(tomlDoc);
        assertTrue(tomlDoc.getText().contains("commonsText = \"1.10.0\""));
    }
}
