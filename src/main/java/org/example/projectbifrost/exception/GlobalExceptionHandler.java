package org.example.projectbifrost.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j //Structured logging
public class GlobalExceptionHandler {

    @ExceptionHandler(LLMException.class)
    public ResponseEntity<ApiErrorResponse> handleLLMException(LLMException ex) {
        HttpStatus status;
        try {
            status = HttpStatus.valueOf(ex.getStatusCode()); //Get the error from OpenRouter
        } catch (IllegalArgumentException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.warn("LLM Error [{}]: {}", status.value(), ex.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), status.value(), ex.getMessage()),
                status
        );
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleTimeoutException(ResourceAccessException ex) {
        log.warn("Timeout connecting to LLM: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), 504, "Connection timeout. The Gods are taking too long to respond."),
                HttpStatus.GATEWAY_TIMEOUT
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error - {} field(s) invalid", ex.getBindingResult().getErrorCount());
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), 400, "Invalid request - check your input"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Malformed request body"),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        //If Spring has something to say about an error(e.g 404), use it!
        if (ex instanceof ErrorResponse er) {
            var status = er.getStatusCode();
            String message = ex.getMessage() != null ? ex.getMessage() : "Request failed";
            return ResponseEntity.status(status).body(
                    new ApiErrorResponse(LocalDateTime.now(), status.value(), message)
            );
        }
        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), 500, "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
