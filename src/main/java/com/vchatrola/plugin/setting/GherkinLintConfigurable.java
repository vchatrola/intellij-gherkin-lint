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
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        return settings.isCustomLogicEnabled() != gherkinLintSettingsUI.isCustomLogicEnabled()
                || !settings.getCustomFilePath().equals(gherkinLintSettingsUI.getCustomFilePath())
                || !settings.getCopyDirectoryPath().equals(gherkinLintSettingsUI.getCopyDirectoryPath());
    }

    @Override
    public void apply() {
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        settings.customLogicEnabled = gherkinLintSettingsUI.isCustomLogicEnabled();
        settings.customFilePath = gherkinLintSettingsUI.getCustomFilePath();
        settings.copyDirectoryPath = gherkinLintSettingsUI.getCopyDirectoryPath();
    }

    @Override
    public void reset() {
        GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
        gherkinLintSettingsUI.setCustomLogicEnabled(settings.customLogicEnabled);
        gherkinLintSettingsUI.setCustomFilePath(settings.customFilePath);
        gherkinLintSettingsUI.setCopyDirectoryPath(settings.copyDirectoryPath);
    }

    @Override
    public void disposeUIResources() {
        gherkinLintSettingsUI = null;
    }
}

