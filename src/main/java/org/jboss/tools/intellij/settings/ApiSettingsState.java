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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "org.jboss.tools.intellij.settings.ApiSettingsState",
        storages = @Storage(
                value = "rhda.exhort.xml",
                roamingType = RoamingType.DISABLED
        )
)
@Service(Service.Level.APP)
public final class ApiSettingsState implements PersistentStateComponent<ApiSettingsState> {

    public static ApiSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ApiSettingsState.class);
    }

    public String mvnPath;
    public String javaPath;
    public String npmPath;
    public String nodePath;
    public String goPath;
    public String pythonPath;
    public String pipPath;
    public boolean usePython2;
    public boolean usePythonVirtualEnv;

    public String snykToken;

    @Override
    public @Nullable ApiSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setApiOptions() {
        if (mvnPath != null && !mvnPath.isBlank()) {
            System.setProperty("EXHORT_MVN_PATH", mvnPath);
        } else {
            System.clearProperty("EXHORT_MVN_PATH");
        }
        if (javaPath != null && !javaPath.isBlank()) {
            System.setProperty("JAVA_HOME", javaPath);
        } else {
            System.clearProperty("JAVA_HOME");
        }
        if (npmPath != null && !npmPath.isBlank()) {
            System.setProperty("EXHORT_NPM_PATH", npmPath);
        } else {
            System.clearProperty("EXHORT_NPM_PATH");
        }
        if (nodePath != null && !nodePath.isBlank()) {
            System.setProperty("NODE_HOME", nodePath);
        } else {
            System.clearProperty("NODE_HOME");
        }
        if (goPath != null && !goPath.isBlank()) {
            System.setProperty("EXHORT_GO_PATH", goPath);
        } else {
            System.clearProperty("EXHORT_GO_PATH");
        }
        if (usePython2) {
            if (pythonPath != null && !pythonPath.isBlank()) {
                System.setProperty("EXHORT_PYTHON_PATH", pythonPath);
            } else {
                System.clearProperty("EXHORT_PYTHON_PATH");
            }
            if (pipPath != null && !pipPath.isBlank()) {
                System.setProperty("EXHORT_PIP_PATH", pipPath);
            } else {
                System.clearProperty("EXHORT_PIP_PATH");
            }
            System.clearProperty("EXHORT_PYTHON3_PATH");
            System.clearProperty("EXHORT_PIP3_PATH");
        } else {
            if (pythonPath != null && !pythonPath.isBlank()) {
                System.setProperty("EXHORT_PYTHON3_PATH", pythonPath);
            } else {
                System.clearProperty("EXHORT_PYTHON3_PATH");
            }
            if (pipPath != null && !pipPath.isBlank()) {
                System.setProperty("EXHORT_PIP3_PATH", pipPath);
            } else {
                System.clearProperty("EXHORT_PIP3_PATH");
            }
            System.clearProperty("EXHORT_PYTHON_PATH");
            System.clearProperty("EXHORT_PIP_PATH");
        }
        if (usePythonVirtualEnv) {
            System.setProperty("EXHORT_PYTHON_VIRTUAL_ENV", "true");
        } else {
            System.clearProperty("EXHORT_PYTHON_VIRTUAL_ENV");
        }
        if (snykToken != null && !snykToken.isBlank()) {
            System.setProperty("EXHORT_SNYK_TOKEN", snykToken);
        } else {
            System.clearProperty("EXHORT_SNYK_TOKEN");
        }
    }
}
