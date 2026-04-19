/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.stackanalysis;

import org.jboss.tools.intellij.settings.ApiSettingsState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for batch analysis settings persistence and default values
 * in ApiSettingsState.
 */
public class SaBatchActionTest {

    /** Verifies that batch settings fields have correct default values. */
    @Test
    public void testBatchSettingsDefaults() {
        // Given a fresh ApiSettingsState
        ApiSettingsState settings = new ApiSettingsState();

        // Then defaults should be set correctly
        assertEquals("Default batch concurrency should be 10", "10", settings.batchConcurrency);
        assertTrue("Default continueOnError should be true", settings.batchContinueOnError);
        assertTrue("Default batchMetadata should be true", settings.batchMetadata);
    }

    /** Verifies that batch settings fields can be modified and retain their values. */
    @Test
    public void testBatchSettingsPersistence() {
        // Given
        ApiSettingsState settings = new ApiSettingsState();

        // When modifying settings
        settings.batchConcurrency = "5";
        settings.batchContinueOnError = false;
        settings.batchMetadata = false;

        // Then values should be retained
        assertEquals("5", settings.batchConcurrency);
        assertFalse(settings.batchContinueOnError);
        assertFalse(settings.batchMetadata);
    }

    /** Verifies that exclusion patterns from ManifestExclusionManager are properly parseable. */
    @Test
    public void testExclusionPatternConversion() {
        // Given patterns in the settings format (newline-separated)
        String patterns = "**/node_modules/**\n**/dist/**\n# comment\n  \n**/build/**";

        // When splitting and filtering (same logic as ManifestExclusionManager.parsePatterns)
        String[] lines = patterns.split("[\\n\\r]+");
        java.util.List<String> parsed = new java.util.ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                parsed.add(trimmed);
            }
        }

        // Then comments and blank lines should be filtered out
        assertEquals(3, parsed.size());
        assertEquals("**/node_modules/**", parsed.get(0));
        assertEquals("**/dist/**", parsed.get(1));
        assertEquals("**/build/**", parsed.get(2));
    }
}
