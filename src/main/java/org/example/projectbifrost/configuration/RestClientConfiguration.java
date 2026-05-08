package org.example.projectbifrost.configuration;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Configuration
@EnableResilientMethods
public class RestClientConfiguration {
    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.base-url}")
    private String baseUrl;

    @Bean
    public RestClient openRouterRestClient(RestClient.Builder builder) {
        var httpClient = HttpClients.custom()
                .disableAutomaticRetries()
                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return builder
                .requestFactory(requestFactory)
                .baseUrl(URI.create(baseUrl))
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

    }
}
