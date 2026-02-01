package com.vchatrola.plugin.setting;

import java.util.List;

public class GherkinLintSettingsManager {

    private final GherkinLintSettingsState settingsState;

    public GherkinLintSettingsManager() {
        this.settingsState = GherkinLintSettingsState.getInstance();
    }

    public boolean isCustomLogicEnabled() {
        return settingsState.customLogicEnabled;
    }

    public String getCustomFilePath() {
        return settingsState.customFilePath;
    }

    public String getCopyDirectoryPath() {
        return settingsState.copyDirectoryPath;
    }

    public String getGeminiModel() {
        return settingsState.geminiModel;
    }

    public void setCustomLogicEnabled(boolean enabled) {
        settingsState.customLogicEnabled = enabled;
    }

    public void setCustomFilePath(String path) {
        settingsState.customFilePath = path;
    }

    public void setCopyDirectoryPath(String path) {
        settingsState.copyDirectoryPath = path;
    }

    public void setGeminiModel(String model) {
        settingsState.geminiModel = model;
    }

    public List<String> getGeminiModels() {
        return settingsState.geminiModels;
    }

    public long getGeminiModelsFetchedAt() {
        return settingsState.geminiModelsFetchedAt;
    }

    public void setGeminiModels(List<String> models, long fetchedAt) {
        settingsState.geminiModels = models;
        settingsState.geminiModelsFetchedAt = fetchedAt;
    }

    public void saveSettings() {
        settingsState.loadState(settingsState);
    }

}
