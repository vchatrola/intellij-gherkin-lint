package com.vchatrola.gemini.api;

import com.vchatrola.gemini.dto.GeminiRecords;

/**
 * @deprecated Legacy Spring HTTP interface retained for reference only.
 *
 * See docs/legacy/SPRING_GEMINI_INTERFACE.md for the original implementation.
 */
@Deprecated
public interface GeminiInterface {

    GeminiRecords.ModelList getModels(String apiKey);

    GeminiRecords.GeminiCountResponse countTokens(String model, String apiKey, GeminiRecords.GeminiRequest request);

    GeminiRecords.GeminiResponse getCompletion(String model, String apiKey, GeminiRecords.GeminiRequest request);
}
