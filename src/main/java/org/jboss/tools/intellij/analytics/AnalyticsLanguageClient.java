package org.jboss.tools.intellij.analytics;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.jetbrains.annotations.NotNull;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.DefaultLanguageClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

public class AnalyticsLanguageClient extends DefaultLanguageClient {
  public AnalyticsLanguageClient(@NotNull ClientContext context) {
    super(context);
  }

  private static String getFilename(Map<String, Object> info) {
    String filename = null;
    String url = (String) info.get("uri");
    if (url != null) {
      try {
        filename = Paths.get(new URI(url)).getFileName().toString();
      } catch (URISyntaxException e) {}
    }
    return filename;
  }

  @JsonNotification("caNotification")
  public void caNotify(Object payload) {
    if (payload instanceof Map) {
      Map<String, Object> info = (Map<String, Object>) payload;
      if (info.containsKey("data") && info.containsKey("diagCount")) {
        ActionMessage telemetry = TelemetryService.instance().action("lsp:component_analysis_done");
        String filename = getFilename(info);
        if (filename != null) {
          telemetry.property("filename", filename);
        }
        telemetry.send();
        Notifications.Bus.notify(new Notification("Analytics", "Analytics", (String) info.get("data"), NotificationType.INFORMATION));
      }
    }
  }

}
