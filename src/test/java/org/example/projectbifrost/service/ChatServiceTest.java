package org.example.projectbifrost.service;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.dto.OpenRouterRequestDTO;
import org.example.projectbifrost.dto.Personality;
import org.example.projectbifrost.exception.InvalidLLMResponseException;
import org.example.projectbifrost.exception.RetryableHttpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Test complete successful chat flow with session management and message history")
    void testSuccessfulChatWithLLM() {
        String sessionId = "test-sessionId-1";
        String userMessage = "What is your advice?";
        String llmResponse = "Heed my wisdom, mortal!";

        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"choices\": [{\"message\": {\"content\": \"" + llmResponse + "\"}}]}")));

        String result = chatService.chatWithLLM(
                new ChatRequestDTO(Personality.ODIN, userMessage, sessionId)
        );

        // Verify response and history
        assertThat(result).isEqualTo(llmResponse);

        ChatSession session = chatService.getSessionHistory(sessionId);
        assertThat(session.getChatHistory()).hasSize(2);
        assertThat(session.getChatHistory().get(0).getRole()).isEqualTo("user");
        assertThat(session.getChatHistory().get(0).getContent()).isEqualTo(userMessage);
        assertThat(session.getChatHistory().get(1).getRole()).isEqualTo("assistant");
        assertThat(session.getChatHistory().get(1).getContent()).isEqualTo(llmResponse);
    }

    @Test
    @DisplayName("Should throw InvalidLLMResponseException if LLM has empty answer")
    void testEmptyAnswerFromLLM() {
        //Tell WireMock to send an answer where "choices" is empty []
        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"choices\": []}")));

        ChatRequestDTO requestDTO = new ChatRequestDTO(Personality.ODIN, "Hello", "session-empty-llm");

        assertThatThrownBy(() ->
                chatService.chatWithLLM(requestDTO)
        ).isInstanceOf(InvalidLLMResponseException.class);
    }

    @Test
    @DisplayName("Should throw RetryableHttpException if server returns status 500")
    void testServerErrorThrowsException() {
        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(500)));

        ChatRequestDTO requestDTO = new ChatRequestDTO(Personality.ODIN, "Hello", "session-server-error");

        assertThatThrownBy(() ->
                chatService.chatWithLLM(requestDTO)
        ).isInstanceOf(RetryableHttpException.class);
    }

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

    @Test
    @DisplayName("Test Circuit Breaker opens after consecutive failures and throws exception")
    void testCircuitBreakerLogic() throws InterruptedException {

        var messages = List.of(new OpenRouterRequestDTO.Message("user", "Hello"));

        stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Fail!")));

        for (int i = 0; i < 10; i++) {
            try {
                chatService.fetchResponseFromLLM(messages);
            } catch (Exception e) {
                //Ignore exceptions in loop, just let CircuitBreaker register them
            }
        }

        // RESET and verify circuit breaker is open, so fallback throws exception
        resetAllRequests();


        assertThatThrownBy(() ->
            chatService.fetchResponseFromLLM(messages)
        )
        .isInstanceOf(RetryableHttpException.class)
        .hasMessageContaining("The Gods are not responding");

        // Verify that WireMock didn't receive the call (because CB is open)
        verify(0, postRequestedFor(urlEqualTo("/chat/completions")));

        Thread.sleep(6000);  //Timeout open state so we are in half-open

        // Now stub WireMock again to return success for the HALF_OPEN test
        stubFor(post("/chat/completions")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"choices\": [{\"message\": {\"content\": \"Success after recovery!\"}}]}")));

        String result = chatService.fetchResponseFromLLM(List.of(new OpenRouterRequestDTO.Message("user", "Hello")));
        assertThat(result)
                .as("Circuit breaker should have recovered and call should succeed")
                .isEqualTo("Success after recovery!");
    }

    @Test
    @DisplayName("Should clear chat history for a session")
    void testClearChatHistory() {
        String sessionId = "history-clear-session";
        String llmResponse = "Clear me!";

        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"choices\": [{\"message\": {\"content\": \"" + llmResponse + "\"}}]}")));

        // Add messages to session
        chatService.chatWithLLM(
                new ChatRequestDTO(Personality.ODIN, "First message", sessionId)
        );

        ChatSession session = chatService.getSessionHistory(sessionId);
        assertThat(session.getChatHistory()).hasSize(2);

        // Clear the history
        chatService.clearChatHistory(sessionId);

        // Verify history is empty but session still exists
        assertThat(session.getChatHistory()).isEmpty();
        assertThat(session.getSessionId()).isEqualTo(sessionId);
    }
}
