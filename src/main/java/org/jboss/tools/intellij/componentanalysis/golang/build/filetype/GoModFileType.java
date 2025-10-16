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

package org.jboss.tools.intellij.componentanalysis.golang.build.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jboss.tools.intellij.componentanalysis.golang.build.lang.GoModLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GoModFileType extends LanguageFileType {
    public static final GoModFileType INSTANCE = new GoModFileType();

    private GoModFileType() {
        super(GoModLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "rhda-go-mod";
    }

    @Override
    public @NotNull String getDescription() {
        return "Go module file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "mod";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }
}