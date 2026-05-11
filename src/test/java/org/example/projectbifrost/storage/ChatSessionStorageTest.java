package org.example.projectbifrost.storage;

import org.example.projectbifrost.domain.ChatMessage;
import org.example.projectbifrost.domain.ChatSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatSessionStorageTest {
    private ChatSessionStorage storage;

    @BeforeEach
    void setUp() {
        storage = new ChatSessionStorage();
    }

    @Test
    @DisplayName("Should create a new session if id does not exist")
    void shouldCreateNewSessionWhenMissing() {
        String sessionId = "new-id";

        ChatSession session = storage.getOrCreateChatSession(sessionId);

        assertThat(session).isNotNull();
        assertThat(session.getSessionId()).isEqualTo(sessionId);

        assertThat(session.getChatHistory()).isNotNull();
    }

    @Test
    @DisplayName("Should return existing session if id exists")
    void shouldReturnExistingSession() {
        String sessionId = "same-id";

        ChatSession first = storage.getOrCreateChatSession(sessionId);
        ChatSession second = storage.getOrCreateChatSession(sessionId);

        assertThat(second).isSameAs(first);
    }


    @Test
    @DisplayName("Should delete session")
    void shouldDeleteSession() {
        String sessionId = "goodbye-session";

        storage.getOrCreateChatSession(sessionId);

        storage.deleteSession(sessionId);

        // After delete, when calling getOrCreate it should create a new session, so we can check that it's different from the old one
        ChatSession oldSession = storage.getOrCreateChatSession(sessionId); // this session was removed
        storage.deleteSession(sessionId);
        ChatSession newSession = storage.getOrCreateChatSession(sessionId);

        assertThat(newSession).isNotSameAs(oldSession);
    }

    @Test
    @DisplayName("Should clear chat history while keeping the session")
    void shouldClearSessionHistory() {
        String sessionId = "history-session";
        ChatSession session = storage.getOrCreateChatSession(sessionId);

        // Add some messages to the history
        session.addMessage(new ChatMessage("user", "Hello", "you"));
        session.addMessage(new ChatMessage("assistant", "Hi there!", "LOKI"));

        assertThat(session.getChatHistory()).hasSize(2);

        // Clear the history
        storage.clearSessionHistory(sessionId);

        // History should be empty but session should still exist
        assertThat(session.getChatHistory()).isEmpty();
        assertThat(session.getSessionId()).isEqualTo(sessionId);
    }
}
