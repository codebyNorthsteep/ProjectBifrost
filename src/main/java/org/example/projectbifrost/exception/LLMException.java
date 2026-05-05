package org.example.projectbifrost.exception;

import lombok.Getter;

@Getter
public class LLMException extends RuntimeException {
    private final String provider;
    public LLMException(String message, String provider) {
        super(message);
        this.provider = provider;
    }
}
