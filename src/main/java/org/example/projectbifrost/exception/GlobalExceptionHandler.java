package org.example.projectbifrost.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;

@ControllerAdvice
@Slf4j //Structured logging
public class GlobalExceptionHandler {
    private static final String TIMESTAMP_PROPERTY = "timestamp";

    @ExceptionHandler(LLMException.class)
    public ResponseEntity<ProblemDetail> handleLLMException(LLMException ex) {
        HttpStatus status;
        try {
            status = HttpStatus.valueOf(ex.getStatusCode()); //Get the error from OpenRouter
        } catch (IllegalArgumentException _) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.warn("LLM Error [{}]: {}", status.value(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
        problem.setTitle("LLM Error");
        return ResponseEntity.status(status).body(problem);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ProblemDetail> handleTimeoutException(ResourceAccessException ex) {
        log.warn("Timeout connecting to LLM: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Connection timeout. The Gods are taking too long to respond.");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
        problem.setTitle("Connection Timeout");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(problem);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error - {} field(s) invalid", ex.getBindingResult().getErrorCount());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request - check your input");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
        problem.setTitle("Validation Error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
        problem.setTitle("Malformed Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        //If Spring has something to say about an error(e.g 404), use it!
        if (ex instanceof ErrorResponse er) {
            var status = er.getStatusCode();
            String message = ex.getMessage() != null ? ex.getMessage() : "Request failed";
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
            problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
            return ResponseEntity.status(status).body(problem);
        }
        log.error("Unexpected error: ", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now().toString());
        problem.setTitle("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
