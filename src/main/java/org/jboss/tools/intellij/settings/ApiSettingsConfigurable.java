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

package org.jboss.tools.intellij.settings;

import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ApiSettingsConfigurable implements com.intellij.openapi.options.Configurable {

    private ApiSettingsComponent settingsComponent;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Red Hat Dependency Analytics";
    }

    @Override
    public @Nullable JComponent createComponent() {
        settingsComponent = new ApiSettingsComponent();
        return settingsComponent.getPanel();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return settingsComponent.getPreferredFocusedComponent();
    }

    @Override
    public boolean isModified() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        boolean modified = !settingsComponent.getMvnPathText().equals(settings.mvnPath);
        modified |= !settingsComponent.getJavaPathText().equals(settings.javaPath);
        modified |= !settingsComponent.getNpmPathText().equals(settings.npmPath);
        modified |= !settingsComponent.getPnpmPathText().equals(settings.pnpmPath);
        modified |= !settingsComponent.getNodePathText().equals(settings.nodePath);
        modified |= !settingsComponent.getGoPathText().equals(settings.goPath);
        modified |= settingsComponent.getGoMatchManifestVersionsCheck() != settings.goMatchManifestVersions;
        modified |= !settingsComponent.getPythonPathText().equals(settings.pythonPath);
        modified |= !settingsComponent.getPipPathText().equals(settings.pipPath);
        modified |= settingsComponent.getUsePython2Check() != settings.usePython2;
        modified |= settingsComponent.getUsePythonVirtualEnvCheck() != settings.usePythonVirtualEnv;
        modified |= settingsComponent.getPythonInstallBestEffortsCheck() != settings.pythonInstallBestEfforts;
        modified |= settingsComponent.getPythonMatchManifestVersionsCheck() != settings.pythonMatchManifestVersions;
        modified |= !settingsComponent.getSyftPathText().equals(settings.syftPath);
        modified |= !settingsComponent.getSyftConfigPathText().equals(settings.syftConfigPath);
        modified |= !settingsComponent.getSkopeoPathText().equals(settings.skopeoPath);
        modified |= !settingsComponent.getSkopeoConfigPathText().equals(settings.skopeoConfigPath);
        modified |= !settingsComponent.getDockerPathText().equals(settings.dockerPath);
        modified |= !settingsComponent.getPodmanPathText().equals(settings.podmanPath);
        modified |= !settingsComponent.getImagePlatformText().equals(settings.imagePlatform);
        modified |= !settingsComponent.getGradlePathText().equals(settings.gradlePath);
        return modified;
    }

    @Override
    public void apply() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        settings.mvnPath = settingsComponent.getMvnPathText();
        settings.javaPath = settingsComponent.getJavaPathText();
        settings.npmPath = settingsComponent.getNpmPathText();
        settings.pnpmPath = settingsComponent.getPnpmPathText();
        settings.nodePath = settingsComponent.getNodePathText();
        settings.goPath = settingsComponent.getGoPathText();
        settings.goMatchManifestVersions = settingsComponent.getGoMatchManifestVersionsCheck();
        settings.pythonPath = settingsComponent.getPythonPathText();
        settings.pipPath = settingsComponent.getPipPathText();
        settings.usePython2 = settingsComponent.getUsePython2Check();
        settings.usePythonVirtualEnv = settingsComponent.getUsePythonVirtualEnvCheck();
        settings.pythonInstallBestEfforts = settingsComponent.getPythonInstallBestEffortsCheck();
        settings.pythonMatchManifestVersions = settingsComponent.getPythonMatchManifestVersionsCheck();
        settings.syftPath = settingsComponent.getSyftPathText();
        settings.syftConfigPath = settingsComponent.getSyftConfigPathText();
        settings.skopeoPath = settingsComponent.getSkopeoPathText();
        settings.skopeoConfigPath = settingsComponent.getSkopeoConfigPathText();
        settings.dockerPath = settingsComponent.getDockerPathText();
        settings.podmanPath = settingsComponent.getPodmanPathText();
        settings.imagePlatform = settingsComponent.getImagePlatformText();
        settings.gradlePath = settingsComponent.getGradlePathText();
    }

    @Override
    public void reset() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        settingsComponent.setMvnPathText(settings.mvnPath != null ? settings.mvnPath : "");
        settingsComponent.setJavaPathText(settings.javaPath != null ? settings.javaPath : "");
        settingsComponent.setNpmPathText(settings.npmPath != null ? settings.npmPath : "");
        settingsComponent.setPnpmPathText(settings.pnpmPath != null ? settings.pnpmPath : "");
        settingsComponent.setNodePathText(settings.nodePath != null ? settings.nodePath : "");
        settingsComponent.setGoPathText(settings.goPath != null ? settings.goPath : "");
        settingsComponent.setGoMatchManifestVersionsCheck(settings.goMatchManifestVersions);
        settingsComponent.setPythonPathText(settings.pythonPath != null ? settings.pythonPath : "");
        settingsComponent.setPipPathText(settings.pipPath != null ? settings.pipPath : "");
        settingsComponent.setUsePython2Check(settings.usePython2);
        settingsComponent.setUsePythonVirtualEnvCheck(settings.usePythonVirtualEnv);
        settingsComponent.setPythonInstallBestEffortsCheck(settings.pythonInstallBestEfforts);
        settingsComponent.setPythonMatchManifestVersionsCheck(settings.pythonMatchManifestVersions);
        settingsComponent.setSyftPathText(settings.syftPath);
        settingsComponent.setSyftConfigPathText(settings.syftConfigPath);
        settingsComponent.setSkopeoPathText(settings.skopeoPath);
        settingsComponent.setSkopeoConfigPathText(settings.skopeoConfigPath);
        settingsComponent.setDockerPathText(settings.dockerPath);
        settingsComponent.setPodmanPathText(settings.podmanPath);
        settingsComponent.setImagePlatformText(settings.imagePlatform);
        settingsComponent.setGradlePathText(settings.gradlePath != null ? settings.gradlePath : "");
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
