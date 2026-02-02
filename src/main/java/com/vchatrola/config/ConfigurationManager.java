package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.vchatrola.plugin.setting.GherkinLintSettingsManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class ConfigurationManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationManager.class.getName());
    private static final Object CACHE_LOCK = new Object();
    private static CachedConfig cachedConfig;

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
        long customLastModified = resolveLastModified(customFilePath, isCustomLogicEnabled);

        CachedConfig cached = getCachedConfig();
        if (cached != null
                && cached.matches(isCustomLogicEnabled, customFilePath, customLastModified)) {
            return cached.mergedConfig();
        }

        JsonNode defaultConfig = loader.loadDefaultConfiguration();
        JsonNode customConfig = isCustomLogicEnabled ? loader.loadCustomConfiguration(customFilePath) : null;

        JsonNode merged = merger.mergeConfigurations(defaultConfig, customConfig);
        setCachedConfig(new CachedConfig(
                merged,
                isCustomLogicEnabled,
                customFilePath,
                customLastModified
        ));
        return merged;
    }

    public static void invalidateCache() {
        synchronized (CACHE_LOCK) {
            cachedConfig = null;
        }
    }

    private static long resolveLastModified(String customFilePath, boolean isCustomLogicEnabled) {
        if (!isCustomLogicEnabled || customFilePath == null || customFilePath.isBlank()) {
            return -1L;
        }
        File file = new File(customFilePath);
        return file.exists() ? file.lastModified() : -1L;
    }

    private static CachedConfig getCachedConfig() {
        synchronized (CACHE_LOCK) {
            return cachedConfig;
        }
    }

    private static void setCachedConfig(CachedConfig config) {
        synchronized (CACHE_LOCK) {
            cachedConfig = config;
        }
        LOGGER.info("Configuration cache updated.");
    }

    private record CachedConfig(JsonNode mergedConfig,
                                boolean customLogicEnabled,
                                String customFilePath,
                                long customLastModified) {
        boolean matches(boolean enabled, String path, long lastModified) {
            return customLogicEnabled == enabled
                    && Objects.equals(customFilePath, path)
                    && customLastModified == lastModified;
        }
    }
}
