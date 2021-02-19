package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.util.SystemInfo;

public class Platform {
  public String lspBundleName;

  private static final Platform WINDOWS = new Platform("analytics-lsp-win.exe");
  private static final Platform LINUX = new Platform("analytics-lsp-linux");
  private static final Platform MACOS = new Platform("analytics-lsp-macos");

  private Platform(String lspBundleName) {
    this.lspBundleName = lspBundleName;
  }

  private static Platform detect() {
    if (SystemInfo.isLinux)
      return LINUX;
    if (SystemInfo.isWindows)
      return WINDOWS;
    if (SystemInfo.isMac)
      return MACOS;
    throw new PlatformDetectionException(SystemInfo.OS_NAME + " is not supported");
  }

  public static final Platform current = detect();
}
