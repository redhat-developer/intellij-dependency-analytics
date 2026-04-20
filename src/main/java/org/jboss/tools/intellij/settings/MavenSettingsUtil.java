package org.jboss.tools.intellij.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenHomeType;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenWorkspaceSettingsComponent;
import org.jetbrains.idea.maven.project.MavenWrapper;

public final class MavenSettingsUtil {

    private static MavenGeneralSettings mavenGeneralSettings;

    private MavenSettingsUtil() {
        // no‑op
    }

    private static Project getProject() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            return openProjects[0];
        }
        return ProjectManager.getInstance().getDefaultProject();
    }

    private static MavenGeneralSettings getMavenGeneralSettings() {
        if (mavenGeneralSettings == null) {
            mavenGeneralSettings = MavenWorkspaceSettingsComponent.getInstance(getProject()).getSettings().getGeneralSettings();
        }
        return mavenGeneralSettings;
    }

    public static boolean isMavenWrapperSelected() {
        MavenGeneralSettings settings = getMavenGeneralSettings();
        MavenHomeType mavenHomeType = settings.getMavenHomeType();
        return mavenHomeType instanceof MavenWrapper;
    }

    public static String getUserSettingsFile() {
        MavenGeneralSettings settings = getMavenGeneralSettings();
        return settings.getUserSettingsFile();
    }

    public static String getLocalRepository() {
        return MavenProjectsManager.getInstance(getProject()).getRepositoryPath().toString();
    }
}
