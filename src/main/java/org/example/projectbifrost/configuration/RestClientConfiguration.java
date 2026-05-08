package org.example.projectbifrost.configuration;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.client.RestClient;

import java.net.URI;

/**
 * Configuration class for setting up the RestClient bean used to interact with the OpenRouter service.
 * Integrates resilient methods to enable fault tolerance when communicating with the API.
 *
 * This configuration retrieves API properties such as the base URL and API key from the application's
 * externalized configuration and injects them as properties. The RestClient is configured with an HTTP client
 * that disables automatic retries and includes default headers required for authentication and proper content type.
 *
 * An instance of the RestClient.Builder is autowired and used to construct the RestClient based on the provided
 * configuration properties.
 */
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
