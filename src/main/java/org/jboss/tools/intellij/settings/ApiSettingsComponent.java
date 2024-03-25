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

package org.jboss.tools.intellij.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ApiSettingsComponent {

    private final static String mvnPathLabel = "<html>Maven > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>mvn</b> executable.</html>";
    private final static String javaPathLabel = "<html>Maven > JAVA_HOME: <b>Path</b>"
            + "<br>Specifies absolute path of Java installation directory.</html>";
    private final static String npmPathLabel = "<html>Npm > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>npm</b> executable.</html>";
    private final static String nodePathLabel = "<html>Node > Directory: <b>Path</b>"
            + "<br>Specifies absolute path of the <i>directory</i> containing <b>node</b> executable.</html>";
    private final static String goPathLabel = "<html>Go > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>go</b> executable.</html>";
    private final static String goMatchManifestVersionsLabel = "<html>Go > Match package version" +
            "<br>Specifies if comparing the resolved package versions with the versions defined in the manifest.</html>";
    private final static String pythonPathLabel = "<html>Python > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>python</b> executable.</html>";
    private final static String pipPathLabel = "<html>Pip > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>pip</b> executable.</html>";
    private final static String usePython2Label = "<html>Python > Executable: <b>Version</b>"
            + "<br>Specifies if using python 2.x.</html>";
    private final static String usePythonVirtualEnvLabel = "<html>Python > Virtual Environment"
            + "<br>Specifies if using virtual environment.</html>";
    private final static String pythonInstallBestEffortsLabel = "<html>Python > Virtual Environment: alternate package version"
            + "<br>Specifies if allowing to use alternate package versions in virtual environment.</html>";
    private final static String pythonMatchManifestVersionsLabel = "<html>Python > Match package version"
            + "<br>Specifies if comparing the resolved package versions with the versions defined in the manifest.</html>";
    private final JPanel mainPanel;

    private final TextFieldWithBrowseButton mvnPathText;
    private final TextFieldWithBrowseButton javaPathText;
    private final TextFieldWithBrowseButton npmPathText;
    private final TextFieldWithBrowseButton nodePathText;
    private final TextFieldWithBrowseButton goPathText;
    private final JCheckBox goMatchManifestVersionsCheck;
    private final TextFieldWithBrowseButton pythonPathText;
    private final TextFieldWithBrowseButton pipPathText;
    private final JCheckBox usePython2Check;
    private final JCheckBox usePythonVirtualEnvCheck;
    private final JCheckBox pythonInstallBestEffortsCheck;
    private final JCheckBox pythonMatchManifestVersionsCheck;

    public ApiSettingsComponent() {
        mvnPathText = new TextFieldWithBrowseButton();
        mvnPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        javaPathText = new TextFieldWithBrowseButton();
        javaPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        npmPathText = new TextFieldWithBrowseButton();
        npmPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        nodePathText = new TextFieldWithBrowseButton();
        nodePathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        goPathText = new TextFieldWithBrowseButton();
        goPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        goMatchManifestVersionsCheck = new JCheckBox("Strictly match package version");

        pythonPathText = new TextFieldWithBrowseButton();
        pythonPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        pipPathText = new TextFieldWithBrowseButton();
        pipPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        usePython2Check = new JCheckBox("Use python 2.x");

        usePythonVirtualEnvCheck = new JCheckBox("Use python virtual environment");

        pythonInstallBestEffortsCheck = new JCheckBox("Allow alternate package version");

        pythonMatchManifestVersionsCheck = new JCheckBox("Strictly match package version");



        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(mvnPathLabel), mvnPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(javaPathLabel), javaPathText, 1, true)
                .addSeparator(10)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(npmPathLabel), npmPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(nodePathLabel), nodePathText, 1, true)
                .addSeparator(10)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(goPathLabel), goPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(goMatchManifestVersionsLabel), goMatchManifestVersionsCheck, 1, true)
                .addSeparator(10)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(pythonPathLabel), pythonPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(pipPathLabel), pipPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(usePython2Label), usePython2Check, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(usePythonVirtualEnvLabel), usePythonVirtualEnvCheck, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(pythonInstallBestEffortsLabel), pythonInstallBestEffortsCheck, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(pythonMatchManifestVersionsLabel), pythonMatchManifestVersionsCheck, 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return mvnPathText;
    }

    @NotNull
    public String getMvnPathText() {
        return mvnPathText.getText();
    }

    public void setMvnPathText(@NotNull String text) {
        mvnPathText.setText(text);
    }

    @NotNull
    public String getJavaPathText() {
        return javaPathText.getText();
    }

    public void setJavaPathText(@NotNull String text) {
        javaPathText.setText(text);
    }

    @NotNull
    public String getNpmPathText() {
        return npmPathText.getText();
    }

    public void setNpmPathText(@NotNull String text) {
        npmPathText.setText(text);
    }

    @NotNull
    public String getNodePathText() {
        return nodePathText.getText();
    }

    public void setNodePathText(@NotNull String text) {
        nodePathText.setText(text);
    }

    @NotNull
    public String getGoPathText() {
        return goPathText.getText();
    }

    public void setGoPathText(@NotNull String text) {
        goPathText.setText(text);
    }

    public boolean getGoMatchManifestVersionsCheck() {
        return goMatchManifestVersionsCheck.isSelected();
    }

    public void setGoMatchManifestVersionsCheck(boolean selected) {
        goMatchManifestVersionsCheck.setSelected(selected);
    }

    @NotNull
    public String getPythonPathText() {
        return pythonPathText.getText();
    }

    public void setPythonPathText(@NotNull String text) {
        pythonPathText.setText(text);
    }

    @NotNull
    public String getPipPathText() {
        return pipPathText.getText();
    }

    public void setPipPathText(@NotNull String text) {
        pipPathText.setText(text);
    }

    public boolean getUsePython2Check() {
        return usePython2Check.isSelected();
    }

    public void setUsePython2Check(boolean selected) {
        usePython2Check.setSelected(selected);
    }

    public boolean getUsePythonVirtualEnvCheck() {
        return usePythonVirtualEnvCheck.isSelected();
    }

    public void setUsePythonVirtualEnvCheck(boolean selected) {
        usePythonVirtualEnvCheck.setSelected(selected);
    }

    public boolean getPythonInstallBestEffortsCheck() {
        return pythonInstallBestEffortsCheck.isSelected();
    }

    public void setPythonInstallBestEffortsCheck(boolean selected) {
        pythonInstallBestEffortsCheck.setSelected(selected);
    }

    public boolean getPythonMatchManifestVersionsCheck() {
        return pythonMatchManifestVersionsCheck.isSelected();
    }

    public void setPythonMatchManifestVersionsCheck(boolean selected) {
        pythonMatchManifestVersionsCheck.setSelected(selected);
    }

}
