package org.jboss.tools.intellij.analytics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;


public class GitHubCLIReleaseTest {

  private GitHubRelease release;

  @Before
  public void setupCli() throws IOException {
    final GitHubRelease release = new GitHubRelease("fabric8-analytics/cli-tools");
    assertNotNull(release);
    this.release = release;
  }

  @After
  public void tearDown() {
    this.release = null;
  }

  @Test
  public void testCliDownloadUri() throws IOException {
    final String uri = this.release.getDownloadUri("v0.2.4", "crda_0.2.4_Windows_64bit.tar.gz");
    assertNotNull(uri);
  }

  @Test(expected = IOException.class)
  public void testCliDownloadUriException() throws IOException {
    final String uri = this.release.getDownloadUri("v0.2.4", "bad file name");
    assertNotNull(uri);
  }
}
