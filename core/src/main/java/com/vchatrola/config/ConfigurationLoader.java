package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vchatrola.util.GherkinLintLogger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationLoader {
  public JsonNode loadDefaultConfiguration() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream("default-rules.json")) {
      if (inputStream == null) {
        GherkinLintLogger.error(
            "Failed to load default configuration file from classpath: " + "default-rules.json");
        throw new FileNotFoundException("Default configuration file not found in classpath");
      }
      return mapper.readTree(inputStream);
    }
  }

  public JsonNode loadCustomConfiguration(String customConfigPath) {
    if (customConfigPath == null || customConfigPath.isEmpty()) {
      GherkinLintLogger.debug("Custom configuration path is empty or null. Returning null.");
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    Path path = Paths.get(customConfigPath);
    String fileName = path.getFileName() != null ? path.getFileName().toString() : "custom rules";
    try {
      if (!Files.exists(path)) {
        throw new FileNotFoundException("Custom configuration file not found.");
      }
      return mapper.readTree(path.toFile());
    } catch (FileNotFoundException e) {
      GherkinLintLogger.info("Custom configuration file not found: " + fileName);
      return null;
    } catch (IOException e) {
      GherkinLintLogger.info("Error while reading custom configuration file: " + fileName);
      return null;
    }
  }
}
