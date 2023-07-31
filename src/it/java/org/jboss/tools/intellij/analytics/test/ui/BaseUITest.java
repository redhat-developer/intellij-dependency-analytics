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
package org.jboss.tools.intellij.analytics.test.ui;

import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ProjectExplorer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Richard Kocian
 */
public class BaseUITest extends AbstractBaseTest {

	@Test
	public void checkIfPomExists() {
		ProjectExplorer projectExplorer = getRobotReference().find(ProjectExplorer.class);
		String[] path = new String[]{"pom.xml"};
		assertTrue(projectExplorer.projectViewTree().isPathExists(path, true));
	}

}
