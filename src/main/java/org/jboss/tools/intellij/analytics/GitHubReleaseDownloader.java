package org.jboss.tools.intellij.analytics;

import java.io.File;
import java.io.IOException;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GitHubReleaseDownloader {
  private static Logger LOGGER = LoggerFactory.getLogger(GitHubReleaseDownloader.class);
  private final IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
  private final String fileName;
  private final ICookie cookies;
  private final GitHubRelease release;

  public GitHubReleaseDownloader(final String fileName, final ICookie cookies) throws IOException {
    this.fileName = fileName;
    this.cookies = cookies;
    this.release = new GitHubRelease("fabric8-analytics/fabric8-analytics-lsp-server");
  }

  private boolean isNewRelease(final String releaseLabel) {
    final String currentVersion = cookies.getValue(ICookie.Name.LSPVersion);
    return !releaseLabel.equals(currentVersion);
  }

  public File download(final ProgressIndicator indicator) throws IOException {
    final File dest = new File(descriptor.getPath(), fileName);
    final String latestReleaseTag = this.release.getLatestRelease();
    if (!isNewRelease(latestReleaseTag) && dest.exists()) {
      return dest;
    }
    final ActionMessage telemetry = TelemetryService.instance().action("lsp:download").property("lspVersion", latestReleaseTag);
    try {
      final String url = this.release.getDownloadUri(latestReleaseTag, this.fileName);
      HttpRequests
        .request(url)
        .productNameAsUserAgent()
        .saveToFile(dest, indicator);

      dest.setExecutable(true);
      cookies.setValue(ICookie.Name.LSPVersion, latestReleaseTag);
      telemetry.send();
      return dest;
    } catch (IOException e) {
      telemetry.error(e).send();
      LOGGER.warn(e.getLocalizedMessage(), e);
      throw e;
    }
  }
}
