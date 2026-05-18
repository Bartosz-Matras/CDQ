package com.bartoszmatras.cdq.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String ERROR = "error";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP = "timestamp";
    public static final String BAD_REQUEST = "Bad request";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(Map.of(
                ERROR, "File too large",
                MESSAGE, "Maximum upload size exceeded. Limit is 50MB.",
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                ERROR, BAD_REQUEST,
                MESSAGE, e.getMessage(),
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(Map.of(
                ERROR, "Unsupported Media Type",
                MESSAGE, e.getMessage() != null ? e.getMessage() : "Content-Type is not supported",
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipartException(MultipartException e) {
        return ResponseEntity.badRequest().body(Map.of(
                ERROR, BAD_REQUEST,
                MESSAGE, "Failed to parse multipart request. Ensure a valid file is provided.",
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(Map.of(
                ERROR, BAD_REQUEST,
                MESSAGE, "Missing required parameter: " + e.getParameterName(),
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(RejectedExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleRejectedExecution(RejectedExecutionException e) {
        log.warn("Task rejected due to capacity: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                ERROR, "Service unavailable",
                MESSAGE, "Import service is at capacity. Please try again later.",
                TIMESTAMP, Instant.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                ERROR, "Internal server error",
                MESSAGE, "An unexpected error occurred",
                TIMESTAMP, Instant.now().toString()
        ));
    }
}