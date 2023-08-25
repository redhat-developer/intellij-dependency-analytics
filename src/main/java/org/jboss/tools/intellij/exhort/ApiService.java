/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.exhort;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.redhat.exhort.Api;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.impl.ExhortApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service(Service.Level.PROJECT)
public final class ApiService {

    private static final Logger LOG = Logger.getInstance(ApiService.class);

    enum TelemetryKeys {
        MANIFEST, ECOSYSTEM, PLATFORM;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private final Api exhortApi;

    public ApiService() {
        this(new ExhortApi());
    }

    ApiService(Api exhortApi) {
        this.exhortApi = exhortApi;
    }

    public Path getStackAnalysis(final String packageManager, final String manifestName, final String manifestPath) {
        var telemetryMsg = TelemetryService.instance().action("stack-analysis");
        telemetryMsg.property(TelemetryKeys.ECOSYSTEM.toString(), packageManager);
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.MANIFEST.toString(), manifestName);

        try {
            var htmlContent = exhortApi.stackAnalysisHtml(manifestPath);
            var tmpFile = Files.createTempFile("exhort_", ".html");
            Files.write(tmpFile, htmlContent.get());

            telemetryMsg.send();
            return tmpFile;

        } catch (IOException | InterruptedException | ExecutionException exc) {
            telemetryMsg.error(exc);
            telemetryMsg.send();
            throw new RuntimeException(exc);
        }
    }

    public AnalysisReport getComponentAnalysis(final String packageManager, final String manifestName, final String manifestPath) {
        var telemetryMsg = TelemetryService.instance().action("component-analysis");
        telemetryMsg.property(TelemetryKeys.ECOSYSTEM.toString(), packageManager);
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.MANIFEST.toString(), manifestName);

        try {
            var manifestContent = Files.readAllBytes(Paths.get(manifestPath));
            CompletableFuture<AnalysisReport> componentReport = exhortApi.componentAnalysis(manifestPath);
            AnalysisReport report = componentReport.get();
            telemetryMsg.send();
            return report;
        } catch (IOException | InterruptedException | ExecutionException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            LOG.warn("Invalid manifest file submitted.", ex);
        } catch (CompletionException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            LOG.warn("Invalid vulnerability report returned.", ex);
        }
        return null;
    }
}
