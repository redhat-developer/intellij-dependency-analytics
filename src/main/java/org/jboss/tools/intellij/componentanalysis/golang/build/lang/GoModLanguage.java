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

package org.jboss.tools.intellij.componentanalysis.golang.build.lang;

import com.intellij.lang.Language;

public class GoModLanguage extends Language {
    public static final GoModLanguage INSTANCE = new GoModLanguage();

    private GoModLanguage() {
        super("rhda-go-mod");
    }
}