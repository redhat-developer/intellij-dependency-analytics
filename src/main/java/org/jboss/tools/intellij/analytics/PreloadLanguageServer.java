package org.jboss.tools.intellij.analytics;

import java.io.File;
import java.io.IOException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.components.ServiceManager;
import org.wso2.lsp4intellij.IntellijLanguageClient;

public final class PreloadLanguageServer extends PreloadingActivity {
  private static final Logger log = Logger.getInstance(PreloadLanguageServer.class);
  private final ICookie cookies = ServiceManager.getService(Settings.class);

  private void attachLanguageClient(final File cliFile) {
    final String[] EXTENSIONS = {"xml", "json", "txt"};
    final String[] cmds = {cliFile.toString(), "--stdio"};
    ApplicationManager.getApplication().invokeAndWait(() -> {
      for (String ext : EXTENSIONS) {
        AnalyticsLanguageServerDefinition serverDefinition = new AnalyticsLanguageServerDefinition(ext, cmds);
        IntellijLanguageClient.addServerDefinition(serverDefinition);
        IntellijLanguageClient.addExtensionManager(ext, serverDefinition);
      }
    });
    log.warn(String.format("lsp registration done %s", cliFile));
  }

  @Override
  public void preload(ProgressIndicator indicator) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    log.debug("lsp preload called");
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
        lspBundle = bundle.download(indicator);
      }
      attachLanguageClient(lspBundle);
    } catch(IOException ex) {
      log.warn("lsp download fail", ex);
    }
  }
}
