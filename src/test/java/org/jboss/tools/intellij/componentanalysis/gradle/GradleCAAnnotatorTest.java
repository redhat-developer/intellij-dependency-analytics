package org.jboss.tools.intellij.componentanalysis.gradle;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.jboss.tools.intellij.componentanalysis.gradle.build.filetype.BuildGradleFileType;

public class GradleCAAnnotatorTest extends BasePlatformTestCase {
    public void testGroovyDependencyGroupsDoNotIncludeQuotes() {
        String source = "dependencies {\n"
                + "    implementation 'org.apache.commons:commons-lang3:3.11'\n"
                + "    implementation \"org.apache.commons:commons-text:1.9\"\n"
                + "}\n";
        PsiFile file = PsiFileFactory.getInstance(getProject())
                .createFileFromText("build.gradle", BuildGradleFileType.INSTANCE, source);

        var dependencies = new GradleCAAnnotator().getDependencies(file);

        assertEquals(2, dependencies.size());
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-lang3", "3.11")));
        assertTrue(dependencies.containsKey(new Dependency("maven", "org.apache.commons", "commons-text", "1.9")));
    }
}
