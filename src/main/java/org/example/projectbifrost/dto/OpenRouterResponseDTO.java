package org.example.projectbifrost.dto;

import java.util.List;

public record OpenRouterResponseDTO(List<Choice> choices) {
     public record Choice(Message message) {}
    public record Message(String content){}
}
