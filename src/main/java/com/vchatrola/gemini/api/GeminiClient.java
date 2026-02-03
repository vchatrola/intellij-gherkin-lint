package com.vchatrola.gemini.api;

import com.vchatrola.gemini.dto.GeminiRecords;

public interface GeminiClient {
  GeminiRecords.ModelList getModels(String apiKey);

  GeminiRecords.GeminiCountResponse countTokens(
      String model, String apiKey, GeminiRecords.GeminiRequest request);

  GeminiRecords.GeminiResponse generateContent(
      String model, String apiKey, GeminiRecords.GeminiRequest request);
}
