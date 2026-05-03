package org.example.projectbifrost.service;

import org.example.projectbifrost.configuration.RestClientConfiguration;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.storage.ChattSessionStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ChatService {

    private final ChattSessionStorage chattSessionStorage;
    private final RestClient restClient;

    public ChatService(ChattSessionStorage chattSessionStorage, RestClient restClient) {
        this.chattSessionStorage = chattSessionStorage;
        this.restClient = restClient;
    }

    public void sendRequestToLLM(ChatRequestDTO dto) {
        var postRequest = restClient.post()
                .uri("/chat/completions")
                .body(dto)
                .retrieve()
                .toEntity(String.class)
                .block();
    }
}
