package org.jboss.tools.intellij.analytics;

import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.extensions.PluginId;

import java.io.File;
import java.lang.reflect.Field;

public class AnalyticsPreloadActivity implements BaseComponent {
  @Override
  public void initComponent() {
    hackClassLoader();
    IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("org.jboss.tools.intellij.analytics"));
    File serverPath = new File(descriptor.getPath(), "lib/server/server.js");
    LanguageServerDefinition.register(new AnalyticsLanguageServerDefinition("xml;json;txt", "node", new String[] { serverPath.getAbsolutePath().toString(), "--stdio"}));
  }

  private void hackClassLoader() {
    ClassLoader loader = AnalyticsPreloadActivity.class.getClassLoader();
    if (loader instanceof PluginClassLoader) {
      try {
        Field parentsField = loader.getClass().getDeclaredField("myParents");
        parentsField.setAccessible(true);
        ClassLoader[] parents = (ClassLoader[]) parentsField.get(loader);
        if (parents.length > 1) {
          ClassLoader first = parents[0];
          parents[0] = parents[parents.length - 1];
          parents[parents.length - 1] = first;
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }
}
