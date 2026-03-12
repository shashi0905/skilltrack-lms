package com.skilltrack.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API error response DTO.
 * 
 * Used by global exception handler to return consistent error responses.
 * 
 * Follows coding standards:
 * - Consistent error structure across all endpoints
 * - Includes timestamp, error code, and user-friendly message
 * - Optional details for validation errors
 * - JsonInclude to exclude null fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp when error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code.
     */
    private int status;

    /**
     * Application-specific error code.
     * Examples: VALIDATION_ERROR, RESOURCE_NOT_FOUND, AUTHENTICATION_ERROR
     */
    private String errorCode;

    /**
     * User-friendly error message.
     * Should not expose sensitive information or stack traces.
     */
    private String message;

    /**
     * Request path where error occurred.
     */
    private String path;

    /**
     * Detailed error information (validation errors, field-level errors).
     * Only included for validation failures.
     */
    private List<FieldError> details;

    /**
     * Field-level error for validation failures.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    /**
     * Factory method for simple error responses.
     */
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }
}
