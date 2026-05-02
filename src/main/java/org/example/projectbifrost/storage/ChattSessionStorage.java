package org.example.projectbifrost.storage;

import org.example.projectbifrost.domain.ChatSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages storage and retrieval of chat sessions.
 *
 * This class provides mechanisms to persist and manage `ChatSession` instances
 * associated with unique session IDs. It uses an internal thread-safe map to store
 * sessions, allowing for concurrent access in multi-threaded environments.
 *
 * Each chat session contains a unique session identifier and a history of chat messages.
 * The class ensures that a session is either retrieved if it exists, or created if it's not
 * yet present in the storage, enabling seamless session management for chat applications.
 */
@Component
public class ChattSessionStorage {
    private final Map<String, ChatSession> sessionStorage = new ConcurrentHashMap<>(); //Thread-safe

    public ChatSession getOrCreateChatSession(String sessionId) {
        if(sessionStorage.containsKey(sessionId)) {
            return sessionStorage.get(sessionId);
        } else {
            ChatSession newSession = new ChatSession(sessionId, new java.util.ArrayList<>());
            sessionStorage.put(sessionId, newSession);
            return newSession;
        }
    }

    public void deleteSession(String sessionId) {
        sessionStorage.remove(sessionId);

    }
}
