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

package org.jboss.tools.intellij.componentanalysis.maven;

import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class POMFileType extends LanguageFileType {

    POMFileType() {
        super(XMLLanguage.INSTANCE, true);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "pom";
    }

    @Override
    public @NlsContexts.Label @NotNull String getDescription() {
        return "Maven project object model";
    }

    @Override
    public @NlsSafe @NotNull String getDefaultExtension() {
        return "xml";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }
}

