package org.example.projectbifrost;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.service.ChatService;
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
    public String sendChatRequest(@Valid @RequestBody ChatRequestDTO dto) {
        log.info("Received chat request: Personality={}, SessionId={}", dto.personality(), maskSessionId(dto.sessionId()));
        return chatService.chatWithLLM(dto);
    }

    @GetMapping("/v1/chat/{sessionId}")
    public ChatSession getChatHistory(@PathVariable String sessionId) {
        return chatService.getSessionHistory(sessionId);
    }

    //Mask when logging seesionId
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 6) return "***";
        return "***" + sessionId.substring(sessionId.length() - 4);
    }
}
