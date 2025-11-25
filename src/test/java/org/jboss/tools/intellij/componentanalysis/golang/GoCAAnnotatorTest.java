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

package org.jboss.tools.intellij.componentanalysis.golang;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jboss.tools.intellij.componentanalysis.Dependency;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive test to verify the GoCAAnnotator fix for all dependency declaration formats:
 * 1. Single-line require and replace statements
 * 2. Require and replace blocks
 * 3. Mixed cases
 */
public class GoCAAnnotatorTest extends BasePlatformTestCase {

    /**
     * Testable subclass that exposes the protected getDependencies method
     */
    private static class TestableGoCAAnnotator extends GoCAAnnotator {
        @Override
        public Map<Dependency, List<PsiElement>> getDependencies(PsiFile file) {
            return super.getDependencies(file);
        }
    }

    /**
     * Test Case 1: Single-line require and replace statements
     */
    @Test
    public void testSingleLineRequireAndReplaceStatements() {
        String goModContent = """
                module test-single-line

                go 1.20

                require golang.org/x/net v0.10.0
                require golang.org/x/text v0.9.0 // indirect
                require google.golang.org/protobuf v1.30.0
                require github.com/google/go-cmp v0.5.9 // indirect

                replace github.com/gin-gonic/gin v1.9.1 => github.com/myfork/gin v1.9.1-patch.2
                replace github.com/onsi/ginkgo/v2 => github.com/openshift/onsi-ginkgo/v2 v2.6.1-0.20250416174521-4eb003743b54
                replace golang.org/x/crypto => golang.org/x/crypto v0.1.0
                """;

        TestableGoCAAnnotator annotator = new TestableGoCAAnnotator();
        PsiFile file = myFixture.configureByText("go.mod", goModContent);
        Map<Dependency, List<PsiElement>> dependencies = annotator.getDependencies(file);

        assertEquals("Should find 7 dependencies (4 require + 3 replace targets)", 7, dependencies.size());

        // Verify require dependencies
        assertTrue("Should contain golang.org/x/net", containsDependency(dependencies, "golang.org/x", "net", "v0.10.0"));
        assertTrue("Should contain golang.org/x/text", containsDependency(dependencies, "golang.org/x", "text", "v0.9.0"));
        assertTrue("Should contain google.golang.org/protobuf", containsDependency(dependencies, "google.golang.org", "protobuf", "v1.30.0"));
        assertTrue("Should contain github.com/google/go-cmp", containsDependency(dependencies, "github.com/google", "go-cmp", "v0.5.9"));

        // Verify replace target dependencies
        assertTrue("Should contain github.com/myfork/gin", containsDependency(dependencies, "github.com/myfork", "gin", "v1.9.1-patch.2"));
        assertTrue("Should contain github.com/openshift/onsi-ginkgo/v2", containsDependency(dependencies, "github.com/openshift/onsi-ginkgo", "v2", "v2.6.1-0.20250416174521-4eb003743b54"));
        assertTrue("Should contain golang.org/x/crypto", containsDependency(dependencies, "golang.org/x", "crypto", "v0.1.0"));
    }

    /**
     * Test Case 2: Require and replace blocks
     */
    @Test
    public void testRequireAndReplaceBlocks() {
        String goModContent = """
                module test-blocks

                go 1.20

                require (
                    github.com/gin-gonic/gin v1.4.0
                    github.com/spf13/viper v1.3.2
                    google.golang.org/protobuf v1.30.0
                    golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2 // indirect
                    github.com/dgrijalva/jwt-go v3.2.0+incompatible
                )

                replace (
                    github.com/gin-gonic/gin v1.9.1 => github.com/myfork/gin v1.9.1-patch.2
                    github.com/onsi/ginkgo/v2 => github.com/openshift/onsi-ginkgo/v2 v2.6.1-0.20250416174521-4eb003743b54
                    golang.org/x/crypto => golang.org/x/crypto v0.1.0
                )
                """;

        TestableGoCAAnnotator annotator = new TestableGoCAAnnotator();
        PsiFile file = myFixture.configureByText("go.mod", goModContent);
        Map<Dependency, List<PsiElement>> dependencies = annotator.getDependencies(file);

        assertEquals("Should find 8 dependencies (5 require + 3 replace targets)", 8, dependencies.size());

        // Verify require block dependencies
        assertTrue("Should contain github.com/gin-gonic/gin", containsDependency(dependencies, "github.com/gin-gonic", "gin", "v1.4.0"));
        assertTrue("Should contain github.com/spf13/viper", containsDependency(dependencies, "github.com/spf13", "viper", "v1.3.2"));
        assertTrue("Should contain google.golang.org/protobuf", containsDependency(dependencies, "google.golang.org", "protobuf", "v1.30.0"));
        assertTrue("Should contain golang.org/x/crypto", containsDependency(dependencies, "golang.org/x", "crypto", "v0.0.0-20190308221718-c2843e01d9a2"));
        assertTrue("Should contain github.com/dgrijalva/jwt-go", containsDependency(dependencies, "github.com/dgrijalva", "jwt-go", "v3.2.0+incompatible"));

        // Verify replace block target dependencies
        assertTrue("Should contain github.com/myfork/gin", containsDependency(dependencies, "github.com/myfork", "gin", "v1.9.1-patch.2"));
        assertTrue("Should contain github.com/openshift/onsi-ginkgo/v2", containsDependency(dependencies, "github.com/openshift/onsi-ginkgo", "v2", "v2.6.1-0.20250416174521-4eb003743b54"));
        assertTrue("Should contain golang.org/x/crypto v0.1.0", containsDependency(dependencies, "golang.org/x", "crypto", "v0.1.0"));
    }

