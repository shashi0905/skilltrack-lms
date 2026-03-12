package com.skilltrack.common.exception;

/**
 * Base exception for all SkillTrack business exceptions.
 * 
 * All business exceptions should extend this class.
 * They are runtime exceptions (unchecked) to avoid cluttering code with try-catch.
 * 
 * Follows coding standards:
 * - Prefer unchecked exceptions for business logic
 * - Include error code for API error responses
 * - Support cause chaining for debugging
 */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
