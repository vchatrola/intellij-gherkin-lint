package com.vchatrola.plugin.setting;

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

    public void setCustomLogicEnabled(boolean enabled) {
        settingsState.customLogicEnabled = enabled;
    }

    public void setCustomFilePath(String path) {
        settingsState.customFilePath = path;
    }

    public void setCopyDirectoryPath(String path) {
        settingsState.copyDirectoryPath = path;
    }

    public void saveSettings() {
        settingsState.loadState(settingsState);
    }

}
