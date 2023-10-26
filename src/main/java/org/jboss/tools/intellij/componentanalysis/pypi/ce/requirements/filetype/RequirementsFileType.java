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


package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.lang.RequirementsLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RequirementsFileType extends LanguageFileType {

    public static final RequirementsFileType INSTANCE = new RequirementsFileType();

    RequirementsFileType() {
        super(RequirementsLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "rhda-requirements";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Items to be installed by pip";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "txt";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }
}
