package com.vchatrola.gemini.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vchatrola.gemini.api.GeminiApiException;
import com.vchatrola.gemini.api.GeminiClient;
import com.vchatrola.gemini.dto.GeminiRecords;
import org.junit.jupiter.api.Test;

class GeminiServiceTest {

  @Test
  void getAvailableModels_propagatesApiExceptions() {
    GeminiService.clearCachedModels();
    GeminiClient client =
        new GeminiClient() {
          @Override
          public GeminiRecords.ModelList getModels(String apiKey) {
            throw new GeminiApiException(400, "INVALID_ARGUMENT", "API key not valid");
          }

          @Override
          public GeminiRecords.GeminiCountResponse countTokens(
              String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
            return null;
          }

          @Override
          public GeminiRecords.GeminiResponse generateContent(
              String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
            return null;
          }
        };

    GeminiService service = new GeminiService(client, () -> "test-key");

    assertThrows(GeminiApiException.class, service::getAvailableModels);
  }
}
