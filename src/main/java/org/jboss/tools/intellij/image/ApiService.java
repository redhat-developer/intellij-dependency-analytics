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
import com.redhat.exhort.Api;
import com.redhat.exhort.api.v4.AnalysisReport;
import com.redhat.exhort.image.ImageRef;
import com.redhat.exhort.impl.ExhortApi;
import org.jboss.tools.intellij.exhort.TelemetryService;
import org.jboss.tools.intellij.settings.ApiSettingsState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(Service.Level.APP)
public final class ApiService {

    private static final Logger LOG = Logger.getInstance(ApiService.class);

    private final Api exhortApi;

    public ApiService() {
        this.exhortApi = new ExhortApi();
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
        telemetryMsg.property("rhda_token", ApiSettingsState.getInstance().rhdaToken);

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
        telemetryMsg.property("rhda_token", ApiSettingsState.getInstance().rhdaToken);

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
            System.setProperty("RHDA_SOURCE", ideName + " / " + pluginName);
        } else {
            System.setProperty("RHDA_SOURCE", ideName);
        }

        var settings = ApiSettingsState.getInstance();
        System.setProperty("RHDA_TOKEN", settings.rhdaToken);

        if (settings.syftPath != null && !settings.syftPath.isBlank()) {
            System.setProperty("EXHORT_SYFT_PATH", settings.syftPath);
        } else {
            System.clearProperty("EXHORT_SYFT_PATH");
        }

        if (settings.syftConfigPath != null && !settings.syftConfigPath.isBlank()) {
            System.setProperty("EXHORT_SYFT_CONFIG_PATH", settings.syftConfigPath);
        } else {
            System.clearProperty("EXHORT_SYFT_CONFIG_PATH");
        }

        if (settings.skopeoPath != null && !settings.skopeoPath.isBlank()) {
            System.setProperty("EXHORT_SKOPEO_PATH", settings.skopeoPath);
        } else {
            System.clearProperty("EXHORT_SKOPEO_PATH");
        }

        if (settings.skopeoConfigPath != null && !settings.skopeoConfigPath.isBlank()) {
            System.setProperty("EXHORT_SKOPEO_CONFIG_PATH", settings.skopeoConfigPath);
        } else {
            System.clearProperty("EXHORT_SKOPEO_CONFIG_PATH");
        }

        if (settings.dockerPath != null && !settings.dockerPath.isBlank()) {
            System.setProperty("EXHORT_DOCKER_PATH", settings.dockerPath);
        } else {
            System.clearProperty("EXHORT_DOCKER_PATH");
        }

        if (settings.podmanPath != null && !settings.podmanPath.isBlank()) {
            System.setProperty("EXHORT_PODMAN_PATH", settings.podmanPath);
        } else {
            System.clearProperty("EXHORT_PODMAN_PATH");
        }

        if (settings.imagePlatform != null && !settings.imagePlatform.isBlank()) {
            System.setProperty("EXHORT_IMAGE_PLATFORM", settings.imagePlatform);
        } else {
            System.clearProperty("EXHORT_IMAGE_PLATFORM");
        }
    }
}
