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

import org.junit.Test;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests for go.mod parsing patterns and scenarios based on real go.mod files.
 * These tests verify the parsing logic using the actual patterns from GoCAAnnotator.
 */
public class GoModParserTest {

    @Test
    public void testParseRequireLines_FromGoModFileA() {
        // Test require lines from the first go.mod file
        String[] requireLines = {
            "    github.com/gin-gonic/gin v1.4.0",
            "    github.com/spf13/viper v1.3.2",
            "    google.golang.org/protobuf v1.30.0",
            "    golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2 // indirect",
            "    github.com/dgrijalva/jwt-go v3.2.0+incompatible"
        };

        for (String line : requireLines) {
            Matcher matcher = GoCAAnnotator.REQUIRE_PATTERN.matcher(line);
            assertTrue("Should match require line: " + line, matcher.matches());

            String module = matcher.group(1);
            String version = matcher.group(2);

            assertNotNull("Module should not be null", module);
            assertNotNull("Version should not be null", version);

            // Verify specific extractions
            if (line.contains("gin-gonic")) {
                assertEquals("github.com/gin-gonic/gin", module);
                assertEquals("v1.4.0", version);
            } else if (line.contains("crypto")) {
                assertEquals("golang.org/x/crypto", module);
                assertEquals("v0.0.0-20190308221718-c2843e01d9a2", version);
            } else if (line.contains("incompatible")) {
                assertEquals("github.com/dgrijalva/jwt-go", module);
                assertEquals("v3.2.0+incompatible", version);
            }
        }
    }

    @Test
    public void testParseRequireLines_FromGoModFileB() {
        // Test require lines from the second go.mod file
        String[] requireLines = {
            "    golang.org/x/net v0.10.0",
            "    google.golang.org/protobuf v1.30.0",
            "    github.com/google/go-cmp v0.5.9 // indirect",
            "    golang.org/x/text v0.9.0 // indirect"
        };

        for (String line : requireLines) {
            Matcher matcher = GoCAAnnotator.REQUIRE_PATTERN.matcher(line);
            assertTrue("Should match require line: " + line, matcher.matches());

            String module = matcher.group(1);
            String version = matcher.group(2);

            assertNotNull("Module should not be null", module);
            assertNotNull("Version should not be null", version);

            // Verify specific extractions
            if (line.contains("x/net")) {
                assertEquals("golang.org/x/net", module);
                assertEquals("v0.10.0", version);
            } else if (line.contains("go-cmp")) {
                assertEquals("github.com/google/go-cmp", module);
                assertEquals("v0.5.9", version);
            }
        }
    }

    @Test
    public void testParseReplaceLines_FromGoModFileA() {
        // Test replace lines from the first go.mod file
        String[] replaceLines = {
            "    github.com/gin-gonic/gin v1.9.1 => github.com/myfork/gin v1.9.1-patch.2",
            "replace github.com/onsi/ginkgo/v2 => github.com/openshift/onsi-ginkgo/v2 v2.6.1-0.20250416174521-4eb003743b54",
            "replace github.com/myorg/anotherlib => /Users/chao/workspace/anotherlib"
        };

        for (String line : replaceLines) {
            // Strip "replace " prefix if present, just like GoCAAnnotator does
            String replaceLine = line.startsWith("replace ") ? line.substring(8).trim() : line.trim();
            Matcher matcher = GoCAAnnotator.REPLACE_PATTERN.matcher(replaceLine);
            assertTrue("Should match replace line: " + line, matcher.matches());

            String sourceModule = matcher.group(1);
            String sourceVersion = matcher.group(2);
            String targetPath = matcher.group(3);
            String targetVersion = matcher.group(4);

            assertNotNull("Source module should not be null", sourceModule);
            assertNotNull("Target path should not be null", targetPath);

            // Verify specific extractions
            if (line.contains("gin-gonic")) {
                assertEquals("github.com/gin-gonic/gin", sourceModule);
                assertEquals("v1.9.1", sourceVersion);
                assertEquals("github.com/myfork/gin", targetPath);
                assertEquals("v1.9.1-patch.2", targetVersion);
            } else if (line.contains("ginkgo")) {
                assertEquals("github.com/onsi/ginkgo/v2", sourceModule);
                assertNull("Ginkgo source version should be null", sourceVersion);
                assertEquals("github.com/openshift/onsi-ginkgo/v2", targetPath);
                assertEquals("v2.6.1-0.20250416174521-4eb003743b54", targetVersion);
            } else if (line.contains("anotherlib")) {
                assertEquals("github.com/myorg/anotherlib", sourceModule);
                assertNull("Anotherlib source version should be null", sourceVersion);
                assertEquals("/Users/chao/workspace/anotherlib", targetPath);
                assertNull("Anotherlib target version should be null", targetVersion);
            }
        }
    }

