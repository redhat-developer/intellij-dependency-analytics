package org.jboss.tools.intellij.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.project.MavenHomeType;
import org.jetbrains.idea.maven.project.MavenWorkspaceSettingsComponent;
import org.jetbrains.idea.maven.project.MavenWrapper;

public final class MavenSettingsUtil {

    private MavenSettingsUtil() {
        // no‑op
    }

    @Nullable
    public static boolean isMavenWrapperSelected() {
        Project project;
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length > 0) {
            project = openProjects[0];
        } else {
            project = ProjectManager.getInstance().getDefaultProject();
        }
        MavenGeneralSettings settings = MavenWorkspaceSettingsComponent.getInstance(project).getSettings().getGeneralSettings();
        MavenHomeType mavenHomeType = settings.getMavenHomeType();
        return mavenHomeType instanceof MavenWrapper;
    }
}
