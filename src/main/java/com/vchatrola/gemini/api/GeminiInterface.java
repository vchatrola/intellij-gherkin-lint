package com.vchatrola.gemini.api;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import static com.vchatrola.gemini.dto.GeminiRecords.*;

@Component
@HttpExchange("/v1beta/models/")
public interface GeminiInterface {

    @GetExchange
    ModelList getModels(@RequestHeader("x-goog-api-key") String apiKey);

    @PostExchange("{model}:countTokens")
    GeminiCountResponse countTokens(
            @PathVariable String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiRequest request);

    @PostExchange("{model}:generateContent")
    GeminiResponse getCompletion(
            @PathVariable String model,
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiRequest request);
}
