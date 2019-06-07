package org.jboss.tools.intellij.analytics;

import com.github.gtache.lsp.client.LanguageClientImpl;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

import java.util.Map;

public class AnalyticsLanguageClient extends LanguageClientImpl {
  @JsonNotification("caNotification")
  public void caNotify(Object payload) {
    if (payload instanceof Map) {
      Map<String, Object> info = (Map<String, Object>) payload;
      if (info.containsKey("data") && info.containsKey("diagCount")) {
        Notifications.Bus.notify(new Notification("Analytics", "Analytics", (String) info.get("data"), NotificationType.INFORMATION));
      }
    }
  }
}
