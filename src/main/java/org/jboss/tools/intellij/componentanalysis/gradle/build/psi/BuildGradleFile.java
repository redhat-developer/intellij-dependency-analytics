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

package org.jboss.tools.intellij.componentanalysis.gradle.build.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jboss.tools.intellij.componentanalysis.gradle.build.filetype.BuildGradleFileType;
import org.jboss.tools.intellij.componentanalysis.gradle.build.lang.BuildGradleLanguage;
import org.jetbrains.annotations.NotNull;

public class BuildGradleFile extends PsiFileBase {

    public BuildGradleFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, BuildGradleLanguage.INSTANCE);
    }

    @NotNull
    public FileType getFileType() {
        return BuildGradleFileType.INSTANCE;
    }

    @NotNull
    public String toString() {
        return "build-gradle-file";
    }
}
