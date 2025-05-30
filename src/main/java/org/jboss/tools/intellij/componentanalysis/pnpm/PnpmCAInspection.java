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

package org.jboss.tools.intellij.componentanalysis.pnpm;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PnpmCAInspection extends LocalInspectionTool {

    @NonNls
    public static final String SHORT_NAME = "PnpmCAInspection";

    @Override
    @NotNull
    public String getGroupDisplayName() {
        return "Imports and dependencies";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String @NotNull [] getGroupPath() {
        return new String[]{"JavaScript and TypeScript"};
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
