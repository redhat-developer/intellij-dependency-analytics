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

package org.jboss.tools.intellij.exhort;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import io.github.guacsec.trustifyda.Api;
import org.jboss.tools.intellij.settings.ApiSettingsState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ApiService.getBatchStackAnalysis, verifying that the service
 * correctly delegates to the ExhortApi and handles exclusion patterns.
 */
public class ApiServiceBatchTest {

    private Api mockApi;
    private ApiService apiService;
    private MockedStatic<TelemetryService> telemetryServiceMock;
    private MockedStatic<ApiSettingsState> apiSettingsStateMock;

    private static final List<String> BATCH_PROPERTIES = Arrays.asList(
            "TRUSTIFY_DA_BATCH_CONCURRENCY",
            "TRUSTIFY_DA_CONTINUE_ON_ERROR",
            "TRUSTIFY_DA_BATCH_METADATA"
    );
    private final Map<String, String> originalProperties = new HashMap<>();

    @Before
    public void setUp() {
        mockApi = mock(Api.class);
        apiService = spy(new ApiService(mockApi));
        doNothing().when(apiService).setBatchRequestProperties();

        // Mock TelemetryService
        TelemetryMessageBuilder mockBuilder = mock(TelemetryMessageBuilder.class);
        TelemetryMessageBuilder.ActionMessage mockAction = mock(TelemetryMessageBuilder.ActionMessage.class);
        when(mockBuilder.action(anyString())).thenReturn(mockAction);
        when(mockAction.property(anyString(), anyString())).thenReturn(mockAction);
        telemetryServiceMock = mockStatic(TelemetryService.class);
        telemetryServiceMock.when(TelemetryService::instance).thenReturn(mockBuilder);

        // Mock ApiSettingsState
        ApiSettingsState mockSettings = new ApiSettingsState();
        mockSettings.rhdaToken = "test-token";
        apiSettingsStateMock = mockStatic(ApiSettingsState.class);
        apiSettingsStateMock.when(ApiSettingsState::getInstance).thenReturn(mockSettings);

        for (String key : BATCH_PROPERTIES) {
            originalProperties.put(key, System.getProperty(key));
        }
    }

    @After
    public void tearDown() {
        telemetryServiceMock.close();
        apiSettingsStateMock.close();
        for (String key : BATCH_PROPERTIES) {
            String original = originalProperties.get(key);
            if (original != null) {
                System.setProperty(key, original);
            } else {
                System.clearProperty(key);
            }
        }
    }

    /** Verifies that getBatchStackAnalysis delegates to exhortApi and returns a valid HTML file. */
    @Test
    public void testGetBatchStackAnalysis_returnsHtmlReport() throws Exception {
        // Given
        byte[] htmlContent = "<html><body>Batch Report</body></html>".getBytes();
        when(mockApi.stackAnalysisBatchHtml(any(Path.class), any()))
                .thenReturn(CompletableFuture.completedFuture(htmlContent));

        // When
        Path result = apiService.getBatchStackAnalysis("/workspace", Collections.emptyList());

        // Then
        assertNotNull("Report path should not be null", result);
        assertTrue("Report file should exist", Files.exists(result));
        String content = Files.readString(result);
        assertEquals("<html><body>Batch Report</body></html>", content);
        assertTrue("Report filename should have .html extension", result.toString().endsWith(".html"));

        Files.deleteIfExists(result);
    }

    /** Verifies that exclusion patterns are correctly passed to the API as a Set. */
    @Test
    public void testGetBatchStackAnalysis_passesIgnorePatterns() throws Exception {
        // Given
        byte[] htmlContent = "<html></html>".getBytes();
        when(mockApi.stackAnalysisBatchHtml(any(Path.class), any()))
                .thenReturn(CompletableFuture.completedFuture(htmlContent));
        List<String> patterns = Arrays.asList("**/node_modules/**", "**/dist/**");

        // When
        Path result = apiService.getBatchStackAnalysis("/workspace", patterns);

        // Then
        Set<String> expectedPatterns = new HashSet<>(Arrays.asList("**/node_modules/**", "**/dist/**"));
        verify(mockApi).stackAnalysisBatchHtml(eq(Path.of("/workspace")), eq(expectedPatterns));

        Files.deleteIfExists(result);
    }

    /** Verifies that an empty ignore patterns list results in an empty set passed to the API. */
    @Test
    public void testGetBatchStackAnalysis_emptyIgnorePatterns() throws Exception {
        // Given
        byte[] htmlContent = "<html></html>".getBytes();
        when(mockApi.stackAnalysisBatchHtml(any(Path.class), any()))
                .thenReturn(CompletableFuture.completedFuture(htmlContent));

        // When
        Path result = apiService.getBatchStackAnalysis("/workspace", Collections.emptyList());

        // Then
        verify(mockApi).stackAnalysisBatchHtml(eq(Path.of("/workspace")), eq(Collections.emptySet()));

        Files.deleteIfExists(result);
    }

    /** Verifies that IOException from the API is wrapped in RuntimeException. */
    @Test(expected = RuntimeException.class)
    public void testGetBatchStackAnalysis_wrapsIOException() throws Exception {
        // Given
        when(mockApi.stackAnalysisBatchHtml(any(Path.class), any()))
                .thenThrow(new IOException("workspace not found"));

        // When
        apiService.getBatchStackAnalysis("/nonexistent", Collections.emptyList());
    }
}
