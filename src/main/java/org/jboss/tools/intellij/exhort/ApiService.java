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

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.redhat.exhort.Api;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.impl.ExhortApi;
import org.jboss.tools.intellij.settings.ApiSettingsState;

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
        MANIFEST, ECOSYSTEM, PLATFORM, RHDA_TOKEN;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private final Api exhortApi;

    static {
        System.setProperty("EXHORT_DEV_MODE","true");
    }

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
        telemetryMsg.property(TelemetryKeys.RHDA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);

        try {
            setRequestProperties(manifestName);
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
        telemetryMsg.property(TelemetryKeys.RHDA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);

        try {
            setRequestProperties(manifestName);
            CompletableFuture<AnalysisReport> componentReport;
            if ("go.mod".equals(manifestName) || "requirements.txt".equals(manifestName)) {
                var manifestContent = Files.readAllBytes(Paths.get(manifestPath));
                componentReport = exhortApi.componentAnalysis(manifestName, manifestContent);
            } else {
                componentReport = exhortApi.componentAnalysis(manifestPath);
            }
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

    private void setRequestProperties(final String manifestName) {
        String ideName = ApplicationInfo.getInstance().getFullApplicationName();
        PluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
        if (pluginDescriptor != null) {
            String pluginName = pluginDescriptor.getName() + " " + pluginDescriptor.getVersion();
            System.setProperty("RHDA_SOURCE", ideName + " / " + pluginName);
        } else {
            System.setProperty("RHDA_SOURCE", ideName);
        }

        ApiSettingsState settings = ApiSettingsState.getInstance();
        System.setProperty("RHDA_TOKEN", settings.rhdaToken);

        if (settings.mvnPath != null && !settings.mvnPath.isBlank()) {
            System.setProperty("EXHORT_MVN_PATH", settings.mvnPath);
        } else {
            System.clearProperty("EXHORT_MVN_PATH");
        }
        if (settings.javaPath != null && !settings.javaPath.isBlank()) {
            System.setProperty("JAVA_HOME", settings.javaPath);
        } else {
            System.clearProperty("JAVA_HOME");
        }
        if (settings.npmPath != null && !settings.npmPath.isBlank()) {
            System.setProperty("EXHORT_NPM_PATH", settings.npmPath);
        } else {
            System.clearProperty("EXHORT_NPM_PATH");
        }
        if (settings.nodePath != null && !settings.nodePath.isBlank()) {
            System.setProperty("NODE_HOME", settings.nodePath);
        } else {
            System.clearProperty("NODE_HOME");
        }
        if (settings.goPath != null && !settings.goPath.isBlank()) {
            System.setProperty("EXHORT_GO_PATH", settings.goPath);
        } else {
            System.clearProperty("EXHORT_GO_PATH");
        }
        if ("go.mod".equals(manifestName)) {
            if (settings.goMatchManifestVersions) {
                System.setProperty("MATCH_MANIFEST_VERSIONS", "true");
            } else {
                System.clearProperty("MATCH_MANIFEST_VERSIONS");
            }
        }
        if (settings.usePython2) {
            if (settings.pythonPath != null && !settings.pythonPath.isBlank()) {
                System.setProperty("EXHORT_PYTHON_PATH", settings.pythonPath);
            } else {
                System.clearProperty("EXHORT_PYTHON_PATH");
            }
            if (settings.pipPath != null && !settings.pipPath.isBlank()) {
                System.setProperty("EXHORT_PIP_PATH", settings.pipPath);
            } else {
                System.clearProperty("EXHORT_PIP_PATH");
            }
            System.clearProperty("EXHORT_PYTHON3_PATH");
            System.clearProperty("EXHORT_PIP3_PATH");
        } else {
            if (settings.pythonPath != null && !settings.pythonPath.isBlank()) {
                System.setProperty("EXHORT_PYTHON3_PATH", settings.pythonPath);
            } else {
                System.clearProperty("EXHORT_PYTHON3_PATH");
            }
            if (settings.pipPath != null && !settings.pipPath.isBlank()) {
                System.setProperty("EXHORT_PIP3_PATH", settings.pipPath);
            } else {
                System.clearProperty("EXHORT_PIP3_PATH");
            }
            System.clearProperty("EXHORT_PYTHON_PATH");
            System.clearProperty("EXHORT_PIP_PATH");
        }
        if (settings.usePythonVirtualEnv) {
            System.setProperty("EXHORT_PYTHON_VIRTUAL_ENV", "true");
            if (settings.pythonInstallBestEfforts) {
                System.setProperty("EXHORT_PYTHON_INSTALL_BEST_EFFORTS", "true");
            } else {
                System.clearProperty("EXHORT_PYTHON_INSTALL_BEST_EFFORTS");
            }
        } else {
            System.clearProperty("EXHORT_PYTHON_VIRTUAL_ENV");
            System.clearProperty("EXHORT_PYTHON_INSTALL_BEST_EFFORTS");
        }
        if ("requirements.txt".equals(manifestName)) {
            if (settings.pythonMatchManifestVersions) {
                System.setProperty("MATCH_MANIFEST_VERSIONS", "true");
            } else {
                System.clearProperty("MATCH_MANIFEST_VERSIONS");
            }
        }
        if (!"go.mod".equals(manifestName) && !"requirements.txt".equals(manifestName)) {
            System.clearProperty("MATCH_MANIFEST_VERSIONS");
        }
        if (settings.snykToken != null && !settings.snykToken.isBlank()) {
            System.setProperty("EXHORT_SNYK_TOKEN", settings.snykToken);
        } else {
            System.clearProperty("EXHORT_SNYK_TOKEN");
        }
    }
}
