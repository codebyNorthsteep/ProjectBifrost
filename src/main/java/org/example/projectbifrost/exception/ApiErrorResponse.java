package org.example.projectbifrost.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(LocalDateTime timestamp,
                               int status,
                               String message)            // ← Detailed message for error

{
}
