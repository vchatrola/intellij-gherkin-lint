package com.vchatrola.plugin.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import com.vchatrola.gemini.dto.GeminiRecords;
import com.vchatrola.gemini.service.GeminiService;
import com.vchatrola.plugin.service.GherkinLintServiceImpl;
import com.vchatrola.plugin.setting.GherkinLintSettingsState;
import com.vchatrola.util.ResourceUtil;

import javax.swing.*;
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
    private final JComboBox<ModelOption> modelComboBox;
    private final JButton loadModelsButton;
    private final JButton refreshModelsButton;
    private final JBLabel modelStatusLabel;
    private final JBLabel modelDetailsLabel;
    private final JBLabel quotaStatusLabel;
    private final JBLabel modelFetchWarningLabel;
    private final JBLabel modelFetchedAtLabel;
    private final JPasswordField apiKeyField;
    private final JBLabel apiKeyStatusLabel;
    private final JButton clearApiKeyButton;
    private final JBLabel instructionsLabel;
    private final Map<String, GeminiRecords.Model> modelDetailsByName = new HashMap<>();
    private final List<String> modelOrder = new ArrayList<>();

    public GherkinLintSettingsUI() {
        panel = new JPanel(new BorderLayout());
        customLogicCheckBox = createCustomLogicCheckBox();
        copyDirectoryField = createCopyDirectoryField();
        customFileField = createCustomFileField();
        copyButton = createCopyButton();
        modelComboBox = createModelComboBox();
        loadModelsButton = createLoadModelsButton();
        refreshModelsButton = createRefreshModelsButton();
        modelStatusLabel = createModelStatusLabel();
        modelDetailsLabel = createModelDetailsLabel();
        quotaStatusLabel = createQuotaStatusLabel();
        modelFetchWarningLabel = createModelFetchWarningLabel();
        modelFetchedAtLabel = createModelFetchedAtLabel();
        apiKeyField = createApiKeyField();
        apiKeyStatusLabel = createApiKeyStatusLabel();
        clearApiKeyButton = createClearApiKeyButton();
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
                .addLabeledComponent("Gemini model:", modelComboBox, 1, false)
                .addVerticalGap(5)
                .addComponent(createModelControlRow())
                .addVerticalGap(5)
                .addComponent(modelFetchWarningLabel)
                .addVerticalGap(5)
                .addComponent(modelFetchedAtLabel)
                .addVerticalGap(5)
                .addComponent(modelStatusLabel)
                .addVerticalGap(5)
                .addComponent(modelDetailsLabel)
                .addVerticalGap(5)
                .addComponent(quotaStatusLabel)
                .addVerticalGap(5)
                .addSeparator()
                .addVerticalGap(5)
                .addLabeledComponent("Gemini API key:", apiKeyField, 1, false)
                .addVerticalGap(5)
                .addComponent(createApiKeyControlRow())
                .addVerticalGap(5)
                .addComponent(apiKeyStatusLabel)
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

    private JComboBox<ModelOption> createModelComboBox() {
        JComboBox<ModelOption> comboBox = new JComboBox<>();
        comboBox.addItem(ModelOption.auto());
        comboBox.addActionListener(e -> updateModelDetails());
        loadCachedModels(comboBox);
        return comboBox;
    }

    private JButton createLoadModelsButton() {
        JButton button = new JButton("Load Models");
        button.addActionListener(e -> loadModelsAsync(modelComboBox));
        return button;
    }

    private JButton createRefreshModelsButton() {
        JButton button = new JButton("Refresh");
        button.addActionListener(e -> {
            clearModelCache();
            loadModelsAsync(modelComboBox);
        });
        return button;
    }

    private JBLabel createModelStatusLabel() {
        JBLabel label = new JBLabel("Models are not loaded yet. Using Auto by default.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelDetailsLabel() {
        JBLabel label = new JBLabel("Model limits: unavailable (models not loaded).");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createQuotaStatusLabel() {
        JBLabel label = new JBLabel("Quota remaining: not available via API. Check Google AI Studio usage.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelFetchWarningLabel() {
        JBLabel label = new JBLabel("Loading models makes an API call and uses quota.");
        label.setForeground(JBColor.GRAY);
        return label;
    }

    private JBLabel createModelFetchedAtLabel() {
        JBLabel label = new JBLabel("Models last fetched: never");
        label.setForeground(JBColor.GRAY);
        return label;
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
        panel.add(Box.createHorizontalStrut(6));
        panel.add(refreshModelsButton);
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
        setModelStatus("Model list loaded from cache.");
        updateFetchedAtLabel(state.getGeminiModelsFetchedAt());
        updateModelDetails();
    }

    private void loadModelsAsync(JComboBox<ModelOption> comboBox) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            GeminiService service = getGeminiService();
            if (service == null) {
                setModelStatus("Gemini service unavailable. Using Auto (server default).");
                return;
            }
            List<GeminiRecords.Model> models = service.getAvailableModels();
            if (models == null || models.isEmpty()) {
                setModelStatus("No models loaded from Gemini. Using Auto (server default).");
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
                setModelStatus("Model list loaded from Gemini.");
                persistModelCache(modelOrder);
                updateFetchedAtLabel(System.currentTimeMillis());
                updateModelDetails();
            });
        });
    }

    private void setModelStatus(String message) {
        SwingUtilities.invokeLater(() -> modelStatusLabel.setText(message));
    }

    private void updateModelDetails() {
        ModelOption option = (ModelOption) modelComboBox.getSelectedItem();
        if (option == null) {
            modelDetailsLabel.setText("Model limits: unavailable (no selection).");
            return;
        }

        String modelName = option.value();
        if (modelName.isEmpty()) {
            if (!modelOrder.isEmpty()) {
                String resolved = modelOrder.get(0);
                GeminiRecords.Model model = modelDetailsByName.get(resolved);
                modelDetailsLabel.setText(formatModelDetails("Auto resolves to", resolved, model));
            } else {
                modelDetailsLabel.setText("Model limits: unavailable (models not loaded).");
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
        setModelStatus("Model cache cleared. Using Auto by default.");
        updateModelDetails();
    }

    private void updateFetchedAtLabel(long fetchedAt) {
        if (fetchedAt <= 0L) {
            modelFetchedAtLabel.setText("Models last fetched: never");
            return;
        }
        modelFetchedAtLabel.setText("Models last fetched: " + new java.util.Date(fetchedAt));
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
