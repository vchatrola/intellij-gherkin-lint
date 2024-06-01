package com.vchatrola.plugin.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.vchatrola.common.ResourceUtil;

import javax.swing.*;
import java.awt.*;

public class GherkinLintSettingsUI {
    private final JPanel panel;
    private final JBCheckBox customLogicCheckBox;
    private final TextFieldWithBrowseButton copyDirectoryField;
    private final TextFieldWithBrowseButton customFileField;
    private final JButton copyButton;
    private final JBLabel instructionsLabel;

    public GherkinLintSettingsUI() {
        panel = new JPanel(new BorderLayout());
        customLogicCheckBox = createCustomLogicCheckBox();
        copyDirectoryField = createCopyDirectoryField();
        customFileField = createCustomFileField();
        copyButton = createCopyButton();
        instructionsLabel = createInstructionsLabel();

        // Add components to form panel using FormBuilder
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addComponent(customLogicCheckBox)
                .addVerticalGap(5)
                .addSeparator()
                .addVerticalGap(5)
                .addLabeledComponent("Custom rules file:", customFileField, 1, false)
                .addVerticalGap(5)
                .addLabeledComponent("Copy sample file to:", copyDirectoryField, 1,
                        false)
                .addVerticalGap(5)
                .addComponent(copyButton)
                .addVerticalGap(5)
                .addSeparator()
                .addVerticalGap(5)
                .addComponent(instructionsLabel)
                .getPanel();

        panel.add(formPanel, BorderLayout.NORTH);

        // Update component states based on checkbox selection
        customLogicCheckBox.addActionListener(e -> updateComponentStates(customLogicCheckBox.isSelected()));
    }

    private JBCheckBox createCustomLogicCheckBox() {
        JBCheckBox checkBox = new JBCheckBox("Enable custom validation");
        checkBox.setToolTipText("Enable custom validation logic to use your own validation rules.");
        return checkBox;
    }

    private TextFieldWithBrowseButton createCopyDirectoryField() {
        TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
        field.addBrowseFolderListener(
                "Select Directory",
                "Select the directory to copy the sample requirements file to",
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );
        return field;
    }

    private TextFieldWithBrowseButton createCustomFileField() {
        TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
        field.addBrowseFolderListener(
                "Select Custom Requirements File",
                "Select the custom requirements file to be used for validation",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor()
        );
        return field;
    }

    private JButton createCopyButton() {
        JButton button = new JButton("Copy Sample");
        button.addActionListener(e -> ResourceUtil.copySampleFile(copyDirectoryField.getText()));
        return button;
    }

    private JBLabel createInstructionsLabel() {
        JBLabel label = new JBLabel("<html><body style='width: 400px'>" +
                "1. Enable custom validation.<br>" +
                "2. Copy the sample file to your directory and modify it as needed.<br>" +
                "3. Specify the path to your custom rules file.</body></html>");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private void updateComponentStates(boolean selected) {
        customFileField.setEnabled(selected);
        copyDirectoryField.setEnabled(selected);
        copyButton.setEnabled(selected);
    }

    public JPanel createPanel() {
        return panel;
    }

    public String getCustomFilePath() {
        return customFileField.getText();
    }

    public void setCustomFilePath(String path) {
        customFileField.setText(path);
    }

    public String getCopyDirectoryPath() {
        return copyDirectoryField.getText();
    }

    public void setCopyDirectoryPath(String path) {
        copyDirectoryField.setText(path);
    }

    public boolean isCustomLogicEnabled() {
        return customLogicCheckBox.isSelected();
    }

    public void setCustomLogicEnabled(boolean enabled) {
        customLogicCheckBox.setSelected(enabled);
        updateComponentStates(enabled);
    }

}
