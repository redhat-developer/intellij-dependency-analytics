package org.jboss.tools.intellij.analytics;

import java.util.Properties;
import java.util.Locale;

public class Platform {
  public String lspBundleName;

  public static final Platform WINDOWS = new Platform("analytics-lsp-win.exe");
  public static final Platform LINUX = new Platform("analytics-lsp-linux");
  public static final Platform MACOS = new Platform("analytics-lsp-macos");

  private Platform(String lspBundleName) {
    this.lspBundleName = lspBundleName;
  }

  static Platform detect(Properties systemProperties) {
    final String osName = systemProperties.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    switch(osName) {
      case "linux":
        return LINUX;
      case "osx":
      case "darwin":
      case "mac os x":
        return MACOS;
      default:
        if (osName.contains("windows"))
          return WINDOWS;
        throw new PlatformDetectionException(osName + " is not supported");
    }
  }

  public static final Platform current = detect(System.getProperties());
}
