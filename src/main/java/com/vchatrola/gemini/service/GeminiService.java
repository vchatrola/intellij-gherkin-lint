package com.vchatrola.gemini.service;

import com.vchatrola.gemini.api.GeminiInterface;
import com.vchatrola.gemini.dto.GeminiRecords;
import com.vchatrola.util.GherkinLintLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.vchatrola.gemini.dto.GeminiRecords.*;

@Service
public class GeminiService {

    private final GeminiInterface geminiInterface;
    private static volatile List<GeminiRecords.Model> cachedModels;

    @Autowired
    public GeminiService(GeminiInterface geminiInterface) {
        this.geminiInterface = geminiInterface;
    }

    public GeminiRecords.ModelList getModels() {
        return geminiInterface.getModels();
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
                    cachedModels = modelList.models().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                GherkinLintLogger.warn("Failed to load Gemini models. Model list will be empty.", e);
                cachedModels = Collections.emptyList();
            }
        }

        return cachedModels;
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
        return geminiInterface.countTokens(resolvedModel, request);
    }

    public int countTokens(String text, String model) {
        GeminiCountResponse response = countTokens(model, new GeminiRequest(
                List.of(new Content(List.of(new TextPart(text))))));
        return response.totalTokens();
    }

    public GeminiResponse getCompletion(GeminiRequest request) {
        String resolvedModel = resolveModelOrThrow(null);
        GeminiResponse response = geminiInterface.getCompletion(resolvedModel, request);
        logUsageMetadata(response);
        return response;
    }

    public GeminiResponse getCompletionWithModel(String model, GeminiRequest request) {
        String resolvedModel = resolveModelOrThrow(model);
        GeminiResponse response = geminiInterface.getCompletion(resolvedModel, request);
        logUsageMetadata(response);
        return response;
    }


    public String getCompletion(String text, String model) {
        GeminiResponse response = getCompletionWithModel(model, new GeminiRequest(
                List.of(new Content(List.of(new TextPart(text))))));
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

    private void logUsageMetadata(GeminiResponse response) {
        if (response == null || response.usageMetadata() == null) {
            return;
        }
        UsageMetadata usage = response.usageMetadata();
        GherkinLintLogger.info(String.format(
                "Gemini usage - prompt: %d, candidates: %d, total: %d",
                usage.promptTokenCount(),
                usage.candidatesTokenCount(),
                usage.totalTokenCount()
        ));
    }

    private String resolveModelOrThrow(String model) {
        if (!isBlank(model)) {
            GherkinLintLogger.info("Using Gemini model: " + model);
            return model;
        }
        List<String> available = getAvailableModelNames();
        if (available.isEmpty()) {
            throw new IllegalStateException("No Gemini models available from API.");
        }
        String resolved = available.get(0);
        GherkinLintLogger.info("Using Gemini model (auto): " + resolved);
        return resolved;
    }
}
