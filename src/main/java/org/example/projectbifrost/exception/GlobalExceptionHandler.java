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
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    /**
     * Fallback handler for all unexpected internal exceptions.
     *
     * This method first checks if the exception implements the ErrorResponse interface (introduced in Spring 6).
     * This prevents the handler from masking standard Spring framework errors—such as 404 (Not Found),
     * 405 (Method Not Allowed), or 415 (Unsupported Media Type)—with a generic 500 status code.
     * If the exception is a genuine, unhandled internal error, it is logged with a full stack trace
     * and returns a 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            HttpStatus status = HttpStatus.valueOf(er.getStatusCode().value());
            return new ResponseEntity<>(
                    new ApiErrorResponse(LocalDateTime.now(), status.value(), ex.getMessage()),
                    status
            );
        }
        log.error("Unexpected error: ", ex);
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(), 500, "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
