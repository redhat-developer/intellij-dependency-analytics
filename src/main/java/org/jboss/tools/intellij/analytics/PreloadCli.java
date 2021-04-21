package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;

import java.io.File;
import java.io.IOException;

public final class PreloadCli extends PreloadingActivity {
  private static final Logger log = Logger.getInstance(PreloadCli.class);
  private final ICookie cookies = ServiceManager.getService(Settings.class);

  @Override
  public void preload(ProgressIndicator indicator) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    log.warn("cli preload is called");

    try {
      final String devUrl = System.getenv("CLI_FILE_PATH");
      //CLI Binary file will be used to run system process.
      File cliBundle;
      if (devUrl != null) {
        cliBundle = new File(devUrl);
      } else {
        final CliReleaseDownloader bundle = new CliReleaseDownloader(Cli.current.cliTarBallName,
                Cli.current.cliBinaryName, Cli.current.cliReleaseTag, cookies, "fabric8-analytics/cli-tools");
        cliBundle = bundle.download(indicator);
      }
    } catch(IOException ex) {
      log.info("cli download fail");
    }
  }
}
