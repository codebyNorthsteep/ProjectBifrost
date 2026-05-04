package org.example.projectbifrost.dto;

import java.util.List;
import java.util.Map;

public record OpenRouterRequestDTO(String model,
                                   List<Map<String, String>> messages) {
}
