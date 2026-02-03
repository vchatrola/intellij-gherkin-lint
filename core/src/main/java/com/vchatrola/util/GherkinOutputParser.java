package com.vchatrola.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GherkinOutputParser {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String JSON_ARRAY_OPEN = "[";
  private static final String JSON_ARRAY_CLOSE = "]";
  private static final String SEPARATOR = "|";

  public static String parseOutput(String jsonString) throws JsonProcessingException {
    StringBuilder resultBuilder = new StringBuilder();
    jsonString = extractJsonContent(jsonString);
    if (jsonString == null || jsonString.isBlank()) {
      throw new IllegalArgumentException("Gemini response did not contain a JSON array.");
    }
    JsonNode jsonArray = objectMapper.readTree(jsonString);
    if (!jsonArray.isArray()) {
      throw new IllegalArgumentException("Gemini response JSON must be an array.");
    }

    for (JsonNode jsonObject : jsonArray) {
      String title = getOptionalText(jsonObject, Constants.PROPERTY_TITLE.toLowerCase());
      String status = getOptionalText(jsonObject, Constants.PROPERTY_STATUS.toLowerCase());
      String reason = getOptionalText(jsonObject, Constants.PROPERTY_REASON.toLowerCase());
      String suggestion = getOptionalText(jsonObject, Constants.PROPERTY_SUGGESTION.toLowerCase());

      String missingFields = getMissingFields(title, status, reason, suggestion);
      if (!missingFields.isBlank()) {
        title = title.isBlank() ? "Unknown" : title;
        status = Constants.STATUS_INVALID;
        reason = "Missing required field(s): " + missingFields;
        suggestion = "Ensure Gemini returns title/status/reason/suggestion for each item.";
      }

      resultBuilder.append(Constants.PROPERTY_TITLE).append(SEPARATOR).append(title).append("\n");
      resultBuilder.append(Constants.PROPERTY_STATUS).append(SEPARATOR).append(status).append("\n");
      resultBuilder.append(Constants.PROPERTY_REASON).append(SEPARATOR).append(reason).append("\n");
      resultBuilder
          .append(Constants.PROPERTY_SUGGESTION)
          .append(SEPARATOR)
          .append(suggestion)
          .append("\n\n");
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

  private static String getOptionalText(JsonNode node, String field) {
    if (node == null || field == null) {
      return "";
    }
    JsonNode value = node.get(field);
    return value != null && !value.isNull() ? value.asText() : "";
  }

  private static String getMissingFields(
      String title, String status, String reason, String suggestion) {
    StringBuilder missing = new StringBuilder();
    if (title == null || title.isBlank()) {
      missing.append("title");
    }
    if (status == null || status.isBlank()) {
      appendMissing(missing, "status");
    }
    if (reason == null || reason.isBlank()) {
      appendMissing(missing, "reason");
    }
    if (suggestion == null || suggestion.isBlank()) {
      appendMissing(missing, "suggestion");
    }
    return missing.toString();
  }

  private static void appendMissing(StringBuilder missing, String field) {
    if (!missing.isEmpty()) {
      missing.append(", ");
    }
    missing.append(field);
  }
}
