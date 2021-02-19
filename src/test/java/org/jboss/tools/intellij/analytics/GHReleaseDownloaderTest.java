package org.jboss.tools.intellij.analytics;

import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class GHReleaseDownloaderTest {

  private GHReleaseDownloader downloader;

  @Before
  public void setup() throws IOException {
    final GHReleaseDownloader downloader = new GHReleaseDownloader("fabric8-analytics/fabric8-analytics-lsp-server");
    assertNotNull(downloader);
    this.downloader = downloader;
  }

  @After
  public void tearDown() {
    this.downloader = null;
  }

  @Test
  public void testLatestDownloadUri() throws IOException {
    final String latestReleaseTag = this.downloader.getLatestRelease();
    assertNotNull(latestReleaseTag);

    final String uri = this.downloader.getDownloadUri(latestReleaseTag, "analytics-lsp-win.exe");
    assertNotNull(uri);
  }

  @Test(expected = IOException.class)
  public void testLatestDownloadUriException() throws IOException {
    final String latestReleaseTag = this.downloader.getLatestRelease();
    assertNotNull(latestReleaseTag);

    final String uri = this.downloader.getDownloadUri(latestReleaseTag, "bad tag name");
    assertNotNull(uri);
  }
}