    /**
     * Test Case 3: Mixed cases (single-line and blocks combined)
     */
    @Test
    public void testMixedRequireAndReplaceStatements() {
        String goModContent = """
                module test-mixed

                go 1.20

                require golang.org/x/net v0.10.0

                require (
                    github.com/gin-gonic/gin v1.4.0
                    github.com/spf13/viper v1.3.2
                )

                require google.golang.org/protobuf v1.30.0

                replace github.com/gin-gonic/gin v1.4.0 => github.com/myfork/gin v1.4.1

                replace (
                    golang.org/x/crypto => golang.org/x/crypto v0.1.0
                    github.com/old/module => github.com/new/module v1.0.0
                )

                require golang.org/x/text v0.9.0 // indirect
                """;

        TestableGoCAAnnotator annotator = new TestableGoCAAnnotator();
        PsiFile file = myFixture.configureByText("go.mod", goModContent);
        Map<Dependency, List<PsiElement>> dependencies = annotator.getDependencies(file);

        assertEquals("Should find 8 dependencies from mixed statements", 8, dependencies.size());

        // Verify single-line require dependencies
        assertTrue("Should contain golang.org/x/net", containsDependency(dependencies, "golang.org/x", "net", "v0.10.0"));
        assertTrue("Should contain google.golang.org/protobuf", containsDependency(dependencies, "google.golang.org", "protobuf", "v1.30.0"));
        assertTrue("Should contain golang.org/x/text", containsDependency(dependencies, "golang.org/x", "text", "v0.9.0"));

        // Verify require block dependencies
        assertTrue("Should contain github.com/gin-gonic/gin", containsDependency(dependencies, "github.com/gin-gonic", "gin", "v1.4.0"));
        assertTrue("Should contain github.com/spf13/viper", containsDependency(dependencies, "github.com/spf13", "viper", "v1.3.2"));

        // Verify single-line replace target dependencies
        assertTrue("Should contain github.com/myfork/gin", containsDependency(dependencies, "github.com/myfork", "gin", "v1.4.1"));

        // Verify replace block target dependencies
        assertTrue("Should contain golang.org/x/crypto", containsDependency(dependencies, "golang.org/x", "crypto", "v0.1.0"));
        assertTrue("Should contain github.com/new/module", containsDependency(dependencies, "github.com/new", "module", "v1.0.0"));
    }

    /**
     * Test Case 4: Robust parentheses detection - ensure comments with '(' don't break parsing
     */
    @Test
    public void testRobustParenthesesDetection() {
        String goModContent = """
                module test-robust-parentheses

                go 1.20

                require golang.org/x/net v0.10.0 // This comment has parentheses (test)
                require golang.org/x/text v0.9.0 // Another comment (with parens)

                require (
                    github.com/gin-gonic/gin v1.4.0 // Comment with (parentheses) inside block
                    github.com/spf13/viper v1.3.2
                )

                replace golang.org/x/crypto v0.1.0 => golang.org/x/crypto v0.2.0 // Comment (test)

                replace (
                    github.com/old/lib => github.com/new/lib v1.0.0
                )
                """;

        TestableGoCAAnnotator annotator = new TestableGoCAAnnotator();
        PsiFile file = myFixture.configureByText("go.mod", goModContent);
        Map<Dependency, List<PsiElement>> dependencies = annotator.getDependencies(file);

        assertEquals("Should find 6 dependencies despite comments with parentheses", 6, dependencies.size());

        // Verify single-line requires with comments containing '(' are parsed correctly
        assertTrue("Should contain golang.org/x/net despite comment with ()",
                  containsDependency(dependencies, "golang.org/x", "net", "v0.10.0"));
        assertTrue("Should contain golang.org/x/text despite comment with ()",
                  containsDependency(dependencies, "golang.org/x", "text", "v0.9.0"));

        // Verify block requires work correctly
        assertTrue("Should contain github.com/gin-gonic/gin from block",
                  containsDependency(dependencies, "github.com/gin-gonic", "gin", "v1.4.0"));
        assertTrue("Should contain github.com/spf13/viper from block",
                  containsDependency(dependencies, "github.com/spf13", "viper", "v1.3.2"));

        // Verify replace statements work correctly
        assertTrue("Should contain golang.org/x/crypto from single-line replace",
                  containsDependency(dependencies, "golang.org/x", "crypto", "v0.2.0"));
        assertTrue("Should contain github.com/new/lib from replace block",
                  containsDependency(dependencies, "github.com/new", "lib", "v1.0.0"));
    }

    /**
     * Helper method to check if dependencies contain a specific dependency with namespace, name, and version.
     */
    private boolean containsDependency(Map<Dependency, List<PsiElement>> dependencies, String expectedNamespace, String expectedName, String expectedVersion) {
        return dependencies.keySet().stream()
                .anyMatch(dep -> "golang".equals(dep.getType()) &&
                        expectedNamespace.equals(dep.getNamespace()) &&
                        expectedName.equals(dep.getName()) &&
                        expectedVersion.equals(dep.getVersion()));
    }
}