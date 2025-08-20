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

package org.jboss.tools.intellij.componentanalysis;

import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Simple tests for glob pattern matching behavior that don't require IntelliJ platform mocking.
 * These tests verify the core glob pattern logic that ManifestExclusionManager relies on.
 */
public class ManifestExclusionManagerSimpleTest {

    @Test
    public void testGlobPatternBehavior_RootFiles() {
        // Test the core issue: ** patterns don't match root files directly
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/pom.xml");
        
        // These should behave as expected
        assertFalse("**/pom.xml should NOT match root pom.xml directly", 
                   matcher.matches(Paths.get("pom.xml")));
        assertTrue("**/pom.xml should match nested pom.xml", 
                  matcher.matches(Paths.get("src/pom.xml")));
        assertTrue("**/pom.xml should match deeply nested pom.xml", 
                  matcher.matches(Paths.get("src/main/java/pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_WithVirtualPrefix() {
        // Test our workaround: adding a virtual directory prefix
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/pom.xml");
        
        // This should work with our virtual prefix approach
        assertTrue("**/pom.xml should match dummy/pom.xml (our workaround)", 
                  matcher.matches(Paths.get("dummy/pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_ExactMatch() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:pom.xml");
        
        assertTrue("pom.xml pattern should match root pom.xml", 
                  matcher.matches(Paths.get("pom.xml")));
        assertFalse("pom.xml pattern should NOT match nested pom.xml", 
                   matcher.matches(Paths.get("src/pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_DirectoryPatterns() {
        PathMatcher testMatcher = FileSystems.getDefault().getPathMatcher("glob:**/test/**");
        
        // Test directory exclusion patterns
        assertTrue("**/test/** should match files in test directories", 
                  testMatcher.matches(Paths.get("src/test/pom.xml")));
        assertTrue("**/test/** should match nested test directories", 
                  testMatcher.matches(Paths.get("modules/core/test/integration/pom.xml")));
        assertFalse("**/test/** should NOT match files outside test directories", 
                   testMatcher.matches(Paths.get("src/main/pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_NodeModules() {
        PathMatcher nodeMatcher = FileSystems.getDefault().getPathMatcher("glob:**/node_modules/**");
        
        // Note: **/node_modules/** requires directories before and after node_modules
        assertFalse("**/node_modules/** should NOT match root node_modules files (no dir before)", 
                   nodeMatcher.matches(Paths.get("node_modules/lodash/package.json")));
        assertTrue("**/node_modules/** should match nested node_modules", 
                  nodeMatcher.matches(Paths.get("frontend/node_modules/react/package.json")));
        assertFalse("**/node_modules/** should NOT match files outside node_modules", 
                   nodeMatcher.matches(Paths.get("src/package.json")));
    }

    @Test
    public void testGlobPatternBehavior_SpecificPaths() {
        PathMatcher specificMatcher = FileSystems.getDefault().getPathMatcher("glob:src/test/**/pom.xml");
        
        // Note: src/test/**/pom.xml requires at least one directory between test/ and pom.xml
        assertFalse("src/test/**/pom.xml should NOT match src/test/pom.xml (no intermediate dir)", 
                   specificMatcher.matches(Paths.get("src/test/pom.xml")));
        assertTrue("src/test/**/pom.xml should match with additional nesting", 
                  specificMatcher.matches(Paths.get("src/test/integration/pom.xml")));
        assertFalse("Specific pattern should NOT match different base paths", 
                   specificMatcher.matches(Paths.get("src/main/pom.xml")));
        assertFalse("Specific pattern should NOT match root files", 
                   specificMatcher.matches(Paths.get("pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_MultipleFileTypes() {
        String[] manifestFiles = {"pom.xml", "package.json", "go.mod", "requirements.txt", "build.gradle"};
        
        for (String fileName : manifestFiles) {
            PathMatcher exactMatcher = FileSystems.getDefault().getPathMatcher("glob:" + fileName);
            PathMatcher globalMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + fileName);
            
            // Exact patterns should match root files
            Path path = Paths.get(fileName);
            assertTrue(fileName + " should match at root with exact pattern",
                      exactMatcher.matches(path));
            
            // Global patterns should NOT match root files directly (Java PathMatcher limitation)
            assertFalse("**/" + fileName + " should NOT match root " + fileName + " directly", 
                       globalMatcher.matches(path));
            
            // But should match with virtual prefix (our workaround)
            assertTrue("**/" + fileName + " should match with virtual prefix", 
                      globalMatcher.matches(Paths.get("dummy/" + fileName)));
            
            // And should match nested files normally
            assertTrue("**/" + fileName + " should match nested " + fileName, 
                      globalMatcher.matches(Paths.get("src/" + fileName)));
        }
    }

    @Test
    public void testGlobPatternBehavior_BuildDirectories() {
        PathMatcher buildMatcher = FileSystems.getDefault().getPathMatcher("glob:**/build/**");
        PathMatcher targetMatcher = FileSystems.getDefault().getPathMatcher("glob:**/target/**");
        
        // Test build directory exclusions (note: **/ requires a directory before build)
        assertFalse("**/build/** should NOT match root build files (no dir before)", 
                   buildMatcher.matches(Paths.get("build/libs/pom.xml")));
        assertTrue("**/build/** should match nested build directories", 
                  buildMatcher.matches(Paths.get("modules/core/build/classes/pom.xml")));
        
        // Test target directory exclusions (note: **/ requires a directory before target)
        assertFalse("**/target/** should NOT match root target files (no dir before)", 
                   targetMatcher.matches(Paths.get("target/classes/pom.xml")));
        assertTrue("**/target/** should match nested target directories", 
                  targetMatcher.matches(Paths.get("modules/core/target/generated/pom.xml")));
    }

    @Test
    public void testGlobPatternBehavior_EdgeCases() {
        // Test edge cases and special characters
        PathMatcher wildcardMatcher = FileSystems.getDefault().getPathMatcher("glob:**");
        
        // ** should match everything
        assertTrue("** should match root files", 
                  wildcardMatcher.matches(Paths.get("pom.xml")));
        assertTrue("** should match nested files", 
                  wildcardMatcher.matches(Paths.get("src/main/pom.xml")));
        
        // Test single directory wildcard
        PathMatcher singleMatcher = FileSystems.getDefault().getPathMatcher("glob:*/pom.xml");
        assertFalse("*/pom.xml should NOT match root pom.xml", 
                   singleMatcher.matches(Paths.get("pom.xml")));
        assertTrue("*/pom.xml should match one level deep", 
                  singleMatcher.matches(Paths.get("src/pom.xml")));
        assertFalse("*/pom.xml should NOT match two levels deep", 
                   singleMatcher.matches(Paths.get("src/main/pom.xml")));
    }
}