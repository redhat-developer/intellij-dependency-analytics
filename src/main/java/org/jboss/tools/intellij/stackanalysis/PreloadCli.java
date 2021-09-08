/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.stackanalysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jboss.tools.intellij.analytics.GitHubReleaseDownloader;
import org.jboss.tools.intellij.analytics.ICookie;
import org.jboss.tools.intellij.analytics.Platform;
import org.jboss.tools.intellij.analytics.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public final class PreloadCli extends PreloadingActivity {
  private static final Logger logger = Logger.getInstance(PreloadCli.class);
  private final ICookie cookies = ServiceManager.getService(Settings.class);

  /**
   * <p> Activity need to be performed when plugin/IDE is started.</p>
   *
   * When IDE is started or plugin is installed setup prerequisites for plugin.
   *
   * @param indicator An object of ProgressIndicator
   */
  @Override
  public void preload(@NotNull ProgressIndicator indicator) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    logger.info("CLI preload is called");

    try {
      // If Env variable is set then use binary file from value given
      final String cliPath = System.getenv("CLI_FILE_PATH");

      // If Env variable is not set download binary from GitHub Repo
      if (cliPath == null) {
        final GitHubReleaseDownloader bundle = new GitHubReleaseDownloader(
                Platform.current.cliTarBallName,
                cookies,
                "fabric8-analytics/cli-tools",
                true);

        // Download the CLI tarball
        bundle.download(indicator);

        // Extract tar file to get CLI Binary
        new SaUtils().unTarBundle(Platform.current.cliTarBallName, Cli.current.cliBinaryName);
        logger.info("CLI binary is ready for use.");
      }

      // Authenticate user
      new SaProcessExecutor().authenticateUser();
    } catch(IOException | InterruptedException e) {
      logger.warn(e);
    }
  }
}
