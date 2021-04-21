package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.util.SystemInfo;

public class Cli {
  public String cliBinaryName;
  public String cliTarBallName;
  public String cliReleaseTag;

  // Set name of tarball, cli binary  and release tag
  private static final Cli WINDOWS = new Cli("crda_0.1.2_Windows_64bit.tar.gz","crda.exe", "v0.1.2");
  private static final Cli LINUX = new Cli("crda_0.1.2_Linux_64bit.tar.gz","crda", "v0.1.2");
  private static final Cli MACOS = new Cli("crda_0.1.2_macOS_64bit.tar.gz","crda", "v0.1.2");

  private Cli(String cliTarBallName, String cliBinaryName, String cliReleaseTag) {
    this.cliBinaryName = cliBinaryName;
    this.cliTarBallName = cliTarBallName;
    this.cliReleaseTag = cliReleaseTag;
  }

  private static Cli detect() {
    if (SystemInfo.isLinux)
      return LINUX;
    if (SystemInfo.isWindows)
      return WINDOWS;
    if (SystemInfo.isMac)
      return MACOS;
    throw new PlatformDetectionException(SystemInfo.OS_NAME + " is not supported");
  }

  public static final Cli current = detect();
}
