package org.example.projectbifrost.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * This record encapsulates the necessary information required to process
 * a chat interaction, including the personality of the chatbot, the message
 * sent by the user, and a session identifier for tracking the conversation.
 */
public record ChatRequestDTO(@NotNull Personality personality,
                             @NotBlank String message,
                             @NotBlank String sessionId) {

}
