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

package org.jboss.tools.intellij.componentanalysis.golang.build.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jboss.tools.intellij.componentanalysis.golang.build.filetype.GoModFileType;
import org.jboss.tools.intellij.componentanalysis.golang.build.lang.GoModLanguage;
import org.jetbrains.annotations.NotNull;

public class GoModFile extends PsiFileBase {
    public GoModFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, GoModLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return GoModFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Go Module File";
    }
}