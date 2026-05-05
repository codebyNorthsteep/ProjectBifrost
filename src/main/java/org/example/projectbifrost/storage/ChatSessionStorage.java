package org.example.projectbifrost.storage;

import org.example.projectbifrost.domain.ChatSession;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.synchronizedList;

/**
 * Manages storage and retrieval of chat sessions.
 * In-memory database for all sessions.
 * Each chat session contains a unique session identifier and a history of chat messages.
 * The class ensures that a session is either retrieved if it exists, or created if it's not
 * yet present in the storage, enabling seamless session management for chat applications.
 */
@Component
public class ChatSessionStorage {
    private final Map<String, ChatSession> sessionStorage = new ConcurrentHashMap<>(); //Thread-safe

    public ChatSession getOrCreateChatSession(String sessionId) {
        return sessionStorage.computeIfAbsent(
                sessionId,
                id -> new ChatSession(id, synchronizedList(new ArrayList<>()))
        );
    }

    public void deleteSession(String sessionId) {
        sessionStorage.remove(sessionId);
    //Copilot påpekar att det kan vara bra med en MAX-size på listan, och sköta delete om den blir för stor
    }
}
