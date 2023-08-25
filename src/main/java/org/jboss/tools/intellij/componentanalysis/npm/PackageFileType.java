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

package org.jboss.tools.intellij.componentanalysis.npm;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PackageFileType extends LanguageFileType {
    protected PackageFileType() {
        super(JsonLanguage.INSTANCE, true);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "package";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Project manifest";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "json";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }
}
