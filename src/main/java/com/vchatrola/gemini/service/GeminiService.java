package com.vchatrola.gemini.service;

import com.vchatrola.gemini.api.GeminiInterface;
import com.vchatrola.gemini.dto.GeminiRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static com.vchatrola.gemini.dto.GeminiRecords.*;

@Service
public class GeminiService {

    public static final String GEMINI_MODEL = "gemini-2.5-pro";
//    public static final String GEMINI_MODEL = "gemini-2.5-flash";
//    public static final String GEMINI_MODEL = "gemini-2.5-flash-lite";

    private final GeminiInterface geminiInterface;

    @Autowired
    public GeminiService(GeminiInterface geminiInterface) {
        this.geminiInterface = geminiInterface;
    }

    public GeminiRecords.ModelList getModels() {
        return geminiInterface.getModels();
    }

    public GeminiCountResponse countTokens(String model, GeminiRequest request) {
        return geminiInterface.countTokens(model, request);
    }

    public int countTokens(String text) {
        GeminiCountResponse response = countTokens(GEMINI_MODEL, new GeminiRequest(
                List.of(new Content(List.of(new TextPart(text))))));
        return response.totalTokens();
    }

    public GeminiResponse getCompletion(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_MODEL, request);
    }

    public GeminiResponse getCompletionWithModel(String model, GeminiRequest request) {
        return geminiInterface.getCompletion(model, request);
    }


    public GeminiResponse getCompletionWithImage(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_MODEL, request);
    }

    public GeminiResponse analyzeImage(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_MODEL, request);
    }

    public String getCompletion(String text) {
        GeminiResponse response = getCompletion(new GeminiRequest(
                List.of(new Content(List.of(new TextPart(text))))));
        return response.candidates().get(0).content().parts().get(0).text();
    }

    public String getCompletionWithImage(String text, String imageFileName) throws IOException {
        GeminiResponse response = getCompletionWithImage(
                new GeminiRequest(List.of(new Content(List.of(
                        new TextPart(text),
                        new InlineDataPart(new InlineData("image/png",
                                Base64.getEncoder().encodeToString(Files.readAllBytes(
                                        Path.of("src/main/resources/", imageFileName))))))
                ))));
        System.out.println(response);
        return response.candidates().get(0).content().parts().get(0).text();
    }

    public String analyzeImage(String text, String imageFileName) throws IOException {
        GeminiResponse response = analyzeImage(
                new GeminiRequest(List.of(new Content(List.of(
                        new TextPart(text),
                        new InlineDataPart(new InlineData("image/png",
                                Base64.getEncoder().encodeToString(Files.readAllBytes(
                                        Path.of("src/main/resources/", imageFileName))))))
                ))));
        System.out.println(response);
        return response.candidates().get(0).content().parts().get(0).text();
    }
}
