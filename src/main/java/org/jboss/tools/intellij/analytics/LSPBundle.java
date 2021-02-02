package org.jboss.tools.intellij.analytics;

import java.io.File;
import java.io.IOException;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.io.HttpRequests;

import org.wso2.lsp4intellij.IntellijLanguageClient;

@Service
public final class LSPBundle {
  private final IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
  private final GHReleaseDownloader downloader;
  private File cliFile;
  private final BackgroundTaskQueue taskQueue;
  private final Project project;

  private static final Logger log = Logger.getInstance(LSPBundle.class);

  public LSPBundle(final Project project) {
    this.project = project;
    try {
      this.downloader = new GHReleaseDownloader("fabric8-analytics/fabric8-analytics-lsp-server");
    } catch (IOException ex) {
      throw new LSPBundleException(ex.toString());
    }
    this.cliFile = new File(descriptor.getPath(), Platform.current.lspBundleName);
    this.taskQueue = new BackgroundTaskQueue(project, "Analytics");
  }

  public File getCliPath() {
    return this.cliFile;
  }

  private void addLSPDefinitions() {
    final String[] EXTENSIONS = {"xml", "json", "txt"};
    final String[] cmds = {this.cliFile.toString(), "--stdio"};
    ApplicationManager.getApplication().invokeAndWait(() -> {
      for(String ext : EXTENSIONS) {
        AnalyticsLanguageServerDefinition serverDefinition = new AnalyticsLanguageServerDefinition(ext, cmds);
        IntellijLanguageClient.addServerDefinition(serverDefinition);
        IntellijLanguageClient.addExtensionManager(ext, serverDefinition);
      }
    });
  }
  public void download() {
    log.debug("download lsp bundle");
    taskQueue.run(new Task.Backgroundable(this.project, "Analytics cli download", true) {
      @Override
      public void run(final ProgressIndicator indicator) {
        try {
          log.info("downloading cli " + LSPBundle.this.cliFile);
          downloadIfNeeded(indicator);
        } catch(IOException ex) {
          log.warn("failed to download cli " + LSPBundle.this.cliFile);
          return;
        }
        addLSPDefinitions();
      }
    });
  }

  private boolean isNewRelease(final String releaseLabel) {
    final String currentVersion = ServiceManager.getService(AnalyticsPersistentSettings.class).getLSPVersion();
    log.info(String.format("lsp version current %s, latest %s", currentVersion, releaseLabel));
    return !releaseLabel.equals(currentVersion);
  }

  public void downloadIfNeeded(final ProgressIndicator indicator) throws IOException {
    final String latestReleaseTag = this.downloader.getLatestRelease();
    if (!isNewRelease(latestReleaseTag) && cliFile.exists()) {
      return;
    }
    final String url = this.downloader.getDownloadUri(latestReleaseTag, Platform.current.lspBundleName);
    HttpRequests
      .request(url)
      // .request("file:///tmp/analytics-lsp-macos")
      .productNameAsUserAgent()
      .saveToFile(cliFile, indicator);

    cliFile.setExecutable(true);
    ServiceManager.getService(AnalyticsPersistentSettings.class).setLSPVersion(latestReleaseTag);
  }
}
