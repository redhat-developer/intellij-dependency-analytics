/*******************************************************************************
 * Copyright (c) 2026 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.stackanalysis;

import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.serviceContainer.AlreadyDisposedException;
import org.jboss.tools.intellij.componentanalysis.ManifestExclusionManager;
import org.jboss.tools.intellij.exhort.ApiService;
import org.jboss.tools.intellij.report.AnalyticsReportUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Action that triggers batch workspace stack analysis on all packages in a
 * JS/TS monorepo or Cargo workspace from the project base path.
 */
public class SaBatchAction extends AnAction {
    private static final Logger logger = Logger.getInstance(SaBatchAction.class);

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * Performs batch stack analysis on the workspace root, displaying the
     * combined HTML report in a custom editor tab.
     *
     * @param event the action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            Messages.showErrorDialog(project, "Cannot determine project base path.", "Error");
            return;
        }

        Path baseDirPath = Path.of(basePath);
        if (!isSupportedWorkspace(baseDirPath)) {
            Messages.showWarningDialog(project,
                    "No supported workspace detected in the project root.\n\n"
                            + "Batch analysis requires one of:\n"
                            + "  • JS/TS workspace (package.json + lock file)\n"
                            + "  • Cargo workspace (Cargo.toml + Cargo.lock)",
                    "Batch Analysis Not Available");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Analyzing workspace dependencies...", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setText("Discovering workspace packages...");
                    indicator.setIndeterminate(true);

                    ApiService apiService = project.getService(ApiService.class);
                    List<String> ignorePatterns = ManifestExclusionManager.getExclusionPatterns();

                    indicator.setText("Generating batch analysis report...");

                    Path reportPath = apiService.getBatchStackAnalysis(basePath, ignorePatterns);
                    String reportLink = reportPath.toUri().toString();

                    JsonObject manifestDetails = new JsonObject();
                    manifestDetails.addProperty("showParent", false);
                    manifestDetails.addProperty("manifestName", "batch-workspace-analysis.html");
                    manifestDetails.addProperty("manifestPath", basePath);
                    manifestDetails.addProperty("manifestFileParent", project.getName());
                    manifestDetails.addProperty("report_link", reportLink);
                    manifestDetails.addProperty("manifestNameWithoutExtension", "batch-workspace-analysis");

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (project.isDisposed()) {
                            return;
                        }
                        try {
                            AnalyticsReportUtils analyticsReportUtils = new AnalyticsReportUtils();
                            analyticsReportUtils.openCustomEditor(
                                    FileEditorManager.getInstance(project), manifestDetails, project);
                        } catch (AlreadyDisposedException e) {
                            // Project was disposed — ignore silently.
                        } catch (Exception e) {
                            logger.error(e);
                            Messages.showErrorDialog(project,
                                    "Can't open report: " + e.getLocalizedMessage(),
                                    "Error");
                        }
                    });
                } catch (RuntimeException ex) {
                    logger.error(ex);
                    ApplicationManager.getApplication().invokeLater(() ->
                            Messages.showErrorDialog(project,
                                    "Batch analysis failed: " + ex.getLocalizedMessage(),
                                    "Error"));
                }
            }
        });
    }

    /**
     * Checks whether the project root contains a supported workspace layout
     * (JS/TS with a lock file, or Cargo with Cargo.lock).
     */
    static boolean isSupportedWorkspace(Path rootDir) {
        // Cargo workspace
        if (Files.isRegularFile(rootDir.resolve("Cargo.toml"))
                && Files.isRegularFile(rootDir.resolve("Cargo.lock"))) {
            return true;
        }
        // JS/TS workspace
        if (Files.isRegularFile(rootDir.resolve("package.json"))) {
            return Files.isRegularFile(rootDir.resolve("pnpm-lock.yaml"))
                    || Files.isRegularFile(rootDir.resolve("yarn.lock"))
                    || Files.isRegularFile(rootDir.resolve("package-lock.json"));
        }
        return false;
    }

    /**
     * Enables the action only when a project is open.
     *
     * @param event the action event
     */
    @Override
    public void update(AnActionEvent event) {
        event.getPresentation().setEnabledAndVisible(event.getProject() != null);
    }
}
