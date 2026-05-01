package org.example.projectbifrost.dto;

import java.time.LocalDateTime;

public record ChatResponseDTO(String personality,
                              String response,
                              String sessionId,
                              LocalDateTime timestamp) {
}
