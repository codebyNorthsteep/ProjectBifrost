package org.example.projectbifrost.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.example.projectbifrost.domain.ChatMessage;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.dto.OpenRouterRequestDTO;
import org.example.projectbifrost.dto.OpenRouterResponseDTO;
import org.example.projectbifrost.exception.InvalidLLMResponseException;
import org.example.projectbifrost.exception.RetryableHttpException;
import org.example.projectbifrost.storage.ChatSessionStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    private final ChatSessionStorage chatSessionStorage;
    private final RestClient restClient;

    @Value("${openrouter.model}")
    private String model;

    public ChatService(ChatSessionStorage chatSessionStorage, RestClient restClient) {
        this.chatSessionStorage = chatSessionStorage;
        this.restClient = restClient;
    }


    /**
     * Sends a chat request to a large language model (LLM) using the supplied chat request data.
     * The method manages the chat session, compiles the request for the LLM, and processes the response.
     * It also maintains the history of chat messages within the session.
     *
     * @param dto the {@link ChatRequestDTO} containing the user message, chat session ID, and personality configuration.
     * @return the response from the LLM as a string.
     * @throws InvalidLLMResponseException if an error occurs during communication with the LLM,
     *                                     or if the LLM response is empty or malformed.
     */
    public String chatWithLLM(ChatRequestDTO dto) {
        ChatSession chatSession = chatSessionStorage.getOrCreateChatSession(dto.sessionId());

        List<OpenRouterRequestDTO.Message> apiMessages = new ArrayList<>();
        apiMessages.add(new OpenRouterRequestDTO.Message("system", dto.personality().getSystemPrompt()));

        chatSession.getChatHistory().forEach(m ->
                apiMessages.add(new OpenRouterRequestDTO.Message(m.getRole(), m.getContent()))
        );

        apiMessages.add(new OpenRouterRequestDTO.Message("user", dto.message()));

        try {
            String content = fetchResponseFromLLM(apiMessages);
            // Only store in history if we got a real LLM response (not a circuit breaker fallback)
            chatSession.addMessage(new ChatMessage("user", dto.message()));
            chatSession.addMessage(new ChatMessage("assistant", content));
            return content;
        } catch (RetryableHttpException e) {
            // Circuit breaker is open or service unavailable - don't pollute history
            throw e;
        }

    }

    @CircuitBreaker(name = "chatService", fallbackMethod = "fallback")
    //Break stream of tries if too many failures, and call fallback method
    @Retry(name = "chatService") //Try again if fails, up to max-attempts
    public String fetchResponseFromLLM(List<OpenRouterRequestDTO.Message> apiMessages) {
        var openRouterRequest = new OpenRouterRequestDTO(model, apiMessages);

        OpenRouterResponseDTO result = restClient.post()
                .uri("/chat/completions")//Start of URI configured in RestClientConfiguration.java
                .body(openRouterRequest) //Send JSON body of messages and model
                .retrieve()
                .onStatus(s -> s.value() == 429 || s.value() == 500,
                        (req, resp) -> {
                            throw new RetryableHttpException(
                                    "Upstream LLM returned " + resp.getStatusCode().value()
                            );
                        })
                .body(OpenRouterResponseDTO.class);

        if (result == null || result.choices() == null || result.choices().isEmpty()) {
            throw new InvalidLLMResponseException(
                    "The Gods sent an empty omen (Invalid response from LLM)",
                    model,
                    HttpStatus.BAD_GATEWAY.value()
            );
        }
        return result.choices().getFirst().message().content();

    }

    public String fallback(Exception e) {
        // Circuit breaker is open - throw exception so it doesn't pollute chat history
        log.warn("Circuit breaker fallback triggered for LLM call. Cause: {}", e.getMessage(), e);
        throw new RetryableHttpException("The Gods are not responding - service temporarily unavailable"+ e.getMessage());
    }

    public ChatSession getSessionHistory(String sessionId) {
        return chatSessionStorage.getOrCreateChatSession(sessionId);
    }

}
