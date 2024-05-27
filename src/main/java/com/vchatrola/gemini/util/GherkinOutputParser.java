package com.vchatrola.gemini.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GherkinOutputParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String JSON_ARRAY_OPEN = "[";
    private static final String JSON_ARRAY_CLOSE = "]";

    public static String parseOutput(String jsonString) throws JsonProcessingException {
        StringBuilder resultBuilder = new StringBuilder();
        jsonString = extractJsonContent(jsonString);
        JsonNode jsonArray = objectMapper.readTree(jsonString);

        for (JsonNode jsonObject : jsonArray) {
            String title = jsonObject.get("title").asText();
            String status = jsonObject.get("status").asText();
            String reason = jsonObject.get("reason").asText();
            String suggestion = jsonObject.get("suggestion").asText();

            resultBuilder.append("Title|").append(title).append("\n");
            resultBuilder.append("Status|").append(status).append("\n");
            resultBuilder.append("Reason|").append(reason).append("\n");
            resultBuilder.append("Suggestion|").append(suggestion).append("\n\n");
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