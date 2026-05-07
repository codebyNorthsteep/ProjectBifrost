package org.example.projectbifrost.service;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.example.projectbifrost.dto.OpenRouterRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "openrouter.api.key=test-key",
        "openrouter.model=test-model",
        "openrouter.base-url=${wiremock.server.baseUrl}"
})
@EnableWireMock
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Test
    @DisplayName("Test retry mechanism when LLM service is temporarily unavailable")
    void testRetry() {
        stubFor(post(urlEqualTo("/chat/completions"))//OpenRouters url
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Failure!"))
                .willSetStateTo("Failure number 1"));

        stubFor(post(urlEqualTo("/chat/completions"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Failure number 1")
                .willReturn(aResponse().withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Failure again!"))
                .willSetStateTo("Failure number 2"));

        stubFor(post(urlEqualTo("/chat/completions"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Failure number 2")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json") //Have to tell RestClient that this is JSON, otherwise it won't parse the body and the test will fail since the response will be empty
                        .withBody("{\"choices\": [{\"message\": {\"content\": \"Success!\"}}]}"))); //OpenRouters JSON format expected

        String result = chatService.fetchResponseFromLLM(List.of(new OpenRouterRequestDTO.Message("user", "Hello")));

        assertThat(result)
                .as("Should be success after two retries")
                .isEqualTo("Success!");

        verify(3, postRequestedFor(urlEqualTo("/chat/completions"))); //Verify that the endpoint was called 3 times (initial + 2 retries)

        assertThat(findAll(postRequestedFor(urlEqualTo("/chat/completions"))))
                .as("Should call the endpoint exactly 3 times due to retry")
                .hasSize(3);
    }
}
