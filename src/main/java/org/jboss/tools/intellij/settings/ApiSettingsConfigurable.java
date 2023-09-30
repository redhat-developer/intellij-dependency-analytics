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
        modified |= !settingsComponent.getNodePathText().equals(settings.nodePath);
        modified |= !settingsComponent.getGoPathText().equals(settings.goPath);
        modified |= !settingsComponent.getPythonPathText().equals(settings.pythonPath);
        modified |= !settingsComponent.getPipPathText().equals(settings.pipPath);
        modified |= settingsComponent.getUsePython2Check() != settings.usePython2;
        modified |= settingsComponent.getUsePythonVirtualEnvCheck() != settings.usePythonVirtualEnv;
        modified |= !settingsComponent.getSnykTokenText().equals(settings.snykToken);
        return modified;
    }

    @Override
    public void apply() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        settings.mvnPath = settingsComponent.getMvnPathText();
        settings.javaPath = settingsComponent.getJavaPathText();
        settings.npmPath = settingsComponent.getNpmPathText();
        settings.nodePath = settingsComponent.getNodePathText();
        settings.goPath = settingsComponent.getGoPathText();
        settings.pythonPath = settingsComponent.getPythonPathText();
        settings.pipPath = settingsComponent.getPipPathText();
        settings.usePython2 = settingsComponent.getUsePython2Check();
        settings.usePythonVirtualEnv = settingsComponent.getUsePythonVirtualEnvCheck();
        settings.snykToken = settingsComponent.getSnykTokenText();
    }

    @Override
    public void reset() {
        ApiSettingsState settings = ApiSettingsState.getInstance();
        settingsComponent.setMvnPathText(settings.mvnPath != null ? settings.mvnPath : "");
        settingsComponent.setJavaPathText(settings.javaPath != null ? settings.javaPath : "");
        settingsComponent.setNpmPathText(settings.npmPath != null ? settings.npmPath : "");
        settingsComponent.setNodePathText(settings.nodePath != null ? settings.nodePath : "");
        settingsComponent.setGoPathText(settings.goPath != null ? settings.goPath : "");
        settingsComponent.setPythonPathText(settings.pythonPath != null ? settings.pythonPath : "");
        settingsComponent.setPipPathText(settings.pipPath != null ? settings.pipPath : "");
        settingsComponent.setUsePython2Check(settings.usePython2);
        settingsComponent.setUsePythonVirtualEnvCheck(settings.usePythonVirtualEnv);
        settingsComponent.setSnykTokenText(settings.snykToken != null ? settings.snykToken : "");
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
