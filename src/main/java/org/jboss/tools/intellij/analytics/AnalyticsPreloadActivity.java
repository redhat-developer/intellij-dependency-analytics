package org.jboss.tools.intellij.analytics;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.extensions.PluginId;
import org.wso2.lsp4intellij.IntellijLanguageClient;

import java.io.File;

public class AnalyticsPreloadActivity implements BaseComponent {
  private static final String[] EXTENSIONS = {"xml", "json", "txt"};

  @Override
  public void initComponent() {
    IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
    File serverPath = new File(descriptor.getPath(), "lib/server/server.js");
    final String[] cmds = {"node", serverPath.getAbsolutePath().toString(), "--stdio"};
    for(String ext : EXTENSIONS) {
      AnalyticsLanguageServerDefinition serverDefinition = new AnalyticsLanguageServerDefinition(ext, cmds);
      IntellijLanguageClient.addServerDefinition(serverDefinition);
      IntellijLanguageClient.addExtensionManager(ext, serverDefinition);
    }
  }
}
