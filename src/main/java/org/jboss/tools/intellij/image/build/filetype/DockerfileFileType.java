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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jboss.tools.intellij.image.build.lang.DockerfileLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DockerfileFileType extends LanguageFileType {
    public static final DockerfileFileType INSTANCE = new DockerfileFileType();

    public static boolean isDockerfile(@Nullable PsiFile file) {
        if (file == null) {
            return false;
        }
        if (file.getVirtualFile() != null) {
            return isDockerfile(file.getVirtualFile());
        }
        return isDockerfileName(file.getName());
    }

    public static boolean isDockerfile(@Nullable VirtualFile file) {
        return file != null && isDockerfileName(file.getName());
    }

    private static boolean isDockerfileName(String name) {
        return name.equals("Dockerfile") || name.startsWith("Dockerfile.")
                || name.equals("Containerfile") || name.startsWith("Containerfile.")
                || name.endsWith(".dockerfile") || name.endsWith(".containerfile");
    }

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