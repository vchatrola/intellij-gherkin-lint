package com.vchatrola.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Logger;

public class ConfigurationMerger {
  private static final Logger LOGGER = Logger.getLogger(ConfigurationMerger.class.getName());

  public JsonNode mergeConfigurations(JsonNode defaultConfig, JsonNode customConfig) {
    if (customConfig == null) {
      LOGGER.info("Custom configuration is null, returning default configuration.");
      return defaultConfig;
    }

    merge(defaultConfig, customConfig);
    return defaultConfig;
  }

  private void merge(JsonNode defaultNode, JsonNode customNode) {
    customNode
        .fields()
        .forEachRemaining(
            entry -> {
              String key = entry.getKey();
              JsonNode customValue = entry.getValue();

              if (defaultNode.has(key)) {
                JsonNode defaultValue = defaultNode.get(key);

                if (defaultValue.isObject() && customValue.isObject()) {
                  merge(defaultValue, customValue);
                } else if (defaultValue.isArray() && customValue.isArray()) {
                  mergeArrays((ArrayNode) defaultValue, (ArrayNode) customValue);
                } else {
                  ((ObjectNode) defaultNode).replace(key, customValue);
                }
              }
            });
  }

  private void mergeArrays(ArrayNode defaultArray, ArrayNode customArray) {
    defaultArray.removeAll();

    for (int i = 0; i < customArray.size(); i++) {
      JsonNode customElement = customArray.get(i);

      if (i < defaultArray.size()) {
        JsonNode defaultElement = defaultArray.get(i);

        if (defaultElement.isObject() && customElement.isObject()) {
          merge(defaultElement, customElement);
        } else {
          defaultArray.set(i, customElement);
        }
      } else {
        defaultArray.add(customElement);
      }
    }
  }
}
