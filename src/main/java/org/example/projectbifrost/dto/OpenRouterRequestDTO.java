package org.example.projectbifrost.dto;

import java.util.List;

/**
 * A data transfer object representing a request to interact with the OpenRouter API.
 * This DTO encapsulates the model to be used and a list of message exchanges.
 *
 * @param model The specific model to use for generating responses, typically representing
 *              the desired large language model or OpenRouter configuration.
 * @param messages A list of message exchanges that define the context of the interaction.
 *                 Each message contains a role (e.g., "user" or "assistant") and the message content.
 */
public record OpenRouterRequestDTO(String model,
                                   List<Message> messages) {
    public record Message(String role, String content) {
    }
}
