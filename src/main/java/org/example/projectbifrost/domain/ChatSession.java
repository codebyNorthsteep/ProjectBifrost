package org.example.projectbifrost.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a chat session that contains a unique session identifier
 * and a chronological history of chat messages.
 *
 * The chat session is a core entity for managing and maintaining
 * conversational histories within a chat application. Each session
 * is uniquely identified by its session ID and maintains a list of
 * chat messages, which are instances of the ChatMessage class.
 */
@Getter
@Setter
@AllArgsConstructor
public class ChatSession {
    private String sessionId;
    private List<ChatMessage> chatHistory;

    public void addMessage(ChatMessage message) {
        chatHistory.add(message);
    }

    public void clearChatHistory() {
        chatHistory.clear();  // keeps sessionId
    }
}
