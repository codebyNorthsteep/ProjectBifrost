package org.example.projectbifrost.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class BifrostController {


    private final ChatService chatService;

    public BifrostController(ChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping("/v1/chat")
    public ResponseEntity<String> sendChatRequest(@Valid @RequestBody ChatRequestDTO dto) {
        log.info("Received chat request: Personality={}, SessionId={}", dto.personality(), maskSessionId(dto.sessionId()));
        String response = chatService.chatWithLLM(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/chat/{sessionId}")
    public ResponseEntity<ChatSession> getChatHistory(@PathVariable String sessionId) {
        ChatSession session = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/v1/chat/{sessionId}")
    public ResponseEntity<Void> clearChatHistory(@PathVariable String sessionId) {
        log.info("Clearing chat history for session: {}", maskSessionId(sessionId));
        chatService.clearChatHistory(sessionId);
        return ResponseEntity.noContent().build();
    }

    //Mask when logging seesionId
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 6) return "***";
        return "***" + sessionId.substring(sessionId.length() - 4);
    }
}
