package org.example.projectbifrost.service;

import org.example.projectbifrost.domain.ChatMessage;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.dto.OpenRouterRequestDTO;
import org.example.projectbifrost.dto.OpenRouterResponseDTO;
import org.example.projectbifrost.storage.ChatSessionStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

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
     * Sends a request to the large language model (LLM) with the user's message and personality context
     * and retrieves the response.
     * <p>
     * This method constructs a chat session using the provided session ID or creates a new one if it
     * does not exist. It adds the user's message to the chat session and builds a context-aware request
     * to be sent to the LLM. The LLM's response is then returned as a string.
     */
    public String sendRequestToLLM(ChatRequestDTO dto) {
        ChatSession chatSession = chatSessionStorage.getOrCreateChatSession(dto.sessionId());
        chatSession.addMessage(new ChatMessage("user", dto.message()));

List<OpenRouterRequestDTO.Message> apiMessages = new ArrayList<>();
        apiMessages.add(new OpenRouterRequestDTO.Message("system", dto.personality().getSystemPrompt()));

        chatSession.getChatHistory().forEach(m ->
                apiMessages.add(new OpenRouterRequestDTO.Message(m.getRole(), m.getContent()))
        );

        var openRouterRequest = new OpenRouterRequestDTO(model, apiMessages);

        OpenRouterResponseDTO result = restClient.post()
                .uri("/chat/completions")//Start of URI configured in RestClientConfiguration.java
                .body(openRouterRequest) //Send JSON-body of messages and model
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (request, response) -> {
                            throw new RuntimeException("API error: " + response.getStatusCode());
                        })
                .body(OpenRouterResponseDTO.class);

                String content = result.choices().getFirst().message().content();
                if (result == null || result.choices() == null || result.choices().isEmpty()) {
                        throw new RuntimeException("Received empty or invalid response from LLM");
                    }
        chatSession.addMessage(new ChatMessage("assistant", content));
        return content;
    }


        public ChatSession getSessionHistory(String sessionId) {
            return chatSessionStorage.getOrCreateChatSession(sessionId);
        }

}
