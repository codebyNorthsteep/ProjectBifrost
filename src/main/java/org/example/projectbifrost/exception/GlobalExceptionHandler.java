package org.example.projectbifrost.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LLMException.class)
    public ResponseEntity<ApiErrorResponse> handleLLMException(LLMException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE; // Default 503
        if (ex.getMessage().contains("429") || ex.getMessage().contains("limit")) {
            status = HttpStatus.TOO_MANY_REQUESTS; // 429
        }
        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                status.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleTimeoutException(ResourceAccessException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                "The connection to the divine realm timed out. Please try again.",
                HttpStatus.REQUEST_TIMEOUT.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(Exception ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                "The request was invalid: " + ex.getLocalizedMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //Handle other internal exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        // Logg stacktrace
        logger.error("Unexpected error: ", ex);
        ApiErrorResponse error = new ApiErrorResponse(
                "An unexpected error occurred in the Bifrost gateway",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
