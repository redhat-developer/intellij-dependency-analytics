/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.image;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import io.github.guacsec.trustifyda.Api;
import io.github.guacsec.trustifyda.api.v5.AnalysisReport;
import io.github.guacsec.trustifyda.image.ImageRef;
import org.jboss.tools.intellij.exhort.TelemetryService;
import org.jboss.tools.intellij.settings.ApiSettingsState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.tools.intellij.exhort.ApiService.createExhortApiWithBackendUrl;
import static org.jboss.tools.intellij.exhort.ApiService.getProxyUrl;
import static org.jboss.tools.intellij.exhort.ApiService.setBackendUrl;

@Service(Service.Level.APP)
public final class ApiService {

    private static final Logger LOG = Logger.getInstance(ApiService.class);

    private final Api exhortApi;

    public ApiService() {
        this.exhortApi = createExhortApiWithBackendUrl();
    }

    static ApiService getInstance() {
        return ApplicationManager.getApplication().getService(ApiService.class);
    }

    Map<BaseImage, ImageRef> getImageRefs(final Collection<BaseImage> images) {
        setServiceEnvironment();
        return images.stream().collect(Collectors.toMap(
                Function.identity(),
                image -> new ImageRef(image.getImageName(), image.getPlatform())
        ));
    }

    Map<ImageRef, AnalysisReport> getImageAnalysis(final Set<ImageRef> imageRefs) {
        var telemetryMsg = TelemetryService.instance().action("image-analysis");
        telemetryMsg.property("ecosystem", "image");
        telemetryMsg.property("platform", System.getProperty("os.name"));
        telemetryMsg.property("images", String.join(";", imageRefs.toString()));
        telemetryMsg.property("trust_da_token", ApiSettingsState.getInstance().rhdaToken);

        try {
            setServiceEnvironment();
            var imageReports = exhortApi.imageAnalysis(imageRefs);
            var reports = imageReports.get();
            telemetryMsg.send();
            return reports;
        } catch (IOException | InterruptedException | ExecutionException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            LOG.warn("Invalid image reference submitted.", ex);
        } catch (CompletionException ex) {
            telemetryMsg.error(ex);
            telemetryMsg.send();
            LOG.warn("Invalid vulnerability report returned.", ex);
        }
        return null;
    }

    Path getImageAnalysisReport(final Set<ImageRef> imageRefs) {
        var telemetryMsg = TelemetryService.instance().action("image-analysis-report");
        telemetryMsg.property("ecosystem", "image");
        telemetryMsg.property("platform", System.getProperty("os.name"));
        telemetryMsg.property("images", String.join(";", imageRefs.toString()));
        telemetryMsg.property("trust_da_token", ApiSettingsState.getInstance().rhdaToken);

        try {
            setServiceEnvironment();
            var htmlContent = exhortApi.imageAnalysisHtml(imageRefs);
            var tmpFile = Files.createTempFile("exhort_image_", ".html");
            Files.write(tmpFile, htmlContent.get());
            telemetryMsg.send();
            return tmpFile;
        } catch (IOException | InterruptedException | ExecutionException exc) {
            telemetryMsg.error(exc);
            telemetryMsg.send();
            throw new RuntimeException(exc);
        }
    }

    private void setServiceEnvironment() {
        var ideName = ApplicationInfo.getInstance().getFullApplicationName();
        PluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
        if (pluginDescriptor != null) {
            var pluginName = pluginDescriptor.getName() + " " + pluginDescriptor.getVersion();
            System.setProperty("TRUST_DA_SOURCE", ideName + " / " + pluginName);
        } else {
            System.setProperty("TRUST_DA_SOURCE", ideName);
        }

        var settings = ApiSettingsState.getInstance();
        System.setProperty("TRUST_DA_TOKEN", settings.rhdaToken);

        setBackendUrl();

        if (settings.syftPath != null && !settings.syftPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_SYFT_PATH", settings.syftPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_SYFT_PATH");
        }

        if (settings.syftConfigPath != null && !settings.syftConfigPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_SYFT_CONFIG_PATH", settings.syftConfigPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_SYFT_CONFIG_PATH");
        }

        if (settings.skopeoPath != null && !settings.skopeoPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_SKOPEO_PATH", settings.skopeoPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_SKOPEO_PATH");
        }

        if (settings.skopeoConfigPath != null && !settings.skopeoConfigPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_SKOPEO_CONFIG_PATH", settings.skopeoConfigPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_SKOPEO_CONFIG_PATH");
        }

        if (settings.dockerPath != null && !settings.dockerPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_DOCKER_PATH", settings.dockerPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_DOCKER_PATH");
        }

        if (settings.podmanPath != null && !settings.podmanPath.isBlank()) {
            System.setProperty("TRUSTIFY_DA_PODMAN_PATH", settings.podmanPath);
        } else {
            System.clearProperty("TRUSTIFY_DA_PODMAN_PATH");
        }

        if (settings.imagePlatform != null && !settings.imagePlatform.isBlank()) {
            System.setProperty("TRUSTIFY_DA_IMAGE_PLATFORM", settings.imagePlatform);
        } else {
            System.clearProperty("TRUSTIFY_DA_IMAGE_PLATFORM");
        }

        Optional<String> proxyUrlOpt = getProxyUrl();
        if (proxyUrlOpt.isPresent()) {
            System.setProperty("TRUSTIFY_DA_PROXY_URL", proxyUrlOpt.get());
        } else {
            System.clearProperty("TRUSTIFY_DA_PROXY_URL");
        }
    }
}
