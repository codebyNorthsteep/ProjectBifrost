package org.example.projectbifrost.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a chat message in a conversation.
 * This class encapsulates the details of a single message, including its role, content,
 * and the timestamp at which it was created.
 *
 * The role typically signifies the sender's identity (e.g., user, system, assistant),
 * while the message contains the text content of the chat.
 * The timestamp indicates when the message was generated.
 */
@Getter
@Setter
public class ChatMessage {
    private String role;
    private String content;
    private LocalDateTime timeStamp;

     public ChatMessage(String role, String message) {
        this.role = role;
        this.content = message;
        this.timeStamp = LocalDateTime.now();//Adds the current time to each message
    }
}
