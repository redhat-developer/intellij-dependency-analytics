package org.jboss.tools.intellij.analytics;

import org.wso2.lsp4intellij.client.connection.ProcessStreamConnectionProvider;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class AnalyticsProcessStreamConnectionProvider extends ProcessStreamConnectionProvider {
  public AnalyticsProcessStreamConnectionProvider(List<String> commands, String workingDir) {
    super(createProcessBuilder(commands, workingDir));
  }

  protected static ProcessBuilder createProcessBuilder(List<String> commands, String workingDir) {
    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.directory(new File(workingDir));
    builder.redirectError(ProcessBuilder.Redirect.INHERIT);
    builder.environment().put("RECOMMENDER_API_URL", "https://f8a-analytics-2445582058137.production.gw.apicast.io:443/api/v2");
    builder.environment().put("THREE_SCALE_USER_TOKEN", "9e7da76708fe374d8c10fa752e72989f");
    builder.environment().put("UUID", UUID.randomUUID().toString());
    //builder.environment().put("PROVIDE_FULLSTACK_ACTION", "true");
    return builder;
  }
}
