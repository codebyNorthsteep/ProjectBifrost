package org.example.projectbifrost.service;

import org.example.projectbifrost.configuration.WebClientConfiguration;
import org.example.projectbifrost.domain.ChatMessage;
import org.example.projectbifrost.storage.ChattSessionStorage;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

private final ChattSessionStorage chattSessionStorage;
private final WebClientConfiguration webClientConfiguration;

    public ChatService(ChattSessionStorage chattSessionStorage, WebClientConfiguration webClientConfiguration) {
        this.chattSessionStorage = chattSessionStorage;
        this.webClientConfiguration = webClientConfiguration;
    }

}
