package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vchatrola.util.GherkinLintLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ConfigurationLoader {

    public JsonNode loadDefaultConfiguration() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "default-validation-rules.json")) {
            if (inputStream == null) {
                GherkinLintLogger.error("Failed to load default configuration file from classpath: " +
                        "default-validation-rules.json");
                throw new FileNotFoundException("Default configuration file not found in classpath");
            }
            return mapper.readTree(inputStream);
        }
    }

    public JsonNode loadCustomConfiguration(String customConfigPath) {
        if (customConfigPath == null || customConfigPath.isEmpty()) {
            GherkinLintLogger.warn("Custom configuration path is empty or null. Returning null.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(new File(customConfigPath));
        } catch (FileNotFoundException e) {
            GherkinLintLogger.error("Custom configuration file not found: " + customConfigPath);
            return null;
        } catch (IOException e) {
            GherkinLintLogger.error("Error while reading custom configuration file: " + customConfigPath + ", " + e.getMessage());
            return null;
        }
    }
}
