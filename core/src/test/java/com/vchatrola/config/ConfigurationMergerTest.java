package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfigurationMergerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mergeConfigurations_mergesObjectsAndReplacesArrays() throws Exception {
        JsonNode defaultConfig = mapper.readTree("""
                {
                  "A": { "B": 1, "C": 2 },
                  "ARR": [ { "k": "v1" }, { "k": "v2" } ],
                  "STR": "x"
                }
                """);
        JsonNode customConfig = mapper.readTree("""
                {
                  "A": { "C": 3 },
                  "ARR": [ { "k": "v3" } ],
                  "STR": "y",
                  "NEW": "ignored"
                }
                """);

        JsonNode merged = new ConfigurationMerger().mergeConfigurations(defaultConfig, customConfig);

        assertEquals(1, merged.get("A").get("B").asInt());
        assertEquals(3, merged.get("A").get("C").asInt());
        assertEquals("y", merged.get("STR").asText());
        assertEquals(1, merged.get("ARR").size());
        assertEquals("v3", merged.get("ARR").get(0).get("k").asText());
        assertFalse(merged.has("NEW"));
    }

    @Test
    void mergeConfigurations_replacesArrayWithEmptyCustomArray() throws Exception {
        JsonNode defaultConfig = mapper.readTree("""
                {
                  "ARR": [ { "k": "v1" } ]
                }
                """);
        JsonNode customConfig = mapper.readTree("""
                {
                  "ARR": []
                }
                """);

        JsonNode merged = new ConfigurationMerger().mergeConfigurations(defaultConfig, customConfig);

        assertEquals(0, merged.get("ARR").size());
    }
}
