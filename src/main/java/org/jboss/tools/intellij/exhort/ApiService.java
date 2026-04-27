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
import com.intellij.util.net.ProxyConfiguration;
import com.intellij.util.net.ProxySettings;
import io.github.guacsec.trustifyda.Api;
import io.github.guacsec.trustifyda.ComponentAnalysisResult;
import io.github.guacsec.trustifyda.impl.ExhortApi;
import org.jboss.tools.intellij.settings.ApiSettingsState;
import org.jboss.tools.intellij.settings.MavenSettingsUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service(Service.Level.PROJECT)
public final class ApiService {

    private static final Logger LOG = Logger.getInstance(ApiService.class);
    private static final String TRUSTIFY_DA_BACKEND_URL_PROPERTY = "TRUSTIFY_DA_BACKEND_URL";
    private static final String RHDA_BACKEND_URL = "https://rhda.rhcloud.com";

    enum TelemetryKeys {
        MANIFEST, ECOSYSTEM, PLATFORM, TRUST_DA_TOKEN;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    private final Api exhortApi;

    public ApiService() {
        this(createExhortApiWithBackendUrl());
    }

    public static Api createExhortApiWithBackendUrl() {
        setBackendUrl();
        return new ExhortApi();
    }

    ApiService(Api exhortApi) {
        this.exhortApi = exhortApi;
    }

    public Path getStackAnalysis(final String packageManager, final String manifestName, final String manifestPath) {
        var telemetryMsg = TelemetryService.instance().action("stack-analysis");
        telemetryMsg.property(TelemetryKeys.ECOSYSTEM.toString(), packageManager);
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.MANIFEST.toString(), manifestName);
        telemetryMsg.property(TelemetryKeys.TRUST_DA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);

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

    /**
     * Performs batch stack analysis on all workspace members in the given directory,
     * returning the path to a temporary HTML report file.
     *
     * @param workspacePath the workspace root directory path
     * @param ignorePatterns glob patterns for paths to exclude from workspace discovery
     * @return the path to the generated HTML report file
     */
    public Path getBatchStackAnalysis(final String workspacePath, final List<String> ignorePatterns) {
        var telemetryMsg = TelemetryService.instance().action("batch-stack-analysis");
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.TRUST_DA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);

        try {
            setBatchRequestProperties();
            Set<String> ignorePatternsSet = new HashSet<>(ignorePatterns);
            var htmlContent = exhortApi.stackAnalysisBatchHtml(Path.of(workspacePath), ignorePatternsSet);
            var tmpFile = Files.createTempFile("exhort_batch_", ".html");
            Files.write(tmpFile, htmlContent.get());

            telemetryMsg.send();
            return tmpFile;

        } catch (IOException | InterruptedException | ExecutionException exc) {
            telemetryMsg.error(exc);
            telemetryMsg.send();
            throw new RuntimeException(exc);
        }
    }

