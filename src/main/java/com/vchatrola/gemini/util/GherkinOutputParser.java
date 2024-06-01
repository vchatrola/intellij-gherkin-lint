package com.vchatrola.gemini.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vchatrola.plugin.util.PluginConstants;

public class GherkinOutputParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_ARRAY_OPEN = "[";
    private static final String JSON_ARRAY_CLOSE = "]";
    private static final String SEPARATOR = "|";

    public static String parseOutput(String jsonString) throws JsonProcessingException {
        StringBuilder resultBuilder = new StringBuilder();
        jsonString = extractJsonContent(jsonString);
        JsonNode jsonArray = objectMapper.readTree(jsonString);

        for (JsonNode jsonObject : jsonArray) {
            String title = jsonObject.get(PluginConstants.PROPERTY_TITLE.toLowerCase()).asText();
            String status = jsonObject.get(PluginConstants.PROPERTY_STATUS.toLowerCase()).asText();
            String reason = jsonObject.get(PluginConstants.PROPERTY_REASON.toLowerCase()).asText();
            String suggestion = jsonObject.get(PluginConstants.PROPERTY_SUGGESTION.toLowerCase()).asText();

            resultBuilder.append(PluginConstants.PROPERTY_TITLE).append(SEPARATOR).append(title).append("\n");
            resultBuilder.append(PluginConstants.PROPERTY_STATUS).append(SEPARATOR).append(status).append("\n");
            resultBuilder.append(PluginConstants.PROPERTY_REASON).append(SEPARATOR).append(reason).append("\n");
            resultBuilder.append(PluginConstants.PROPERTY_SUGGESTION).append(SEPARATOR).append(suggestion).append("\n\n");
        }

        return resultBuilder.toString();

    }

    private static String extractJsonContent(String response) {
        int startIndex = response.indexOf(JSON_ARRAY_OPEN);
        int endIndex = response.lastIndexOf(JSON_ARRAY_CLOSE);

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return response.substring(startIndex, endIndex + 1);
        } else {
            return null;
        }
    }

}