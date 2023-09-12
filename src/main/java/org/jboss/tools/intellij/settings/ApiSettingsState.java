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
        if (snykToken != null && !snykToken.isBlank()) {
            System.setProperty("EXHORT_SNYK_TOKEN", snykToken);
        } else {
            System.clearProperty("EXHORT_SNYK_TOKEN");
        }
    }
}
