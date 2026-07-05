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

package org.jboss.tools.intellij.settings;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ApiSettingsState field defaults and persistence behavior.
 * Uses direct instantiation to avoid IntelliJ platform dependencies.
 */
public class ApiSettingsStateTest {

    /** Verifies that recommendationsEnabled defaults to true. */
    @Test
    public void testRecommendationsEnabledDefaultsToTrue() {
        ApiSettingsState state = new ApiSettingsState();
        assertTrue("recommendationsEnabled should default to true", state.recommendationsEnabled);
    }

    /** Verifies that recommendationsEnabled can be toggled to false. */
    @Test
    public void testRecommendationsEnabledCanBeDisabled() {
        // Given a settings state with defaults
        ApiSettingsState state = new ApiSettingsState();

        // When disabling recommendations
        state.recommendationsEnabled = false;

        // Then the value is persisted
        assertFalse("recommendationsEnabled should be false after disabling", state.recommendationsEnabled);
    }

    /** Verifies that licenseCheckEnabled defaults to true (existing behavior). */
    @Test
    public void testLicenseCheckEnabledDefaultsToTrue() {
        ApiSettingsState state = new ApiSettingsState();
        assertTrue("licenseCheckEnabled should default to true", state.licenseCheckEnabled);
    }
}
