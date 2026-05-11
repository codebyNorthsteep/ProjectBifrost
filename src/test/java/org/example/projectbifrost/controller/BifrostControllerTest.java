package org.example.projectbifrost.controller;

import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BifrostController.class)
class BifrostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("Should return 200 OK for valid chat request")
    void shouldReturn200ForValidRequest() throws Exception {
        when(chatService.chatWithLLM(any(ChatRequestDTO.class))).thenReturn("Hello mortal");

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "personality": "ODIN",
                        "message": "Hello Odin",
                        "sessionId": "session123"
                    }
                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when message is missing")
    void shouldReturn400WhenMessageIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "personality": "ODIN",
                        "sessionId": "session123"
                    }
                """))
                .andExpect(status().isBadRequest()); // @Valid stops
    }

    @Test
    @DisplayName("Should return 200 OK when fetching chat history for existing session")
    void shouldReturn200ForGetHistory() throws Exception {
        mockMvc.perform(get("/api/v1/chat/session123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 204 No Content when clearing chat history")
    void shouldReturn204ForClearHistory() throws Exception {
        String sessionId = "session123";

        mockMvc.perform(delete("/api/v1/chat/" + sessionId))
                .andExpect(status().isNoContent());

        verify(chatService).clearChatHistory(sessionId);
    }
}
