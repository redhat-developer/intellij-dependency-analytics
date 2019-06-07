package org.jboss.tools.intellij.analytics;

import com.github.gtache.lsp.client.LanguageClientImpl;
import com.github.gtache.lsp.client.connection.StreamConnectionProvider;
import com.github.gtache.lsp.client.languageserver.serverdefinition.ExeLanguageServerDefinition;
import scala.collection.JavaConverters;

import java.util.Arrays;

public class AnalyticsLanguageServerDefinition extends ExeLanguageServerDefinition {
  public AnalyticsLanguageServerDefinition(String ext, String path, String[] args) {
    super(ext, path, args);
  }

  @Override
  public LanguageClientImpl createLanguageClient() {
    return new AnalyticsLanguageClient();
  }

  @Override
  public StreamConnectionProvider createConnectionProvider(String workingDir) {
    return new AnalyticsProcessStreamConnectionProvider(JavaConverters.asScalaIteratorConverter(Arrays.asList(command()).iterator()).asScala().toSeq(), workingDir);
  }
}
