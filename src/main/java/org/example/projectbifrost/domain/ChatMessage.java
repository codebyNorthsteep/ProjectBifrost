package org.example.projectbifrost.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessage {
    private String role;
    private String message;
    private LocalDateTime timeStamp;

     public ChatMessage(String role, String message) {
        this.role = role;
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }
}
