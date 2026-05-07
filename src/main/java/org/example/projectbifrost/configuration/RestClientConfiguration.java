package org.example.projectbifrost.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Configuration
public class RestClientConfiguration {
    @Value("${openrouter.api.key}")
    private String apiKey;

    @Bean
    public RestClient openRouterRestClient() {
        return RestClient.builder()
                .baseUrl(URI.create("https://openrouter.ai/api/v1"))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

    }
}
