/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.image.build.filetype;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides the file type for Dockerfile files to ensure they use our custom
 * rhda-dockerfile language and parser, even when IntelliJ's bundled TextMate
 * Docker bundle would otherwise claim them.
 *
 * This is necessary because the TextMate file type implements PlainTextLikeFileType,
 * which causes IntelliJ to skip ExternalAnnotator execution, preventing
 * vulnerability analysis from running.
 */
public class DockerfileFileTypeOverrider implements FileTypeOverrider {

    @Override
    public @Nullable FileType getOverriddenFileType(@NotNull VirtualFile file) {
        if (DockerfileFileType.isDockerfile(file)) {
            return DockerfileFileType.INSTANCE;
        }
        return null;
    }
}
