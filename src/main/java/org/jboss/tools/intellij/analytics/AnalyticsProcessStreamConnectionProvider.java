package org.jboss.tools.intellij.analytics;

import com.github.gtache.lsp.client.connection.ProcessStreamConnectionProvider;
import scala.collection.Seq;

public class AnalyticsProcessStreamConnectionProvider extends ProcessStreamConnectionProvider {
  public AnalyticsProcessStreamConnectionProvider(Seq<String> commands, String workingDir) {
    super(commands, workingDir);
  }

  @Override
  public ProcessBuilder createProcessBuilder() {
    ProcessBuilder builder = super.createProcessBuilder();
    builder.environment().put("RECOMMENDER_API_URL", "https://f8a-analytics-2445582058137.production.gw.apicast.io:443/api/v2");
    builder.environment().put("THREE_SCALE_USER_TOKEN", "9e7da76708fe374d8c10fa752e72989f");
    return builder;
  }
}
