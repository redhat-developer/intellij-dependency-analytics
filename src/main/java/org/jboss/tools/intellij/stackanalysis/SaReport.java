/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.stackanalysis;

import com.intellij.ui.jcef.JBCefBrowser;
import javax.swing.JComponent;


public class SaReport {
    private final JBCefBrowser browser;

    /**
     * <p>Initialize JCEF Browser.</p>
     */
    public SaReport(String url) {
        // Open given url in JCEF browser
        browser = new JBCefBrowser();
        browser.loadURL(url);
    }


    /**
     * <p>Get JCEF Browser component.</p>
     *
     * <p>This method will be called within ReportFileEditor.</p>
     *
     * @return JComponent Object
     */
    public JComponent getContent() {
        // return content of browser to be attached in editor window
        return browser.getComponent();
    }
}
