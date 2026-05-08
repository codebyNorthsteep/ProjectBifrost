package org.example.projectbifrost.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * Represents a chat message within a chat session.
 *
 * This class encapsulates the details of a message exchanged
 * during a chat session, including its role (e.g., "user", "assistant"),
 * the message content, the sender's name, and the timestamp
 * of when the message was created. Instances of this class
 * are typically used and managed in the context of a {@link ChatSession}.
 *
 * The timestamp is automatically generated at the time of message creation.
 */
@Getter
@Setter
public class ChatMessage {
    private String role;
    private String content;
    private String senderName; //For chat
    private LocalDateTime timeStamp;

     public ChatMessage(String role, String message, String senderName) {
        this.role = role;
        this.content = message;
        this.senderName = senderName;
        this.timeStamp = LocalDateTime.now();//Adds the current time to each message
    }
}
