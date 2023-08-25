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

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class NpmCAInspection extends LocalInspectionTool {

    @NonNls
    public static final String SHORT_NAME = "NpmCAInspection";

    @Override
    @NotNull
    public String getGroupDisplayName() {
        return "npm";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String @NotNull [] getGroupPath() {
        return new String[]{"Javascript and Typescript"};
    }

    @Override
    @NotNull
    public String getShortName() {
        return SHORT_NAME;
    }

    @Override
    @NotNull
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}
