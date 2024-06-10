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

package org.jboss.tools.intellij.componentanalysis.gradle.build.lexer;

import com.intellij.lexer.FlexAdapter;
import org.jboss.tools.intellij.componentanalysis.pypi.requirements.lexer.RequirementsLexer;

public class BuildGradleLexerAdapter extends FlexAdapter {
    public BuildGradleLexerAdapter() {
        super(new BuildGradleLexer(null));
    }
}

