package org.jboss.tools.intellij.analytics;

import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class GithubReleaseTest {

  private GithubRelease release;

  @Before
  public void setup() throws IOException {
    final GithubRelease release = new GithubRelease("fabric8-analytics/fabric8-analytics-lsp-server");
    assertNotNull(release);
    this.release = release;
  }

  @After
  public void tearDown() {
    this.release = null;
  }

  @Test
  public void testLatestDownloadUri() throws IOException {
    final String latestReleaseTag = this.release.getLatestRelease();
    assertNotNull(latestReleaseTag);

    final String uri = this.release.getDownloadUri(latestReleaseTag, "analytics-lsp-win.exe");
    assertNotNull(uri);
  }

  @Test(expected = IOException.class)
  public void testLatestDownloadUriException() throws IOException {
    final String latestReleaseTag = this.release.getLatestRelease();
    assertNotNull(latestReleaseTag);

    final String uri = this.release.getDownloadUri(latestReleaseTag, "bad tag name");
    assertNotNull(uri);
  }
}
