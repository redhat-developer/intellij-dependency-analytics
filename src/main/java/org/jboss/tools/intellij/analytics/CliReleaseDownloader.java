package org.jboss.tools.intellij.analytics;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.io.HttpRequests;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;

import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;


public final class CliReleaseDownloader {
  private static Logger LOGGER = LoggerFactory.getLogger(CliReleaseDownloader.class);
  private final IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
  private String tarBallName;
  private final String binaryName;
  private final String cliReleaseTag;
  private final ICookie cookies;
  private final GitHubRelease release;

  public CliReleaseDownloader(final String tarBallName, final String binaryName, final String cliReleaseTag, final ICookie cookies, final String repoName) throws IOException {
    this.tarBallName = tarBallName;
    this.binaryName = binaryName;
    this.cliReleaseTag = cliReleaseTag;
    this.cookies = cookies;
    this.release = new GitHubRelease(repoName);
  }

  private boolean isNewRelease(final String releaseLabel) {
    final String currentVersion = cookies.getValue(ICookie.Name.CLIVersion);
    return !releaseLabel.equals(currentVersion);
  }

  public File download(final ProgressIndicator indicator) throws IOException {
    File sandBox = descriptor.getPath();
    final File tarBallDest = new File(sandBox, tarBallName);
    final File binaryDest = new File(sandBox, binaryName);

    // If tarball is present in system then assuming its already been extracted
    // and using existing binary
    if (tarBallDest.exists()) {
      return binaryDest;
    }

    final ActionMessage telemetry = TelemetryService.instance()
            .action("cli:download").property("cliVersion", cliReleaseTag);
    try {
      // Download the given tarball from Github repo
      final String url = this.release.getDownloadUri(cliReleaseTag, tarBallName);
      HttpRequests
        .request(url)
        .productNameAsUserAgent()
        .saveToFile(tarBallDest, indicator);
      LOGGER.warn("downloaded CLI tarball.");

      // Extract the tarball
      unTarBundle(sandBox);
      LOGGER.warn("extracted CLI tarball.");

      binaryDest.setExecutable(true);
      cookies.setValue(ICookie.Name.CLIVersion, cliReleaseTag);
      telemetry.send();

      return binaryDest;
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
      telemetry.error(e).send();
      LOGGER.warn(e.getLocalizedMessage(), e);
      throw e;
    }
  }


  private void unTarBundle(File sandBox) throws IOException {
    TarArchiveInputStream tarArchiveInputStream = null;

    try {
      FileInputStream fileInputStream = new FileInputStream(
              sandBox.getAbsolutePath() + File.separator + tarBallName);
      GZIPInputStream gzipInputStream = new GZIPInputStream(
              new BufferedInputStream(fileInputStream));
      tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
      TarArchiveEntry tarEntry = null;

      while ((tarEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
        File outputFile = new File(sandBox + File.separator + tarEntry.getName());
        outputFile.getParentFile().mkdirs();
        IOUtils.copy(tarArchiveInputStream, new FileOutputStream(outputFile));
      }
    }catch(IOException e) {
      LOGGER.error(e.getMessage());
      throw e;
    }finally {
      if(tarArchiveInputStream != null) {
        try {
          tarArchiveInputStream.close();
        } catch (IOException e) {
          LOGGER.error(e.getMessage());
          throw e;
        }
      }
    }
  }
}
