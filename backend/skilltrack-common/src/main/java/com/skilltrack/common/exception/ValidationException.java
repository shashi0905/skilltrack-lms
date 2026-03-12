package com.skilltrack.common.exception;

/**
 * Exception thrown when a validation rule is violated.
 * 
 * Maps to HTTP 400 Bad Request in API responses.
 * 
 * Examples:
 * - Email already registered
 * - Invalid token
 * - Weak password
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Throwable cause) {
        super("VALIDATION_ERROR", message, cause);
    }
}
