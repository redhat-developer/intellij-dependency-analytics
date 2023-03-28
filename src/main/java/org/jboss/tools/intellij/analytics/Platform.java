package org.jboss.tools.intellij.analytics;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;

import java.util.Arrays;
import java.util.List;

public class Platform {
  //Set Plugin location in host machine. Location will be used as download location.
  public static final String pluginDirectory = PluginManagerCore.getPlugin(
          PluginId.getId("org.jboss.tools.intellij.analytics")).getPluginPath().toAbsolutePath().toString();

  // Set LSP and CLI tarballs to be downloaded, CLI version is pinned to last stable version instead of latest.
  private static final Platform WINDOWS = new Platform("analytics-lsp-win.exe", "crda_0.2.5_Windows_64bit.tar.gz");
  private static final Platform LINUX = new Platform("analytics-lsp-linux", "crda_0.2.5_Linux_64bit.tar.gz");
  private static final Platform MACOS = new Platform("analytics-lsp-macos", "crda_0.2.5_macOS_64bit.tar.gz");

  public String lspBundleName;
  public String cliTarBallName;
  private Platform(String lspBundleName, String cliTarBallName) {
    this.lspBundleName = lspBundleName;
    this.cliTarBallName = cliTarBallName;
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

  // Set supported file names
  public static final List<String> supportedManifestFiles = Arrays.asList("pom.xml",
          "package.json", "go.mod", "requirements.txt", "requirements-dev.txt");
}
