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

package org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.psi;

import com.intellij.psi.tree.IElementType;
import org.jboss.tools.intellij.componentanalysis.pypi.ce.requirements.lang.RequirementsLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RequirementsElementType extends IElementType {
    public RequirementsElementType(@NonNls @NotNull String debugName) {
        super(debugName, RequirementsLanguage.INSTANCE);
    }
}