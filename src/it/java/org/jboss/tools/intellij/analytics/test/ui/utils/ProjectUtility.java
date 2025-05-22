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
package org.jboss.tools.intellij.analytics.test.ui.utils;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.JButtonFixture;
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.information.TipDialog;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.project.NewProjectDialogWizard;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * @author Ondrej Dockal, Oleksii Korniienko, Ihor Okhrimenko, Richard Kocian
 */
public class ProjectUtility {



    public static void createEmptyProject(RemoteRobot robot, String projectName) {
        final FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class);
        flatWelcomeFrame.createNewProject();
        final NewProjectDialogWizard newProjectDialogWizard = flatWelcomeFrame.find(NewProjectDialogWizard.class, Duration.ofSeconds(20));
        selectNewProjectType(robot, "Empty Project");
        newProjectDialogWizard.next();
        JTextFieldFixture textField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='untitled']"));
        textField.setText(projectName);
        newProjectDialogWizard.finish();
        final IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        ideStatusBar.waitUntilProjectImportIsComplete();
    }

    public static void selectNewProjectType(RemoteRobot robot, String projectType) {
        ComponentFixture newProjectTypeList = robot.findAll(ComponentFixture.class, byXpath("JBList", "//div[contains(@visible_text, 'FX')]")).get(0);
        newProjectTypeList.findText(projectType).click();
    }

    public static void copyProjectFromVSC(RemoteRobot robot) {
        JButtonFixture copyProjectFromVSCList = robot.find(JButtonFixture.class, byXpath("//div[@accessiblename.key='action.Vcs.VcsClone.text']"));
        copyProjectFromVSCList.click();
        JTextFieldFixture textField = robot.find(JTextFieldFixture.class, byXpath("//div[@class='BorderlessTextField']"));
        textField.click();

        textField.setText("https://github.com/devfile-samples/devfile-sample-java-springboot-basic.git");

        JButtonFixture buttonFixture = robot.find(JButtonFixture.class, byXpath("//div[@text.key='clone.dialog.clone.button']"));
        buttonFixture.click();

        clickOnTrustProjectIfAppears(robot);

        final IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        ideStatusBar.waitUntilProjectImportIsComplete();
    }


    public static void clickOnTrustProjectIfAppears(RemoteRobot robot) {
        try {
            JButtonFixture trustProjectButton = robot.find(JButtonFixture.class, byXpath("//div[@text.key='untrusted.project.dialog.trust.button']"), Duration.ofSeconds(20));
            trustProjectButton.click();
        } catch (WaitForConditionTimeoutException e) {
            // no dialog appeared, no need to exception handling
        }
    }

    public static void closeTipDialogIfItAppears(RemoteRobot robot) {
        try {
            TipDialog tipDialog = robot.find(TipDialog.class, Duration.ofSeconds(10));
//            tipDialog.close(); // temporary commented
            robot.find(ComponentFixture.class, byXpath("//div[@accessiblename='Close' and @class='JButton' and @text='Close']"), Duration.ofSeconds(5)).click(); // temporary workaround
        } catch (WaitForConditionTimeoutException e) {
            // no dialog appeared, no need to exception handling
        }
    }

    public static void closeGotItPopup(RemoteRobot robot) {
        try {
            robot.find(ComponentFixture.class, byXpath("JBList", "//div[@accessiblename='Got It' and @class='JButton' and @text='Got It']"), Duration.ofSeconds(10)).click();
        } catch (WaitForConditionTimeoutException e) {
            // no dialog appeared, no need to exception handling
        }
    }

    public static void sleep(long ms) {
        System.out.println("Putting thread into sleep for: " + ms + " ms");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
