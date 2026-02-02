package com.vchatrola.plugin.setting;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GherkinLintConfigurable implements Configurable {
    private GherkinLintSettingsUI gherkinLintSettingsUI;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "GherkinLint";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        gherkinLintSettingsUI = new GherkinLintSettingsUI();
        reset();
        return gherkinLintSettingsUI.createPanel();
    }

    @Override
    public boolean isModified() {
        if (gherkinLintSettingsUI == null) {
            return false;
        }
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        String apiKey = gherkinLintSettingsUI.getApiKey();
        return settings.isCustomLogicEnabled() != gherkinLintSettingsUI.isCustomLogicEnabled()
                || !settings.getCustomFilePath().equals(gherkinLintSettingsUI.getCustomFilePath())
                || !settings.getCopyDirectoryPath().equals(gherkinLintSettingsUI.getCopyDirectoryPath())
                || !settings.getGeminiModel().equals(gherkinLintSettingsUI.getGeminiModel())
                || !apiKey.isEmpty();
    }

    @Override
    public void apply() {
        if (gherkinLintSettingsUI == null) {
            return;
        }
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        boolean customEnabled = gherkinLintSettingsUI.isCustomLogicEnabled();
        if (customEnabled && gherkinLintSettingsUI.getCustomFilePath().trim().isEmpty()) {
            customEnabled = false;
        }
        settings.customLogicEnabled = customEnabled;
        settings.customFilePath = gherkinLintSettingsUI.getCustomFilePath();
        settings.copyDirectoryPath = gherkinLintSettingsUI.getCopyDirectoryPath();
        settings.geminiModel = gherkinLintSettingsUI.getGeminiModel();
        String apiKey = gherkinLintSettingsUI.getApiKey();
        if (!apiKey.isEmpty()) {
            GherkinLintSecrets.saveApiKey(apiKey);
            com.vchatrola.gemini.service.GeminiService.clearCachedModels();
        }
        gherkinLintSettingsUI.resetApiKeyField();
        gherkinLintSettingsUI.setCustomLogicEnabled(customEnabled);
        gherkinLintSettingsUI.updateCustomRulesWarning();
    }

    @Override
    public void reset() {
        if (gherkinLintSettingsUI == null) {
            return;
        }
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        gherkinLintSettingsUI.setCustomLogicEnabled(settings.customLogicEnabled);
        gherkinLintSettingsUI.setCustomFilePath(settings.customFilePath);
        gherkinLintSettingsUI.setCopyDirectoryPath(settings.copyDirectoryPath);
        gherkinLintSettingsUI.setGeminiModel(settings.geminiModel);
        gherkinLintSettingsUI.resetApiKeyField();
    }

    @Override
    public void disposeUIResources() {
        gherkinLintSettingsUI = null;
    }
}