    @Test
    public void testVersionFormats() {
        // Test various version formats from both go.mod files
        String[] versionFormats = {
            "v1.4.0",                                          // Standard semantic version
            "v1.3.2",                                          // Standard version
            "v1.30.0",                                         // Standard version
            "v0.10.0",                                         // Standard version
            "v0.0.0-20190308221718-c2843e01d9a2",             // Pseudo-version (timestamp)
            "v3.2.0+incompatible",                            // Incompatible version
            "v2.6.1-0.20250416174521-4eb003743b54",           // Complex version with timestamp
            "v1.9.1-patch.2",                                 // Custom patch version
            "v0.5.9",                                          // Standard version
            "v0.9.0"                                           // Standard version
        };

        Pattern versionPattern = Pattern.compile("v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*");

        for (String version : versionFormats) {
            Matcher matcher = versionPattern.matcher(version);
            assertTrue("Should match version format: " + version, matcher.matches());
        }
    }

    @Test
    public void testModuleNameFormats() {
        // Test various module name formats from both go.mod files
        String[] moduleNames = {
            "github.com/gin-gonic/gin",
            "github.com/spf13/viper",
            "google.golang.org/protobuf",
            "golang.org/x/crypto",
            "golang.org/x/net",
            "github.com/dgrijalva/jwt-go",
            "github.com/google/go-cmp",
            "golang.org/x/text",
            "github.com/onsi/ginkgo/v2",
            "github.com/openshift/onsi-ginkgo/v2",
            "github.com/myfork/gin",
            "github.com/myorg/anotherlib"
        };

        Pattern modulePattern = Pattern.compile("[a-zA-Z0-9._/-]+");

        for (String module : moduleNames) {
            Matcher matcher = modulePattern.matcher(module);
            assertTrue("Should match module name: " + module, matcher.matches());
        }
    }

    @Test
    public void testPathFormats() {
        // Test path formats from the go.mod files
        String[] pathFormats = {
            "../viper-local",                    // Relative path (commented in file)
            "/Users/chao/workspace/anotherlib",  // Absolute path
            "github.com/myfork/gin",             // Module path
            "github.com/openshift/onsi-ginkgo/v2" // Module path with version
        };

        // Pattern for identifiers including paths
        Pattern pathPattern = Pattern.compile("(\\.\\.?/[a-zA-Z0-9._/-]*|/[a-zA-Z0-9._/-]+|[a-zA-Z_][a-zA-Z0-9._/-]*)");

        for (String path : pathFormats) {
            Matcher matcher = pathPattern.matcher(path);
            assertTrue("Should match path format: " + path, matcher.matches());
        }
    }

    @Test
    public void testCommentHandling() {
        // Test lines with comments from both files
        String[] linesWithComments = {
            "    golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2 // indirect",
            "    github.com/google/go-cmp v0.5.9 // indirect",
            "    golang.org/x/text v0.9.0 // indirect",
            "//    github.com/spf13/viper v1.3.2 => ../viper-local"
        };

        for (String line : linesWithComments) {
            Matcher requireMatcher = GoCAAnnotator.REQUIRE_PATTERN.matcher(line);
            if (line.trim().startsWith("//")) {
                // Commented out line - should not match require pattern
                assertFalse("Commented line should not match require pattern: " + line,
                           requireMatcher.matches());
            } else {
                // Line with inline comment - should still match
                assertTrue("Line with inline comment should match: " + line,
                          requireMatcher.matches());
            }
        }
    }

    @Test
    public void testGoVersionStatements() {
        // Test go version statements from both files
        String[] goVersions = {
            "go 1.18",
            "go 1.20"
        };

        Pattern goPattern = Pattern.compile("^\\s*go\\s+([0-9]+\\.[0-9]+)\\s*$");

        for (String goVersion : goVersions) {
            Matcher matcher = goPattern.matcher(goVersion);
            assertTrue("Should match go version: " + goVersion, matcher.matches());

            String version = matcher.group(1);
            assertTrue("Version should be valid", version.equals("1.18") || version.equals("1.20"));
        }
    }

    @Test
    public void testExcludeStatements() {
        // Test exclude statement from the first file
        String excludeLine = "exclude github.com/gin-gonic/gin v1.5.0";

        Pattern excludePattern = Pattern.compile("^\\s*exclude\\s+([a-zA-Z0-9._/-]+)\\s+(v?[0-9]+(?:\\.[0-9]+)*[0-9a-zA-Z\\-+._]*)\\s*$");

        Matcher matcher = excludePattern.matcher(excludeLine);
        assertTrue("Should match exclude statement", matcher.matches());

        String module = matcher.group(1);
        String version = matcher.group(2);

        assertEquals("github.com/gin-gonic/gin", module);
        assertEquals("v1.5.0", version);
    }