    public ComponentAnalysisResult getComponentAnalysis(final String packageManager, final String manifestName, final String manifestPath) {
        var telemetryMsg = TelemetryService.instance().action("component-analysis");
        telemetryMsg.property(TelemetryKeys.ECOSYSTEM.toString(), packageManager);
        telemetryMsg.property(TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(TelemetryKeys.MANIFEST.toString(), manifestName);
        telemetryMsg.property(TelemetryKeys.TRUST_DA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);

        try {
            setRequestProperties(manifestName);
            CompletableFuture<ComponentAnalysisResult> componentReport =
                    exhortApi.componentAnalysisWithLicense(manifestPath);
            ComponentAnalysisResult result = componentReport.get();
            telemetryMsg.send();
            return result;
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

    /**
     * Sets up common request properties shared across all analysis types (stack, batch, image).
     * Configures the plugin descriptor, backend connection, all tool paths, and proxy settings.
     * Not all analysis types use every property configured here (e.g., batch analysis does not use
     * Maven/Gradle/Python-specific properties, and image analysis does not use any of the package
     * manager tool paths), but setting them unconditionally is harmless as unused properties are
     * simply ignored by the backend.
     */
    public static void setCommonRequestProperties() {
        String ideName = ApplicationInfo.getInstance().getFullApplicationName();
        PluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
        if (pluginDescriptor != null) {
            String pluginName = pluginDescriptor.getName() + " " + pluginDescriptor.getVersion();
            System.setProperty("TRUST_DA_SOURCE", ideName + " / " + pluginName);
        } else {
            System.setProperty("TRUST_DA_SOURCE", ideName);
        }

        ApiSettingsState settings = ApiSettingsState.getInstance();
        System.setProperty("TRUST_DA_TOKEN", settings.rhdaToken);

        setBackendUrl();

        if (settings.mvnPath != null && !settings.mvnPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_MVN_PATH", settings.mvnPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_MVN_PATH");
        }

        if (settings.useMavenWrapper.equals("fallback")) {
            if (MavenSettingsUtil.isMavenWrapperSelected()) {
                System.setProperty("TRUSTIFY_DA_PREFER_MVNW", settings.useMavenWrapper);
            } else {
                System.clearProperty("TRUSTIFY_DA_PREFER_MVNW");
            }
        } else if (settings.useMavenWrapper.equals("true")) {
            System.setProperty("TRUSTIFY_DA_PREFER_MVNW", settings.useMavenWrapper);
        } else {
            System.clearProperty("TRUSTIFY_DA_PREFER_MVNW");
        }

        String userSettingsFile = MavenSettingsUtil.getUserSettingsFile();
        if (!userSettingsFile.isBlank()) {
            System.setProperty("TRUSTIFY_DA_MVN_USER_SETTINGS", userSettingsFile);
        } else {
            System.clearProperty("TRUSTIFY_DA_MVN_USER_SETTINGS");
        }

        String localRepository = MavenSettingsUtil.getLocalRepository();
        if (!localRepository.isBlank()) {
            System.setProperty("TRUSTIFY_DA_MVN_LOCAL_REPO", localRepository);
        } else {
            System.clearProperty("TRUSTIFY_DA_MVN_LOCAL_REPO");
        }

        if (settings.gradlePath != null && !settings.gradlePath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_GRADLE_PATH", settings.gradlePath);
        } else {
            System.clearProperty("TRUSTIFY_DA_GRADLE_PATH");
        }

        if (settings.javaPath != null && !settings.javaPath.isBlank()) {
            System.setProperty("JAVA_HOME", settings.javaPath);
        } else {
            System.clearProperty("JAVA_HOME");
        }
        if (settings.npmPath != null && !settings.npmPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_NPM_PATH", settings.npmPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_NPM_PATH");
        }
        if (settings.yarnPath != null && !settings.yarnPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_YARN_PATH", settings.yarnPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_YARN_PATH");
        }
        if (settings.nodePath != null && !settings.nodePath.isBlank()) {
            System.setProperty("NODE_HOME", settings.nodePath);
        } else {
            System.clearProperty("NODE_HOME");
        }
        if (settings.pnpmPath != null && !settings.pnpmPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_PNPM_PATH", settings.pnpmPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_PNPM_PATH");
        }
        if (settings.goPath != null && !settings.goPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_GO_PATH", settings.goPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_GO_PATH");
        }
        if (settings.cargoPath != null && !settings.cargoPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_CARGO_PATH", settings.cargoPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_CARGO_PATH");
        }
        if (settings.usePython2) {
            if (settings.pythonPath != null && !settings.pythonPath.isBlank()) {
                System.setProperty("TRUSTIFY_DA_PYTHON_PATH", settings.pythonPath);
            } else {
                System.clearProperty("TRUSTIFY_DA_PYTHON_PATH");
            }
            if (settings.pipPath != null && !settings.pipPath.isBlank()) {
                System.setProperty("TRUSTIFY_DA_PIP_PATH", settings.pipPath);
            } else {
                System.clearProperty("TRUSTIFY_DA_PIP_PATH");
            }
            System.clearProperty("TRUSTIFY_DA_PYTHON3_PATH");
            System.clearProperty("TRUSTIFY_DA_PIP3_PATH");
        } else {
            if (settings.pythonPath != null && !settings.pythonPath.isBlank()) {
                System.setProperty("TRUSTIFY_DA_PYTHON3_PATH", settings.pythonPath);
            } else {
                System.clearProperty("TRUSTIFY_DA_PYTHON3_PATH");
            }
            if (settings.pipPath != null && !settings.pipPath.isBlank()) {
                System.setProperty("TRUSTIFY_DA_PIP3_PATH", settings.pipPath);
            } else {
                System.clearProperty("TRUSTIFY_DA_PIP3_PATH");
            }
            System.clearProperty("TRUSTIFY_DA_PYTHON_PATH");
            System.clearProperty("TRUSTIFY_DA_PIP_PATH");
        }
        if (settings.uvPath != null && !settings.uvPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_UV_PATH", settings.uvPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_UV_PATH");
        }
        if (settings.usePythonVirtualEnv) {
            System.setProperty("TRUSTIFY_DA_PYTHON_VIRTUAL_ENV", "true");
            if (settings.pythonInstallBestEfforts) {
                System.setProperty("TRUSTIFY_DA_PYTHON_INSTALL_BEST_EFFORTS", "true");
            } else {
                System.clearProperty("TRUSTIFY_DA_PYTHON_INSTALL_BEST_EFFORTS");
            }
        } else {
            System.clearProperty("TRUSTIFY_DA_PYTHON_VIRTUAL_ENV");
            System.clearProperty("TRUSTIFY_DA_PYTHON_INSTALL_BEST_EFFORTS");
        }
        if (settings.licenseCheckEnabled) {
            System.setProperty("TRUSTIFY_DA_LICENSE_CHECK", "true");
        } else {
            System.setProperty("TRUSTIFY_DA_LICENSE_CHECK", "false");
        }

        Optional<String> proxyUrlOpt = getProxyUrl();
        if (proxyUrlOpt.isPresent()) {
            System.setProperty("TRUSTIFY_DA_PROXY_URL", proxyUrlOpt.get());
        } else {
            System.clearProperty("TRUSTIFY_DA_PROXY_URL");
        }
    }

    private void setRequestProperties(final String manifestName) {
        setCommonRequestProperties();

        if ("go.mod".equals(manifestName)) {
            ApiSettingsState settings = ApiSettingsState.getInstance();
            if (settings.goMatchManifestVersions) {
                System.setProperty("MATCH_MANIFEST_VERSIONS", "true");
            } else {
                System.clearProperty("MATCH_MANIFEST_VERSIONS");
            }
        } else if ("requirements.txt".equals(manifestName)) {
            ApiSettingsState settings = ApiSettingsState.getInstance();
            if (settings.pythonMatchManifestVersions) {
                System.setProperty("MATCH_MANIFEST_VERSIONS", "true");
            } else {
                System.setProperty("MATCH_MANIFEST_VERSIONS", "false");
            }
        } else {
            System.clearProperty("MATCH_MANIFEST_VERSIONS");
        }
    }

    void setBatchRequestProperties() {
        setCommonRequestProperties();

        ApiSettingsState settings = ApiSettingsState.getInstance();
        if (settings.batchConcurrency != null && !settings.batchConcurrency.isBlank()) {
            System.setProperty("TRUSTIFY_DA_BATCH_CONCURRENCY", settings.batchConcurrency);
        } else {
            System.setProperty("TRUSTIFY_DA_BATCH_CONCURRENCY", "10");
        }
        System.setProperty("TRUSTIFY_DA_CONTINUE_ON_ERROR", String.valueOf(settings.batchContinueOnError));
        System.setProperty("TRUSTIFY_DA_BATCH_METADATA", String.valueOf(settings.batchMetadata));
    }

    public static void setBackendUrl() {
        String backendUrl = System.getenv(TRUSTIFY_DA_BACKEND_URL_PROPERTY);
        if (backendUrl == null || backendUrl.isBlank()) {
            backendUrl = RHDA_BACKEND_URL;
        }
        System.setProperty(TRUSTIFY_DA_BACKEND_URL_PROPERTY, backendUrl);
    }

    public static Optional<String> getProxyUrl() {
        // This API only works in 2024.2+ versions.
        ProxyConfiguration proxyConfiguration = ProxySettings.getInstance().getProxyConfiguration();

        if (proxyConfiguration instanceof ProxyConfiguration.StaticProxyConfiguration staticProxyConfiguration) {

            String protocol = staticProxyConfiguration.getProtocol().toString().toLowerCase(); // e.g., "http" or "socks"
            String host = staticProxyConfiguration.getHost();
            int port = staticProxyConfiguration.getPort();

            if (host != null && !host.isBlank() && port > 0 && protocol.equals("http")) {
                String proxyUrl = protocol + "://" + host.trim() + ":" + port;
                return Optional.of(proxyUrl);
            }
        }

        return Optional.empty();
    }
}
