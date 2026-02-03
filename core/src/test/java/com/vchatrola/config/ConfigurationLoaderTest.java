package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadDefaultConfiguration_readsFromClasspath() throws Exception {
        ConfigurationLoader loader = new ConfigurationLoader();
        JsonNode node = loader.loadDefaultConfiguration();
        assertNotNull(node);
        assertEquals("Test context", node.get("CONTEXT").asText());
    }

    @Test
    void loadCustomConfiguration_returnsNullForMissingFile() {
        ConfigurationLoader loader = new ConfigurationLoader();
        JsonNode node = loader.loadCustomConfiguration(tempDir.resolve("missing.json").toString());
        assertNull(node);
    }

    @Test
    void loadCustomConfiguration_readsValidFile() throws Exception {
        Path file = tempDir.resolve("custom.json");
        Files.writeString(file, "{ \"CUSTOM\": true }");
        ConfigurationLoader loader = new ConfigurationLoader();
        JsonNode node = loader.loadCustomConfiguration(file.toString());
        assertNotNull(node);
        assertTrue(node.get("CUSTOM").asBoolean());
    }
}
