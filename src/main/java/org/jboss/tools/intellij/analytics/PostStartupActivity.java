package org.jboss.tools.intellij.analytics;

import java.io.File;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.diagnostic.Logger;
import org.wso2.lsp4intellij.IntellijLanguageClient;

public final class PostStartupActivity implements StartupActivity {
  private static final Logger log = Logger.getInstance(PostStartupActivity.class);

  @Override
  public void runActivity(Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    final LSPBundle.DownloadListener fileDownloadListener = (final File cliFile) -> {
      final String[] EXTENSIONS = {"xml", "json", "txt"};
      final String[] cmds = {cliFile.toString(), "--stdio"};
      for(String ext : EXTENSIONS) {
        AnalyticsLanguageServerDefinition serverDefinition = new AnalyticsLanguageServerDefinition(ext, cmds);
        IntellijLanguageClient.addServerDefinition(serverDefinition);
        IntellijLanguageClient.addExtensionManager(ext, serverDefinition);
      }
      log.debug("lsp registration done {0}", cliFile);
    };

    log.debug("runActivity called for analytics with project {0}", project);
    project.getService(LSPBundle.class).download(fileDownloadListener);
  }
}
