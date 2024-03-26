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
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
    private final static String syftPathLabel = "<html>Syft > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of the <b>syft</b> executable.</html>";
    private final static String syftConfigPathLabel = "<html>Syft > Configuration File: <b>Path</b>"
            + "<br>Specifies absolute path of the <b>syft</b> configuration file.</html>";
    private final static String skopeoPathLabel = "<html>Skopeo > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of the <b>skopeo</b> executable.</html>";
    private final static String skopeoConfigPathLabel = "<html>Skopeo > Authentication File: <b>Path</b>"
            + "<br>Specifies absolute path of the <b>skopeo</b> authentication file.</html>";
    private final static String dockerPathLabel = "<html>Docker > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of the <b>docker</b> executable.</html>";
    private final static String podmanPathLabel = "<html>Podman > Executable: <b>Path</b>"
            + "<br>Specifies absolute path of <b>podman</b> executable.</html>";
     private final static String imagePlatformLabel = "<html>Image > Build: <b>Platform</b>"
            + "<br>Specifies the platform of the images, e.g. <b>linux/amd64</b> or <b>linux/arm64</b>.</html>";

    private final JPanel mainPanel;

    private final TextFieldWithBrowseButton mvnPathText;
    private final TextFieldWithBrowseButton javaPathText;
    private final TextFieldWithBrowseButton npmPathText;
    private final TextFieldWithBrowseButton nodePathText;
    private final TextFieldWithBrowseButton goPathText;
    private final JBCheckBox goMatchManifestVersionsCheck;
    private final TextFieldWithBrowseButton pythonPathText;
    private final TextFieldWithBrowseButton pipPathText;
    private final JBCheckBox usePython2Check;
    private final JBCheckBox usePythonVirtualEnvCheck;
    private final JBCheckBox pythonInstallBestEffortsCheck;
    private final JBCheckBox pythonMatchManifestVersionsCheck;
    private final TextFieldWithBrowseButton syftPathText;
    private final TextFieldWithBrowseButton syftConfigPathText;

    private final TextFieldWithBrowseButton skopeoPathText;
    private final TextFieldWithBrowseButton skopeoConfigPathText;
    private final TextFieldWithBrowseButton dockerPathText;
    private final TextFieldWithBrowseButton podmanPathText;
    private final JBTextField imagePlatformText;

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

        goMatchManifestVersionsCheck = new JBCheckBox("Strictly match package version");

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

        usePython2Check = new JBCheckBox("Use python 2.x");

        usePythonVirtualEnvCheck = new JBCheckBox("Use python virtual environment");

        pythonInstallBestEffortsCheck = new JBCheckBox("Allow alternate package version");

        pythonMatchManifestVersionsCheck = new JBCheckBox("Strictly match package version");

        syftPathText = new TextFieldWithBrowseButton();
        syftPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        syftConfigPathText = new TextFieldWithBrowseButton();
        syftConfigPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        skopeoPathText = new TextFieldWithBrowseButton();
        skopeoPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        skopeoConfigPathText = new TextFieldWithBrowseButton();
        skopeoConfigPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        dockerPathText = new TextFieldWithBrowseButton();
        dockerPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        podmanPathText = new TextFieldWithBrowseButton();
        podmanPathText.addBrowseFolderListener(
                null,
                null,
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        );

        imagePlatformText = new JBTextField();

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
                .addSeparator(10)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(syftPathLabel), syftPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(syftConfigPathLabel), syftConfigPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(skopeoPathLabel), skopeoPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(skopeoConfigPathLabel), skopeoConfigPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(dockerPathLabel), dockerPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(podmanPathLabel), podmanPathText, 1, true)
                .addVerticalGap(10)
                .addLabeledComponent(new JBLabel(imagePlatformLabel), imagePlatformText, 1, true)
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

    @NotNull
    public String getSyftPathText() {
        return syftPathText.getText();
    }

    public void setSyftPathText(@NotNull String text) {
        syftPathText.setText(text);
    }

    @NotNull
    public String getSyftConfigPathText() {
        return syftConfigPathText.getText();
    }

    public void setSyftConfigPathText(@NotNull String text) {
        syftConfigPathText.setText(text);
    }

    @NotNull
    public String getSkopeoPathText() {
        return skopeoPathText.getText();
    }

    public void setSkopeoPathText(@NotNull String text) {
        skopeoPathText.setText(text);
    }

    @NotNull
    public String getSkopeoConfigPathText() {
        return skopeoConfigPathText.getText();
    }

    public void setSkopeoConfigPathText(@NotNull String text) {
        skopeoConfigPathText.setText(text);
    }

    @NotNull
    public String getDockerPathText() {
        return dockerPathText.getText();
    }

    public void setDockerPathText(@NotNull String text) {
        dockerPathText.setText(text);
    }

    @NotNull
    public String getPodmanPathText() {
        return podmanPathText.getText();
    }

    public void setPodmanPathText(@NotNull String text) {
        podmanPathText.setText(text);
    }

    @NotNull
    public String getImagePlatformText() {
        return imagePlatformText.getText();
    }

    public void setImagePlatformText(@NotNull String text) {
        imagePlatformText.setText(text);
    }
}
