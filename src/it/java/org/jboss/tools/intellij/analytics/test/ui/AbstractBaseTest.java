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


import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowsPane;
import org.jboss.tools.intellij.analytics.test.ui.annotations.UITest;
import org.jboss.tools.intellij.analytics.test.ui.junit.TestRunnerExtension;
import org.jboss.tools.intellij.analytics.test.ui.runner.IdeaRunner;
import org.jboss.tools.intellij.analytics.test.ui.utils.ProjectUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

/**
 * @author Ondrej Dockal, odockal@redhat.com
 */
@ExtendWith(TestRunnerExtension.class)
@UITest
abstract public class AbstractBaseTest {

    private static RemoteRobot robot;
    @BeforeAll
    public static void connect() {
        robot = IdeaRunner.getInstance().getRemoteRobot();
        ProjectUtility.copyProjectFromVSC(robot);
        ProjectUtility.closeTipDialogIfItAppears(robot);
        //ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
        ProjectUtility.closeGotItPopup(robot);
        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        ideStatusBar.waitUntilAllBgTasksFinish();
    }

    public RemoteRobot getRobotReference() {
        return robot;
    }

    public boolean isStripeButtonAvailable(String label) {
        try {
            ToolWindowsPane toolWindowsPane = robot.find(ToolWindowsPane.class);
            toolWindowsPane.stripeButton(label, false);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }
}
