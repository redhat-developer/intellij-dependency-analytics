package org.jboss.tools.intellij.analytics;

import java.io.File;
import java.io.IOException;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.IntellijLanguageClient;

public final class PreloadLanguageServer implements StartupActivity.Background {
  private static final Logger log = Logger.getInstance(PreloadLanguageServer.class);
  private final ICookie cookies = ServiceManager.getService(Settings.class);

  private void attachLanguageClient(final File cliFile) {
    final String[] cmds = {cliFile.toString(), "--stdio"};
    ApplicationManager.getApplication().invokeAndWait(() -> {
      Platform.supportedManifestFiles.stream().map(s -> s.substring(s.lastIndexOf('.') + 1)).distinct().forEach(ext -> {
        AnalyticsLanguageServerDefinition serverDefinition = new AnalyticsLanguageServerDefinition(ext, cmds);
        IntellijLanguageClient.addServerDefinition(serverDefinition);
        IntellijLanguageClient.addExtensionManager(ext, serverDefinition);}
      );
    });
    log.warn(String.format("lsp registration done %s", cliFile));
  }

  @Override
  public void runActivity(@NotNull Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    log.info("lsp preload called");

    try {
      final String devUrl = System.getenv("ANALYTICS_LSP_FILE_PATH");
      File lspBundle;
      if (devUrl != null) {
        lspBundle = new File(devUrl);
      } else {
        final GitHubReleaseDownloader bundle = new GitHubReleaseDownloader(
          Platform.current.lspBundleName,
          cookies,
          "fabric8-analytics/fabric8-analytics-lsp-server",
          false);
        lspBundle = bundle.download();

        log.info("lsp binary is ready for use.");
      }
      attachLanguageClient(lspBundle);
    } catch(IOException ex) {
      log.warn("lsp download fail", ex);
    }
  }
}
