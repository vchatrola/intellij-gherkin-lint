package com.vchatrola.gemini.config;

import com.vchatrola.gemini.api.GeminiInterface;
import com.vchatrola.util.GherkinLintLogger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@ComponentScan(basePackages = "com.vchatrola.gemini")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public RestClient geminiRestClient(@Value("${gemini.baseurl}") String baseUrl,
                                       @Value("${google.api.key}") String apiKey) {
        try {
            if (apiKey == null) {
                GherkinLintLogger.warn("google.api.key property not found. Ensure it's properly configured in the " +
                        "environment variables.");
            }

            GherkinLintLogger.info("Creating RestClient with baseUrl: " + baseUrl);

            RestClient restClient = RestClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("x-goog-api-key", apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .build();

            GherkinLintLogger.info("RestClient created successfully");

            return restClient;
        } catch (Exception e) {
            String errorMessage = "Error occurred while creating RestClient: " + e.getMessage();
            GherkinLintLogger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Bean
    public GeminiInterface geminiInterface(@Qualifier("geminiRestClient") RestClient client) {
        try {
            RestClientAdapter adapter = RestClientAdapter.create(client);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(GeminiInterface.class);
        } catch (Exception e) {
            String errorMessage = "Error occurred while creating GeminiInterface: " + e.getMessage();
            GherkinLintLogger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}
