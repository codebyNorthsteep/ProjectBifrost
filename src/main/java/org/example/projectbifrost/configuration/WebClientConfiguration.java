package org.example.projectbifrost.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {
    @Value("${openrouter.api.key}")
    private String apiKey;

    @Bean
    public WebClient openAIWebClient() {
        return WebClient.builder()
                .baseUrl("https://openrouter.ai/api/v1") // URL for OpenRouter
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
}
