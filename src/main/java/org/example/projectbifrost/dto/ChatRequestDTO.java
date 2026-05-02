package org.example.projectbifrost.dto;

/**
 * This record encapsulates the necessary information required to process
 * a chat interaction, including the personality of the chatbot, the message
 * sent by the user, and a session identifier for tracking the conversation.
 */
public record ChatRequestDTO(Personality personality,
                             String message,
                             String sessionId) {

}
