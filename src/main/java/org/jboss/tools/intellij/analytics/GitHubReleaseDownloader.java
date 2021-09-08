package org.jboss.tools.intellij.analytics;

import java.io.File;
import java.io.IOException;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import com.intellij.openapi.diagnostic.Logger;
import org.jboss.tools.intellij.stackanalysis.Cli;


public class GitHubReleaseDownloader {
  private static final Logger logger = Logger.getInstance(GitHubReleaseDownloader.class);
  private final String fileName;
  private final ICookie cookies;
  private final GitHubRelease release;
  private final boolean forCli;
  private ICookie.Name cookieName; // Set name according to LSP/CLI calls

  public GitHubReleaseDownloader(final String fileName, final ICookie cookies, final String repoName, final boolean forCli) throws IOException {
    this.fileName = fileName;
    this.cookies = cookies;
    this.release = new GitHubRelease(repoName);
    this.forCli=forCli; // False for LSP download, True for CLI
  }

  private boolean isNewRelease(final String releaseLabel) {
    if (forCli) {
      this.cookieName = ICookie.Name.CLIVersion;
    } else {
      this.cookieName = ICookie.Name.LSPVersion;
    }
    final String currentVersion = cookies.getValue(cookieName);
    return !releaseLabel.equals(currentVersion);
  }


  public File download(final ProgressIndicator indicator) throws IOException {
    final ActionMessage telemetry;
    final String latestReleaseTag;
    final File dest = new File(Platform.pluginDirectory, fileName);
    String telemetryAction;
    String telemetryProperty;

    if (forCli){
      // CLI Release version is pinned to a latest stable version
      // to avoid issues due to ongoing development of CLI
      latestReleaseTag = Cli.current.cliReleaseTag;

      // Set telemetry action and property for CLI.
      telemetryAction = "cli:download";
      telemetryProperty = "cliVersion";
    } else {
      // Get latest LSP release version from repo
      latestReleaseTag = this.release.getLatestRelease();

      // Set telemetry action and property for LSP.
      telemetryAction = "lsp:download";
      telemetryProperty = "lspVersion";
    }

    if (!isNewRelease(latestReleaseTag) && dest.exists()) {
      return dest;
    }

    telemetry = TelemetryService.instance().action(telemetryAction)
            .property(telemetryProperty, latestReleaseTag);

    try {
      final String url = this.release.getDownloadUri(latestReleaseTag, this.fileName);
      HttpRequests
        .request(url)
        .productNameAsUserAgent()
        .saveToFile(dest, indicator);

      dest.setExecutable(true);
      cookies.setValue(cookieName, latestReleaseTag);
      telemetry.send();
      return dest;
    } catch (IOException e) {
      telemetry.error(e).send();
      logger.warn(e.getLocalizedMessage(), e);
      throw e;
    }
  }
}
