package org.example.projectbifrost.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(String message,
                               int status,
                               LocalDateTime timestamp) {
}