    @Test
    public void testComplexRealWorldScenarios() {
        // Test parsing a complete require block structure
        String[] requireBlockLines = {
            "require (",
            "    github.com/gin-gonic/gin v1.4.0",
            "    github.com/spf13/viper v1.3.2",
            "    google.golang.org/protobuf v1.30.0",
            "    golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2 // indirect",
            "    github.com/dgrijalva/jwt-go v3.2.0+incompatible",
            ")"
        };

        int validRequireLines = 0;
        for (String line : requireBlockLines) {
            if (line.trim().equals("require (") || line.trim().equals(")")) {
                // Block delimiters - not require specs
                continue;
            }

            Matcher matcher = GoCAAnnotator.REQUIRE_PATTERN.matcher(line);
            if (matcher.matches()) {
                validRequireLines++;
            }
        }

        assertEquals("Should find 5 valid require specifications", 5, validRequireLines);
    }

    @Test
    public void testVersionPatternFromGoCAIntentionAction() {
        // Test the VERSION_PATTERN used for version updates in GoCAIntentionAction
        String[] testLines = {
            "    github.com/gin-gonic/gin v1.4.0",
            "    github.com/spf13/viper v1.3.2",
            "    google.golang.org/protobuf v1.30.0",
            "    golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2 // indirect",
            "    github.com/dgrijalva/jwt-go v3.2.0+incompatible",
            "    golang.org/x/net v0.10.0",
            "    github.com/google/go-cmp v0.5.9 // indirect",
            "    golang.org/x/text v0.9.0 // indirect",
            "github.com/gin-gonic/gin v1.9.1 => github.com/myfork/gin v1.9.1-patch.2",
            "github.com/onsi/ginkgo/v2 => github.com/openshift/onsi-ginkgo/v2 v2.6.1-0.20250416174521-4eb003743b54"
        };

        for (String line : testLines) {
            Matcher matcher = GoCAIntentionAction.VERSION_PATTERN.matcher(line);
            assertTrue("Should find version in line: " + line, matcher.find());

            String whitespace = matcher.group(1);
            String version = matcher.group(2);

            assertNotNull("Whitespace should not be null", whitespace);
            assertNotNull("Version should not be null", version);
            assertFalse("Should have whitespace before version", whitespace.isEmpty());

            // Verify specific version extractions
            if (line.contains("gin-gonic/gin v1.4.0")) {
                assertEquals("v1.4.0", version);
            } else if (line.contains("crypto")) {
                assertEquals("v0.0.0-20190308221718-c2843e01d9a2", version);
            } else if (line.contains("incompatible")) {
                assertEquals("v3.2.0+incompatible", version);
            } else if (line.contains("v2.6.1-0.20250416174521-4eb003743b54")) {
                assertEquals("v2.6.1-0.20250416174521-4eb003743b54", version);
            }
        }
    }

    @Test
    public void testVersionPatternCapturingGroups() {
        // Test that VERSION_PATTERN correctly captures whitespace and version for replacement
        String testLine = "    github.com/gin-gonic/gin v1.4.0";
        Matcher matcher = GoCAIntentionAction.VERSION_PATTERN.matcher(testLine);

        assertTrue("Should match the line", matcher.find());

        String whitespace = matcher.group(1);
        String version = matcher.group(2);

        assertEquals("Should capture whitespace", " ", whitespace);
        assertEquals("Should capture version", "v1.4.0", version);

        // Test version replacement scenario
        String newVersion = "v1.5.0";
        String updatedLine = testLine.substring(0, matcher.start(2)) + newVersion + testLine.substring(matcher.end(2));
        assertEquals("Should replace version correctly", "    github.com/gin-gonic/gin v1.5.0", updatedLine);
    }

    @Test
    public void testVersionPatternEdgeCases() {
        // Test edge cases for VERSION_PATTERN
        String[] shouldMatch = {
            " v1.2.3",           // Basic semantic version
            "  v0.0.1",          // With more whitespace
            "\tv1.0.0",          // With tab
            " 1.2.3",            // Version without 'v' prefix
            " v1.2.3-alpha",     // Pre-release version
            " v1.2.3+build",     // Build metadata
            " v1.2.3-alpha+build" // Both pre-release and build
        };

        String[] shouldNotMatch = {
            "v1.2.3",            // No leading whitespace
            " v1.2",             // Only major.minor (VERSION_PATTERN expects major.minor.patch)
            " version1.2.3",     // Extra text before version
            "github.com/module", // Module name only
        };

        for (String line : shouldMatch) {
            Matcher matcher = GoCAIntentionAction.VERSION_PATTERN.matcher(line);
            assertTrue("Should match: '" + line + "'", matcher.find());
        }

        for (String line : shouldNotMatch) {
            Matcher matcher = GoCAIntentionAction.VERSION_PATTERN.matcher(line);
            assertFalse("Should NOT match: '" + line + "'", matcher.find());
        }
    }
}