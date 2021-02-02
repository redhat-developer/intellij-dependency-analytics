package org.jboss.tools.intellij.analytics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.diagnostic.Logger;

public final class PostStartupActivity implements StartupActivity {
  private static final Logger log = Logger.getInstance(PostStartupActivity.class);

  @Override
  public void runActivity(Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }

    log.debug("runActivity called for analytics with project {0}", project);
    project.getService(LSPBundle.class).download();
  }
}
