package com.vchatrola.plugin.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.vchatrola.gemini.dto.GeminiRecords;
import com.vchatrola.gemini.service.GeminiService;
import com.vchatrola.plugin.service.GherkinLintServiceImpl;
import com.vchatrola.util.GherkinLintLogger;
import com.vchatrola.util.ResourceUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GherkinLintSettingsUI {
    private final JPanel panel;
    private final JBCheckBox customLogicCheckBox;
    private final TextFieldWithBrowseButton copyDirectoryField;
    private final TextFieldWithBrowseButton customFileField;
    private final JButton copyButton;
    private final JBLabel customRulesWarningLabel;
    private final JComboBox<ModelOption> modelComboBox;
    private final JButton loadModelsButton;
    private final JBLabel modelStatusLabel;
    private final JBLabel modelDetailsLabel;
    private final JBLabel quotaStatusLabel;
    private final JBLabel modelFetchWarningLabel;
    private final JBLabel modelFetchedAtLabel;
    private final JBLabel modelCacheNoteLabel;
    private final JComponent modelInfoPanel;
    private final JPasswordField apiKeyField;
    private final JBLabel apiKeyStatusLabel;
    private final JButton clearApiKeyButton;
    private final JBLabel privacyNoticeLabel;
    private final JBLabel instructionsLabel;
    private final Map<String, GeminiRecords.Model> modelDetailsByName = new HashMap<>();
    private final List<String> modelOrder = new ArrayList<>();

    public GherkinLintSettingsUI() {
        panel = new JPanel(new BorderLayout());
        customLogicCheckBox = createCustomLogicCheckBox();
        copyDirectoryField = createCopyDirectoryField();
        customFileField = createCustomFileField();
        copyButton = createCopyButton();
        customRulesWarningLabel = createCustomRulesWarningLabel();
        modelStatusLabel = createModelStatusLabel();
        modelDetailsLabel = createModelDetailsLabel();
        quotaStatusLabel = createQuotaStatusLabel();
        modelFetchWarningLabel = createModelFetchWarningLabel();
        modelFetchedAtLabel = createModelFetchedAtLabel();
        modelCacheNoteLabel = createModelCacheNoteLabel();
        modelInfoPanel = createModelInfoPanel();
        modelComboBox = createModelComboBox();
        loadModelsButton = createLoadModelsButton();
        apiKeyField = createApiKeyField();
        apiKeyStatusLabel = createApiKeyStatusLabel();
        clearApiKeyButton = createClearApiKeyButton();
        privacyNoticeLabel = createPrivacyNoticeLabel();
        instructionsLabel = createInstructionsLabel();
        loadCachedModels(modelComboBox);

        // Add components to form panel using FormBuilder
        JPanel formPanel = FormBuilder.createFormBuilder()
                .addComponent(new TitledSeparator("Gemini API"))
                .addVerticalGap(4)
                .addLabeledComponent("API key:", apiKeyField, 1, false)
                .addVerticalGap(4)
                .addComponent(createApiKeyControlRow())
                .addVerticalGap(4)
                .addComponent(apiKeyStatusLabel)
                .addVerticalGap(4)
                .addComponent(privacyNoticeLabel)
                .addVerticalGap(8)
                .addComponent(new TitledSeparator("Custom Rules"))
                .addVerticalGap(4)
                .addComponent(customLogicCheckBox)
                .addVerticalGap(5)
                .addLabeledComponent("Custom rules file:", customFileField, 1, false)
                .addVerticalGap(4)
                .addComponent(customRulesWarningLabel)
                .addVerticalGap(4)
                .addLabeledComponent("Copy sample file to:", copyDirectoryField, 1, false)
                .addVerticalGap(4)
                .addComponent(copyButton)
                .addVerticalGap(8)
                .addComponent(instructionsLabel)
                .addVerticalGap(8)
                .addComponent(new TitledSeparator("Model Selection"))
                .addVerticalGap(4)
                .addLabeledComponent("Gemini model:", modelComboBox, 1, false)
                .addVerticalGap(4)
                .addComponent(createModelControlRow())
                .addVerticalGap(4)
                .addComponent(modelCacheNoteLabel)
                .addVerticalGap(4)
                .addComponent(modelInfoPanel)
                .addVerticalGap(8)
                .getPanel();

        panel.add(formPanel, BorderLayout.NORTH);

        // Update component states based on checkbox selection
        customLogicCheckBox.addActionListener(e -> updateComponentStates(customLogicCheckBox.isSelected()));
        customFileField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCustomRulesWarning();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCustomRulesWarning();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCustomRulesWarning();
            }
        });
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

    private JBLabel createCustomRulesWarningLabel() {
        JBLabel label = new JBLabel("Custom rules file is required when custom validation is enabled.");
        label.setForeground(JBColor.RED);
        label.setVisible(false);
        return label;
    }

    private JBLabel createInstructionsLabel() {
        JBLabel label = new JBLabel("<html><body style='width: 400px'>" +
                "Custom rules let you enforce your team’s Gherkin style. " +
                "Enable custom validation, copy the sample file, edit it, and select it above. " +
                "Custom rules are included in Gemini requests—avoid sensitive content." +
                "</body></html>");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JComboBox<ModelOption> createModelComboBox() {
        JComboBox<ModelOption> comboBox = new JComboBox<>();
        comboBox.addItem(ModelOption.auto());
        comboBox.addActionListener(e -> updateModelDetails());
        return comboBox;
    }

    private JButton createLoadModelsButton() {
        JButton button = new JButton("Load / Refresh Models");
        button.addActionListener(e -> {
            clearModelCache();
            loadModelsAsync(modelComboBox);
        });
        return button;
    }

    private JBLabel createModelStatusLabel() {
        JBLabel label = new JBLabel("Models not loaded; Auto will be used.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelDetailsLabel() {
        JBLabel label = new JBLabel("Model limits unavailable until loaded.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createQuotaStatusLabel() {
        JBLabel label = new JBLabel("Quota remaining isn’t shown here.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelFetchWarningLabel() {
        JBLabel label = new JBLabel("Loading models uses API quota.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelFetchedAtLabel() {
        JBLabel label = new JBLabel("Last fetched: never");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelCacheNoteLabel() {
        JBLabel label = new JBLabel("Models are cached after the first validation run.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JComponent createModelInfoPanel() {
        JPanel panel = new JBPanel<>(new GridBagLayout());
        GridBagConstraints row = new GridBagConstraints();
        row.gridx = 0;
        row.weightx = 1.0;
        row.anchor = GridBagConstraints.WEST;
        row.fill = GridBagConstraints.HORIZONTAL;

        row.gridy = 0;
        panel.add(modelFetchWarningLabel, row);
        row.gridy = 1;
        panel.add(modelFetchedAtLabel, row);
        row.gridy = 2;
        panel.add(modelStatusLabel, row);
        row.gridy = 3;
        panel.add(modelDetailsLabel, row);
        row.gridy = 4;
        panel.add(quotaStatusLabel, row);
        return panel;
    }


    private JPasswordField createApiKeyField() {
        JPasswordField field = new JPasswordField();
        field.setColumns(30);
        return field;
    }

    private JBLabel createApiKeyStatusLabel() {
        JBLabel label = new JBLabel(getApiKeyStatusText());
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createPrivacyNoticeLabel() {
        JBLabel label = new JBLabel("<html><body style='width: 420px'>" +
                "Selected Gherkin text is sent to Gemini for validation. " +
                "Avoid sharing sensitive or confidential data." +
                "</body></html>");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private String getApiKeyStatusText() {
        return GherkinLintSecrets.hasApiKey()
                ? "API key stored securely in IDE."
                : "No API key stored. Environment variable GOOGLE_API_KEY will be used if set.";
    }

    private JButton createClearApiKeyButton() {
        JButton button = new JButton("Clear Stored Key");
        button.addActionListener(e -> {
            GherkinLintSecrets.saveApiKey("");
            resetApiKeyField();
        });
        return button;
    }

    private JComponent createApiKeyControlRow() {
        JPanel panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(clearApiKeyButton);
        return panel;
    }

    private JComponent createModelControlRow() {
        JPanel panel = new JBPanel<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(loadModelsButton);
        return panel;
    }

    private void loadCachedModels(JComboBox<ModelOption> comboBox) {
        GherkinLintSettingsState state = GherkinLintSettingsState.getInstance();
        List<String> cached = state.getGeminiModels();
        if (cached == null || cached.isEmpty()) {
            return;
        }
        for (String model : cached) {
            comboBox.addItem(new ModelOption(model, model));
            modelOrder.add(model);
        }
        setModelStatus("Model list loaded from cache.", JBColor.GRAY);
        updateFetchedAtLabel(state.getGeminiModelsFetchedAt());
        if (state.getGeminiModel().isBlank() && !modelOrder.isEmpty()) {
            comboBox.setSelectedItem(new ModelOption(modelOrder.get(0), modelOrder.get(0)));
        }
        updateModelDetails();
    }

    private void loadModelsAsync(JComboBox<ModelOption> comboBox) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            GeminiService service = getGeminiService();
            if (service == null) {
                setModelStatus("Gemini service unavailable. Using Auto (server default).", JBColor.RED);
                GherkinLintLogger.warn("Gemini service unavailable while loading models.");
                return;
            }
            GeminiService.clearCachedModels();
            List<GeminiRecords.Model> models = service.getAvailableModels();
            if (models == null || models.isEmpty()) {
                setModelStatus("No models loaded from Gemini. Using Auto (server default).", JBColor.RED);
                GherkinLintLogger.warn("No Gemini models returned by API.");
                return;
            }

            SwingUtilities.invokeLater(() -> {
                modelDetailsByName.clear();
                modelOrder.clear();
                comboBox.removeAllItems();
                comboBox.addItem(ModelOption.auto());
                for (GeminiRecords.Model model : models) {
                    String normalizedName = normalizeModelName(model.name());
                    modelDetailsByName.put(normalizedName, model);
                    modelOrder.add(normalizedName);
                    comboBox.addItem(new ModelOption(normalizedName, normalizedName));
                }
                setModelStatus("Model list loaded from Gemini.", JBColor.GREEN);
                persistModelCache(modelOrder);
                GherkinLintLogger.info("Loaded " + modelOrder.size() + " Gemini models.");
                if (!modelOrder.isEmpty() && isAutoSelected()) {
                    comboBox.setSelectedItem(new ModelOption(modelOrder.get(0), modelOrder.get(0)));
                }
                updateFetchedAtLabel(System.currentTimeMillis());
                updateModelDetails();
            });
        });
    }

    private void setModelStatus(String message, JBColor color) {
        SwingUtilities.invokeLater(() -> {
            modelStatusLabel.setText(message);
            modelStatusLabel.setForeground(color);
        });
    }

    private Project getAnyOpenProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0];
        }
        return null;
    }

    private void updateModelDetails() {
        if (modelComboBox == null) {
            return;
        }
        ModelOption option = (ModelOption) modelComboBox.getSelectedItem();
        if (option == null) {
            modelDetailsLabel.setText("Model limits unavailable (no selection).");
            return;
        }

        String modelName = option.value();
        if (modelName.isEmpty()) {
            if (!modelOrder.isEmpty()) {
                String resolved = modelOrder.get(0);
                GeminiRecords.Model model = modelDetailsByName.get(resolved);
                modelDetailsLabel.setText(formatModelDetails("Auto resolves to", resolved, model));
            } else {
                modelDetailsLabel.setText("Model limits unavailable until loaded.");
            }
            return;
        }

        GeminiRecords.Model model = modelDetailsByName.get(modelName);
        modelDetailsLabel.setText(formatModelDetails("Model limits", modelName, model));
    }

    private String formatModelDetails(String prefix, String name, GeminiRecords.Model model) {
        if (model == null) {
            return prefix + ": " + name;
        }
        return String.format("%s: %s (input %d, output %d tokens)",
                prefix, name, model.inputTokenLimit(), model.outputTokenLimit());
    }

    private static String normalizeModelName(String modelName) {
        if (modelName == null) {
            return "";
        }
        if (modelName.startsWith("models/")) {
            return modelName.substring("models/".length());
        }
        return modelName;
    }

    private void persistModelCache(List<String> models) {
        GherkinLintSettingsState state = GherkinLintSettingsState.getInstance();
        state.geminiModels = new ArrayList<>(models);
        state.geminiModelsFetchedAt = System.currentTimeMillis();
    }

    private void clearModelCache() {
        GherkinLintSettingsState state = GherkinLintSettingsState.getInstance();
        state.geminiModels = new ArrayList<>();
        state.geminiModelsFetchedAt = 0L;
        modelDetailsByName.clear();
        modelOrder.clear();
        modelComboBox.removeAllItems();
        modelComboBox.addItem(ModelOption.auto());
        updateFetchedAtLabel(0L);
        setModelStatus("Model cache cleared. Using Auto by default.", JBColor.GRAY);
        updateModelDetails();
    }

    private void updateFetchedAtLabel(long fetchedAt) {
        if (fetchedAt <= 0L) {
            modelFetchedAtLabel.setText("Last fetched: never");
            return;
        }
        modelFetchedAtLabel.setText("Last fetched: " + new java.util.Date(fetchedAt));
    }

    private GeminiService getGeminiService() {
        GherkinLintServiceImpl gherkinLintServiceImpl =
                ApplicationManager.getApplication().getService(GherkinLintServiceImpl.class);
        return gherkinLintServiceImpl != null ? gherkinLintServiceImpl.getGeminiService() : null;
    }

    private void updateComponentStates(boolean selected) {
        customFileField.setEnabled(selected);
        copyDirectoryField.setEnabled(selected);
        copyButton.setEnabled(selected);
        updateCustomRulesWarning();
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

    public void updateCustomRulesWarning() {
        boolean show = customLogicCheckBox.isSelected() && customFileField.getText().trim().isEmpty();
        customRulesWarningLabel.setVisible(show);
    }

    public String getApiKey() {
        return new String(apiKeyField.getPassword()).trim();
    }

    public void resetApiKeyField() {
        apiKeyField.setText("");
        apiKeyStatusLabel.setText(getApiKeyStatusText());
    }

    public String getGeminiModel() {
        ModelOption option = (ModelOption) modelComboBox.getSelectedItem();
        return option != null ? option.value() : "";
    }

    private boolean isAutoSelected() {
        ModelOption option = (ModelOption) modelComboBox.getSelectedItem();
        return option == null || option.value().isEmpty();
    }

    public void setGeminiModel(String model) {
        for (int i = 0; i < modelComboBox.getItemCount(); i++) {
            ModelOption option = modelComboBox.getItemAt(i);
            if (option.value().equals(model)) {
                modelComboBox.setSelectedItem(option);
                return;
            }
        }
        modelComboBox.setSelectedItem(ModelOption.auto());
    }

    private record ModelOption(String label, String value) {
        static ModelOption auto() {
            return new ModelOption("Auto (server default)", "");
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
