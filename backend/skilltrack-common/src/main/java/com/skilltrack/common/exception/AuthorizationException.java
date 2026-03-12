package com.skilltrack.common.exception;

/**
 * Exception thrown when a user tries to perform an action they're not authorized for.
 * 
 * Maps to HTTP 403 Forbidden in API responses.
 * 
 * Examples:
 * - Unverified user trying to enroll in course
 * - Student trying to access admin endpoint
 * - User trying to modify another user's data
 */
public class AuthorizationException extends BusinessException {

    public AuthorizationException(String message) {
        super("AUTHORIZATION_ERROR", message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super("AUTHORIZATION_ERROR", message, cause);
    }
}
