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

package org.jboss.tools.intellij.image.build.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jboss.tools.intellij.image.build.filetype.DockerfileFileType;
import org.jboss.tools.intellij.image.build.lang.DockerfileLanguage;
import org.jetbrains.annotations.NotNull;

public class DockerfileFile extends PsiFileBase {
    public DockerfileFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DockerfileLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DockerfileFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Dockerfile File";
    }
}