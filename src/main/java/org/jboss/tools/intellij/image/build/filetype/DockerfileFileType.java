/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.image.build.filetype;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jboss.tools.intellij.image.build.lang.DockerfileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DockerfileFileType extends LanguageFileType {
    public static final DockerfileFileType INSTANCE = new DockerfileFileType();

    private DockerfileFileType() {
        super(DockerfileLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "rhda-dockerfile";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "RHDA Dockerfile";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}