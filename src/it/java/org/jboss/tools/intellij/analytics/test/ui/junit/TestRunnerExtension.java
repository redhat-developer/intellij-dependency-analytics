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
package org.jboss.tools.intellij.analytics.test.ui.junit;

import com.redhat.devtools.intellij.commonuitest.UITestRunner;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import org.jboss.tools.intellij.analytics.test.ui.runner.IdeaRunner;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

/**
 * JUnit 5 test extension providing wrapper for tests and initiate connection to IDEA under test
 * @author Ondrej Dockal
 *
 */
public class TestRunnerExtension implements BeforeAllCallback, CloseableResource {

	@Override
	public void beforeAll(ExtensionContext context) {
		System.out.println("Initialize IdeaRunner and start IDE");
		// need to initialize store, so that close method will be called at the testing end
		context.getRoot().getStore(Namespace.GLOBAL).put("InitializeTest", this);
		IdeaRunner.getInstance().startIDE(IntelliJVersion.COMMUNITY_V_2022_1, 8580);
	}

	@Override
	public void close() {
		System.out.println("Closing IDE");
		UITestRunner.closeIde();

	}

}
