package org.example.projectbifrost.dto;

import java.time.LocalDateTime;

/**
 * This record encapsulates the data provided as a response by the chatbot after processing
 * a user’s input. It includes the personality of the chatbot, the generated response,
 * the session identifier for tracking the conversation, and the timestamp indicating
 * when the response was created.
 */
public record ChatResponseDTO(Personality personality,
                              String response,
                              String sessionId,
                              LocalDateTime timestamp) {
}
