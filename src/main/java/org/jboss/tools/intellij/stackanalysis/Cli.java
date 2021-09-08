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

import com.intellij.openapi.util.SystemInfo;
import org.jboss.tools.intellij.analytics.PlatformDetectionException;

public class Cli {
  public String cliBinaryName;
  public String cliReleaseTag;

  // Set name of CLI binary and release tag
  private static final Cli WINDOWS = new Cli("crda.exe", "v0.2.4");
  private static final Cli LINUX = new Cli("crda", "v0.2.4");
  private static final Cli MACOS = new Cli("crda", "v0.2.4");

  private Cli(String cliBinaryName, String cliReleaseTag) {
    this.cliBinaryName = cliBinaryName;
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
