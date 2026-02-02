package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ConfigurationLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationLoader.class.getName());

    public JsonNode loadDefaultConfiguration() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "default-rules.json")) {
            if (inputStream == null) {
                LOGGER.severe("Failed to load default configuration file from classpath: " +
                        "default-rules.json");
                throw new FileNotFoundException("Default configuration file not found in classpath");
            }
            return mapper.readTree(inputStream);
        }
    }

    public JsonNode loadCustomConfiguration(String customConfigPath) {
        if (customConfigPath == null || customConfigPath.isEmpty()) {
            LOGGER.warning("Custom configuration path is empty or null. Returning null.");
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(new File(customConfigPath));
        } catch (FileNotFoundException e) {
            LOGGER.severe("Custom configuration file not found: " + customConfigPath);
            return null;
        } catch (IOException e) {
            LOGGER.severe("Error while reading custom configuration file: " + customConfigPath + ", " + e.getMessage());
            return null;
        }
    }
}
