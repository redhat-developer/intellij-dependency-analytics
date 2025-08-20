/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.componentanalysis;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jboss.tools.intellij.settings.ApiSettingsState;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManifestExclusionManager {

    private static final Logger LOG = Logger.getInstance(ManifestExclusionManager.class);

    public static boolean isManifestExcluded(VirtualFile file, Project project) {
        if (file == null) {
            return false;
        }

        ApiSettingsState settings = ApiSettingsState.getInstance();
        String patterns = settings.manifestExclusionPatterns;

        if (patterns == null || patterns.trim().isEmpty()) {
            return false;
        }

        VirtualFile projectRoot = project.getBaseDir();
        if (projectRoot == null) {
            return false;
        }

        String relativePath = getRelativePath(file, projectRoot);
        if (relativePath == null) {
            return false;
        }

        List<String> exclusionPatterns = parsePatterns(patterns);
        return matchesAnyPattern(relativePath, exclusionPatterns);
    }

    public static void addExclusionPattern(String manifestPath, Project project) {
        if (manifestPath == null || manifestPath.trim().isEmpty()) {
            return;
        }

        VirtualFile projectRoot = project.getBaseDir();
        if (projectRoot == null) {
            return;
        }

        VirtualFile manifestFile = project.getBaseDir().findFileByRelativePath(manifestPath);
        if (manifestFile == null) {
            return;
        }

        String relativePath = getRelativePath(manifestFile, projectRoot);
        if (relativePath == null) {
            return;
        }

        ApiSettingsState settings = ApiSettingsState.getInstance();
        List<String> currentPatterns = parsePatterns(settings.manifestExclusionPatterns);
        
        if (!currentPatterns.contains(relativePath)) {
            currentPatterns.add(relativePath);
            settings.manifestExclusionPatterns = String.join("\n", currentPatterns);
        }
    }

    private static String getRelativePath(VirtualFile file, VirtualFile projectRoot) {
        try {
            String filePath = file.getPath();
            String projectPath = projectRoot.getPath();
            
            if (filePath.startsWith(projectPath)) {
                String relativePath = filePath.substring(projectPath.length());
                if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                    relativePath = relativePath.substring(1);
                }
                return relativePath;
            }
        } catch (Exception e) {
            LOG.warn("Failed to get relative path for file: " + file.getPath(), e);
        }
        return null;
    }

    private static List<String> parsePatterns(String patterns) {
        if (patterns == null || patterns.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(patterns.split("[\n\r]+"))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty() && !pattern.startsWith("#"))
                .collect(Collectors.toList());
    }

    private static boolean matchesAnyPattern(String path, List<String> patterns) {
        Path filePath = Paths.get(path);
        
        for (String pattern : patterns) {
            try {
                // Test the pattern as-is first
                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
                if (matcher.matches(filePath)) {
                    LOG.debug("File " + path + " matches exclusion pattern: " + pattern);
                    return true;
                }
                
                // For patterns starting with "**/" that don't match, also test against
                // the path with a virtual directory prefix to handle the Java PathMatcher
                // limitation where "**/file.txt" doesn't match "file.txt" at root
                if (pattern.startsWith("**/")) {
                    // Try matching with a virtual directory prefix
                    Path prefixedPath = Paths.get("dummy/" + path);
                    if (matcher.matches(prefixedPath)) {
                        LOG.debug("File " + path + " matches exclusion pattern: " + pattern + " (with directory prefix)");
                        return true;
                    }
                }
            } catch (Exception e) {
                LOG.warn("Invalid glob pattern: " + pattern, e);
            }
        }
        
        return false;
    }

    public static List<String> getExclusionPatterns() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        return parsePatterns(settings.manifestExclusionPatterns);
    }

    public static void setExclusionPatterns(List<String> patterns) {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        settings.manifestExclusionPatterns = String.join("\n", patterns);
    }
}