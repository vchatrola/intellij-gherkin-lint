package com.vchatrola.gemini.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vchatrola.gemini.dto.GeminiRecords;
import com.vchatrola.util.GherkinLintLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiHttpClient implements GeminiClient {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MILLIS = 500L;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiHttpClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build(), new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    GeminiHttpClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GeminiRecords.ModelList getModels(String apiKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("x-goog-api-key", apiKey)
                .header("Accept", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        return sendRequest(request, GeminiRecords.ModelList.class);
    }

    @Override
    public GeminiRecords.GeminiCountResponse countTokens(String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
        String url = BASE_URL + model + ":countTokens";
        return sendPost(url, apiKey, requestBody, GeminiRecords.GeminiCountResponse.class);
    }

    @Override
    public GeminiRecords.GeminiResponse generateContent(String model, String apiKey, GeminiRecords.GeminiRequest requestBody) {
        String url = BASE_URL + model + ":generateContent";
        return sendPost(url, apiKey, requestBody, GeminiRecords.GeminiResponse.class);
    }

    private <T> T sendPost(String url, String apiKey, Object body, Class<T> type) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-goog-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(REQUEST_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Gemini request.", e);
        }
    }

    private <T> T sendRequest(HttpRequest request, Class<T> type) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    if (isRetryableStatus(response.statusCode()) && attempt < MAX_ATTEMPTS) {
                        lastError = new RuntimeException(response.statusCode() + " " + response.body());
                        backoff(attempt);
                        continue;
                    }
                    throw new RuntimeException(response.statusCode() + " " + response.body());
                }
                return objectMapper.readValue(response.body(), type);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                GherkinLintLogger.error("Gemini HTTP request interrupted.", e);
                throw new RuntimeException("Gemini HTTP request interrupted.", e);
            } catch (IOException e) {
                if (attempt < MAX_ATTEMPTS) {
                    lastError = new RuntimeException("Gemini HTTP request failed.", e);
                    backoff(attempt);
                    continue;
                }
                GherkinLintLogger.error("Gemini HTTP request failed.", e);
                throw new RuntimeException("Gemini HTTP request failed.", e);
            }
        }
        throw lastError != null ? lastError : new RuntimeException("Gemini HTTP request failed.");
    }

    private static boolean isRetryableStatus(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode == 500 || statusCode == 502
                || statusCode == 503 || statusCode == 504;
    }

    private static void backoff(int attempt) {
        long delay = (long) (BASE_BACKOFF_MILLIS * Math.pow(2, attempt - 1));
        long jitter = (long) (Math.random() * 200L);
        try {
            Thread.sleep(delay + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
