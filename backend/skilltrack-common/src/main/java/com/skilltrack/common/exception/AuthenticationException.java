package com.skilltrack.common.exception;

/**
 * Exception thrown for authentication failures.
 * 
 * Maps to HTTP 401 Unauthorized in API responses.
 * 
 * Examples:
 * - Invalid credentials
 * - Expired token
 * - Account locked
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_ERROR", message, cause);
    }
}
