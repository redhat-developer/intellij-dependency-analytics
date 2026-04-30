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

package org.jboss.tools.intellij.exhort;

import io.github.guacsec.trustifyda.Api;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for ApiService that don't require IntelliJ platform initialization.
 * The generateSbom method depends on TelemetryService and ApiSettingsState
 * which need the platform, so we verify the API contract at the interface level.
 */
public class ApiServiceTest {

    @Test
    public void apiInterfaceHasGenerateSbomMethod() throws NoSuchMethodException {
        Method method = Api.class.getMethod("generateSbom", String.class);
        assertNotNull("Api interface should have generateSbom(String) method", method);
        assertEquals("generateSbom should return String", String.class, method.getReturnType());
    }

    @Test
    public void apiServiceHasGenerateSbomMethod() throws NoSuchMethodException {
        Method method = ApiService.class.getMethod("generateSbom", String.class, String.class, String.class);
        assertNotNull("ApiService should have generateSbom(String, String, String) method", method);
        assertEquals("generateSbom should return String", String.class, method.getReturnType());
    }

    @Test
    public void apiServiceAcceptsApiInConstructor() throws NoSuchMethodException {
        // Verify the package-private constructor used for testing exists
        ApiService.class.getDeclaredConstructor(Api.class);
    }
}
