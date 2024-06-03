package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.vchatrola.plugin.setting.GherkinLintSettingsManager;

import java.io.IOException;

public class ConfigurationManager {

    private final ConfigurationLoader loader;
    private final ConfigurationMerger merger;

    public ConfigurationManager() {
        this.loader = new ConfigurationLoader();
        this.merger = new ConfigurationMerger();
    }

    public JsonNode getFinalConfiguration() throws IOException {
        GherkinLintSettingsManager settingsManager = new GherkinLintSettingsManager();
        String customFilePath = settingsManager.getCustomFilePath();
        boolean isCustomLogicEnabled = settingsManager.isCustomLogicEnabled();

        JsonNode defaultConfig = loader.loadDefaultConfiguration();
        JsonNode customConfig = isCustomLogicEnabled ? loader.loadCustomConfiguration(customFilePath) : null;

        return merger.mergeConfigurations(defaultConfig, customConfig);
    }
}
