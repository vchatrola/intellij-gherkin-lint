package com.vchatrola.gemini.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vchatrola.gemini.api.GeminiApiException;
import com.vchatrola.gemini.api.GeminiClient;
import com.vchatrola.gemini.dto.GeminiRecords;
import java.util.List;
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

  @Test
  void getCompletion_returnsTextWhenResponseIsValid() {
    GeminiRecords.GeminiResponse.Candidate.Content content =
        new GeminiRecords.GeminiResponse.Candidate.Content(
            List.of(new GeminiRecords.TextPart("ok")), "model");
    GeminiRecords.GeminiResponse response =
        new GeminiRecords.GeminiResponse(
            List.of(new GeminiRecords.GeminiResponse.Candidate(content, "STOP", 0, List.of())),
            null,
            null);

    GeminiService service = new GeminiService(clientReturning(response), () -> "test-key");

    assertEquals("ok", service.getCompletion("prompt", "models/gemini-test"));
  }

  @Test
  void getCompletion_throwsWhenCandidatesMissing() {
    GeminiRecords.GeminiResponse response = new GeminiRecords.GeminiResponse(List.of(), null, null);
    GeminiService service = new GeminiService(clientReturning(response), () -> "test-key");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getCompletion("prompt", "models/gemini-test"));
    assertEquals("Gemini response did not include any candidates.", ex.getMessage());
  }

  @Test
  void getCompletion_throwsWhenCandidateTextEmpty() {
    GeminiRecords.GeminiResponse.Candidate.Content content =
        new GeminiRecords.GeminiResponse.Candidate.Content(
            List.of(new GeminiRecords.TextPart("   ")), "model");
    GeminiRecords.GeminiResponse response =
        new GeminiRecords.GeminiResponse(
            List.of(new GeminiRecords.GeminiResponse.Candidate(content, "STOP", 0, List.of())),
            null,
            null);
    GeminiService service = new GeminiService(clientReturning(response), () -> "test-key");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getCompletion("prompt", "models/gemini-test"));
    assertEquals("Gemini response candidate text is empty.", ex.getMessage());
  }

  private static GeminiClient clientReturning(GeminiRecords.GeminiResponse response) {
    return new GeminiClient() {
      @Override
      public GeminiRecords.ModelList getModels(String apiKey) {
        return new GeminiRecords.ModelList(List.of());
      }

      @Override
      public GeminiRecords.GeminiCountResponse countTokens(
          String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
        return null;
      }

      @Override
      public GeminiRecords.GeminiResponse generateContent(
          String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
        return response;
      }
    };
  }
}
