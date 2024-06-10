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
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@State(
        name = "org.jboss.tools.intellij.settings.ApiSettingsState",
        storages = @Storage(
                value = "rhda.exhort.xml",
                roamingType = RoamingType.DISABLED
        )
)
@Service(Service.Level.APP)
public final class ApiSettingsState implements PersistentStateComponent<ApiSettingsState> {

    public String rhdaToken;

    public String mvnPath;
    public String javaPath;

    public String npmPath;
    public String nodePath;

    public String goPath;
    public boolean goMatchManifestVersions;

    public String pythonPath;
    public String pipPath;
    public boolean usePython2;
    public boolean usePythonVirtualEnv;
    public boolean pythonMatchManifestVersions;
    public boolean pythonInstallBestEfforts;

    public String syftPath;
    public String syftConfigPath;
    public String skopeoPath;
    public String skopeoConfigPath;
    public String dockerPath;
    public String podmanPath;
    public String imagePlatform;
    public String gradlePath;

    public static ApiSettingsState getInstance() {
        ApiSettingsState state = ApplicationManager.getApplication().getService(ApiSettingsState.class);
        if (state.rhdaToken == null || state.rhdaToken.isBlank()) {
            state.rhdaToken = UUID.randomUUID().toString();
        }
        return state;
    }

    @Override
    public @Nullable ApiSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApiSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
