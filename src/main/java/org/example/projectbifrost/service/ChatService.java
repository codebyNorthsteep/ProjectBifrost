package org.example.projectbifrost.service;

import org.example.projectbifrost.domain.ChatMessage;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.domain.PersonalityPromptProvider;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.dto.OpenRouterResponseDTO;
import org.example.projectbifrost.storage.ChattSessionStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ChatService {

    private final ChattSessionStorage chattSessionStorage;
    private final PersonalityPromptProvider personalityPromptProvider;
    private final RestClient restClient;

    public ChatService(ChattSessionStorage chattSessionStorage, PersonalityPromptProvider personalityPromptProvider, RestClient restClient) {
        this.chattSessionStorage = chattSessionStorage;
        this.personalityPromptProvider = personalityPromptProvider;
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
        String systemPrompt = personalityPromptProvider.getSystemPrompt(dto.personality());
        ChatSession chatSession = chattSessionStorage.getOrCreateChatSession(dto.sessionId());
        chatSession.addMessage(new ChatMessage("user", dto.message()));

        //Create JSON-structure for OpenRouter
        var messages = new ArrayList<>();
        messages.add(Map.of("role", "system",
                "content", systemPrompt)
        );
        messages.addAll(chatSession.getChatHistory().stream().map(m -> Map.of("role", m.getRole(),
                "content", m.getContent())
        ).toList());

        var openRouterRequest = Map.of(
                "messages", messages,
                "model", "poolside/laguna-xs.2:free" //Add which model should be used
        );

        OpenRouterResponseDTO response = restClient.post()
                .uri("/chat/completions")//Start of URI configured in RestClientConfiguration.java
                .body(openRouterRequest) //Send JSON-body of messages and model
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        (request, apiResponse) -> {
                            throw new RuntimeException("API error: " + apiResponse.getStatusCode());
                        })
                .body(OpenRouterResponseDTO.class);

        String content = response.choices().getFirst().message().content();
        chatSession.addMessage(new ChatMessage("assistant", content));
        return content;
    }


        public ChatSession getSessionHistory(String sessionId) {
            return chattSessionStorage.getOrCreateChatSession(sessionId);
        }

}
