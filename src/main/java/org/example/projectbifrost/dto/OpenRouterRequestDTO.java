package org.example.projectbifrost.dto;

import java.util.List;

public record OpenRouterRequestDTO(String model,
                                   List<Message> messages) {
    public record Message(String role, String content) {
    }
}
