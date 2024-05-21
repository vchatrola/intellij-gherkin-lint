package com.vchatrola.gemini.config;

import com.vchatrola.gemini.api.GeminiInterface;
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
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean
    public GeminiInterface geminiInterface(@Qualifier("geminiRestClient") RestClient client) {
        RestClientAdapter adapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(GeminiInterface.class);
    }
}
