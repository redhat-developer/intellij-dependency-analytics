/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.exhort;

import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;
import com.redhat.exhort.api.v4.DependencyReport;
import org.jboss.tools.intellij.componentanalysis.CAAnnotator;
import org.jboss.tools.intellij.settings.ApiSettingsState;

public class TelemetryService {
    private static final TelemetryService INSTANCE = new TelemetryService();

    private final Lazy<TelemetryMessageBuilder> builder = new Lazy<>(() -> new TelemetryMessageBuilder(TelemetryService.class.getClassLoader()));

    public static TelemetryMessageBuilder instance() {
        return INSTANCE.builder.get();
    }

    public static void sendPackageUpdateEvent(PsiFile file, String recommendedVersion, String packageName, String actionName) {
        var telemetryMsg = instance().action(actionName);
        telemetryMsg.property("package", packageName);
        telemetryMsg.property("version", recommendedVersion);
        telemetryMsg.property(ApiService.TelemetryKeys.ECOSYSTEM.toString(), CAAnnotator.getPackageManager(file.getName()));
        telemetryMsg.property(ApiService.TelemetryKeys.PLATFORM.toString(), System.getProperty("os.name"));
        telemetryMsg.property(ApiService.TelemetryKeys.MANIFEST.toString(), file.getName());
        telemetryMsg.property(ApiService.TelemetryKeys.RHDA_TOKEN.toString(), ApiSettingsState.getInstance().rhdaToken);
        telemetryMsg.send();
    }

}
