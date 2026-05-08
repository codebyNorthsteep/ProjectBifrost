package org.example.projectbifrost.exception;

import lombok.Getter;

@Getter
public class InvalidLLMResponseException extends RuntimeException {
    private final int statusCode;
    private final String model;

    public InvalidLLMResponseException(String message, String model, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.model = model;
    }
}
