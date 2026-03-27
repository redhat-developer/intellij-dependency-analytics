/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.report;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jboss.tools.intellij.settings.ApiSettingsState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportFileManager {

    private static final Logger LOG = Logger.getInstance(ReportFileManager.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String NOTIFICATION_GROUP_ID = "Red Hat Dependency Analytics";

    /**
     * Save a copy of the report to user-configured directory if specified.
     *
     * @param htmlFilePath Path to the temporary HTML report file
     * @param manifestName Name of the manifest file (e.g., "pom.xml")
     */
    public static void saveReportCopy(String htmlFilePath, String manifestName) {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        String saveDirectory = settings.reportFilePath;

        if (saveDirectory == null || saveDirectory.trim().isEmpty()) {
            LOG.debug("No custom report path configured, skipping permanent save");
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                Path sourcePath = Paths.get(htmlFilePath);
                Path targetDir = Paths.get(saveDirectory.trim());

                if (!Files.exists(targetDir)) {
                    String message = "Report save directory does not exist: " + targetDir;
                    LOG.warn(message);
                    NotificationGroupManager.getInstance()
                            .getNotificationGroup(NOTIFICATION_GROUP_ID)
                            .createNotification(
                                    "Dependency Analytics Report",
                                    message + ". Please create the directory or update the path in Settings > Tools > Red Hat Dependency Analytics.",
                                    NotificationType.WARNING
                            )
                            .notify(null);
                    return;
                }

                if (!Files.isDirectory(targetDir)) {
                    String message = "Report save path is not a directory: " + targetDir;
                    LOG.warn(message);
                    NotificationGroupManager.getInstance()
                            .getNotificationGroup(NOTIFICATION_GROUP_ID)
                            .createNotification(
                                    "Dependency Analytics Report",
                                    message + ". Please update the path in Settings > Tools > Red Hat Dependency Analytics.",
                                    NotificationType.WARNING
                            )
                            .notify(null);
                    return;
                }

                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
                String filename = String.format("report_%s_%s.html", manifestName, timestamp);
                Path targetPath = targetDir.resolve(filename);

                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Report successfully saved to: " + targetPath);
            } catch (IOException e) {
                LOG.error("Failed to save report copy", e);
                NotificationGroupManager.getInstance()
                        .getNotificationGroup(NOTIFICATION_GROUP_ID)
                        .createNotification(
                                "Dependency Analytics Report",
                                "Failed to save report: " + e.getMessage(),
                                NotificationType.WARNING
                        )
                        .notify(null);
            } catch (Exception e) {
                LOG.error("Unexpected error during report save", e);
                NotificationGroupManager.getInstance()
                        .getNotificationGroup(NOTIFICATION_GROUP_ID)
                        .createNotification(
                                "Dependency Analytics Report",
                                "Unexpected error saving report: " + e.getMessage(),
                                NotificationType.WARNING
                        )
                        .notify(null);
            }
        });
    }
}
