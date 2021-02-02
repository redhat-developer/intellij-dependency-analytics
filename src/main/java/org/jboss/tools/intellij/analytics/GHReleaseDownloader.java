package org.jboss.tools.intellij.analytics;

import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRelease;
import java.io.IOException;

public class GHReleaseDownloader {
  private final GHRepository repo;

  public GHReleaseDownloader(final String repository) throws IOException {
    final GitHub github = GitHub.connectAnonymously();
    this.repo = github.getRepository(repository);
  }

  public String getLatestRelease() throws IOException {
    return this.repo.getLatestRelease().getTagName();
  }

  public String getDownloadUri(final String releaseLabel, final String fileLabel) throws IOException {
    final GHRelease release = this.repo.getReleaseByTagName(releaseLabel);
    final GHAsset asset = release.listAssets()
                                 .toList()
                                 .stream()
                                 .filter(a -> a.getLabel().equals(fileLabel))
                                 .findFirst()
                                 .orElseThrow(() -> new IOException(fileLabel + ": unable to download"));
    return asset.getBrowserDownloadUrl();
  }
}
