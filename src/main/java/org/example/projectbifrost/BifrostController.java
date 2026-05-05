package org.example.projectbifrost;

import org.example.projectbifrost.domain.ChatSession;
import org.example.projectbifrost.dto.ChatRequestDTO;
import org.example.projectbifrost.service.ChatService;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BifrostController {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BifrostController.class);

    private final ChatService chatService;

    public BifrostController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/bifrost")
    public String bifrost() {
        return "Welcome to Bifrost, the gateway to the realms!"; }

    @PostMapping("/v1/chat")
    public String sendChatRequest(@RequestBody ChatRequestDTO dto) {
        logger.info("Received chat request: Personality={}, Message={}, SessionId={}", dto.personality(), dto.message(), dto.sessionId());
       return chatService.sendRequestToLLM(dto);
    }

    @GetMapping("/v1/chat/{sessionId}")
    public ChatSession getChatHistory(@PathVariable String sessionId) {
        return chatService.getSessionHistory(sessionId);
    }
}
