package org.example.projectbifrost;

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

    @PostMapping("/bifrost")
    public ChatRequestDTO sendChatRequest(@RequestBody ChatRequestDTO dto) {
        chatService.sendRequestToLLM(dto);
        return dto;
    }
}
