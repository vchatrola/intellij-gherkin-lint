package com.vchatrola.gemini.service;

import com.vchatrola.gemini.api.GeminiApiException;
import com.vchatrola.gemini.api.GeminiClient;
import com.vchatrola.gemini.api.GeminiHttpClient;
import com.vchatrola.gemini.dto.GeminiRecords;
import com.vchatrola.gemini.dto.GeminiRecords.Content;
import com.vchatrola.gemini.dto.GeminiRecords.GeminiCountResponse;
import com.vchatrola.gemini.dto.GeminiRecords.GeminiRequest;
import com.vchatrola.gemini.dto.GeminiRecords.GeminiResponse;
import com.vchatrola.gemini.dto.GeminiRecords.TextPart;
import com.vchatrola.gemini.dto.GeminiRecords.UsageMetadata;
import com.vchatrola.plugin.setting.GherkinLintSecrets;
import com.vchatrola.plugin.setting.GherkinLintSettingsState;
import com.vchatrola.util.GherkinLintLogger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.VisibleForTesting;

public class GeminiService {
  private final GeminiClient geminiClient;
  private final Supplier<String> apiKeySupplier;
  private static volatile List<GeminiRecords.Model> cachedModels;

  public GeminiService() {
    this(new GeminiHttpClient(), GherkinLintSecrets::getApiKeyOrEnv);
  }

  @VisibleForTesting
  GeminiService(GeminiClient geminiClient, Supplier<String> apiKeySupplier) {
    this.geminiClient = geminiClient;
    this.apiKeySupplier = apiKeySupplier;
  }

  public GeminiRecords.ModelList getModels() {
    return geminiClient.getModels(getApiKeyOrThrow());
  }

  public List<GeminiRecords.Model> getAvailableModels() {
    if (cachedModels != null) {
      return cachedModels;
    }

    synchronized (GeminiService.class) {
      if (cachedModels != null) {
        return cachedModels;
      }
      try {
        GeminiRecords.ModelList modelList = getModels();
        if (modelList == null || modelList.models() == null) {
          cachedModels = Collections.emptyList();
        } else {
          cachedModels =
              modelList.models().stream().filter(Objects::nonNull).collect(Collectors.toList());
          persistModelCache(cachedModels);
        }
      } catch (GeminiApiException | IllegalStateException e) {
        throw e;
      } catch (Exception e) {
        GherkinLintLogger.debug("Failed to load Gemini models. Model list will be empty.");
        cachedModels = Collections.emptyList();
      }
    }

    return cachedModels;
  }

  public static void clearCachedModels() {
    cachedModels = null;
  }

  public List<String> getAvailableModelNames() {
    return getAvailableModels().stream()
        .map(GeminiRecords.Model::name)
        .filter(Objects::nonNull)
        .map(GeminiService::normalizeModelName)
        .distinct()
        .collect(Collectors.toList());
  }

  public GeminiCountResponse countTokens(String model, GeminiRequest request) {
    String resolvedModel = resolveModelOrThrow(model);
    return geminiClient.countTokens(resolvedModel, getApiKeyOrThrow(), request);
  }

  public int countTokens(String text, String model) {
    GeminiCountResponse response =
        countTokens(model, new GeminiRequest(List.of(new Content(List.of(new TextPart(text))))));
    return response.totalTokens();
  }

  public GeminiResponse getCompletion(GeminiRequest request) {
    String resolvedModel = resolveModelOrThrow(null);
    GeminiResponse response =
        geminiClient.generateContent(resolvedModel, getApiKeyOrThrow(), request);
    logUsageMetadata(response);
    return response;
  }

  public GeminiResponse getCompletionWithModel(String model, GeminiRequest request) {
    String resolvedModel = resolveModelOrThrow(model);
    GeminiResponse response =
        geminiClient.generateContent(resolvedModel, getApiKeyOrThrow(), request);
    logUsageMetadata(response);
    return response;
  }

  public String getCompletion(String text, String model) {
    GeminiResponse response =
        getCompletionWithModel(
            model, new GeminiRequest(List.of(new Content(List.of(new TextPart(text))))));
    return response.candidates().get(0).content().parts().get(0).text();
  }

  private static String normalizeModelName(String modelName) {
    if (modelName == null) {
      return "";
    }
    if (modelName.startsWith("models/")) {
      return modelName.substring("models/".length());
    }
    return modelName;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private String getApiKeyOrThrow() {
    String apiKey = apiKeySupplier.get();
    if (isBlank(apiKey)) {
      throw new IllegalStateException(
          "Gemini API key is missing. Set it in settings or GOOGLE_API_KEY.");
    }
    return apiKey;
  }

  private void logUsageMetadata(GeminiResponse response) {
    if (response == null || response.usageMetadata() == null) {
      return;
    }
    UsageMetadata usage = response.usageMetadata();
    GherkinLintLogger.debugVerbose(
        String.format(
            "Gemini usage - prompt: %d, candidates: %d, total: %d",
            usage.promptTokenCount(), usage.candidatesTokenCount(), usage.totalTokenCount()));
  }

  private String resolveModelOrThrow(String model) {
    if (!isBlank(model)) {
      GherkinLintLogger.debugVerbose("Using Gemini model: " + model);
      return model;
    }
    List<String> available = getAvailableModelNames();
    if (available.isEmpty()) {
      throw new IllegalStateException("No Gemini models available from API.");
    }
    String resolved = available.get(0);
    GherkinLintLogger.debugVerbose("Using Gemini model (auto): " + resolved);
    return resolved;
  }

  private void persistModelCache(List<GeminiRecords.Model> models) {
    if (models == null || models.isEmpty()) {
      return;
    }
    List<String> names =
        models.stream()
            .map(GeminiRecords.Model::name)
            .filter(Objects::nonNull)
            .map(GeminiService::normalizeModelName)
            .distinct()
            .collect(Collectors.toList());
    GherkinLintSettingsState state = GherkinLintSettingsState.getInstance();
    state.geminiModels = names;
    state.geminiModelsFetchedAt = System.currentTimeMillis();
  }
}
