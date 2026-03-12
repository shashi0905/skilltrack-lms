package com.skilltrack.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * Maps to HTTP 404 Not Found in API responses.
 * 
 * Examples:
 * - User not found by email
 * - Course not found by ID
 * - Enrollment not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found with identifier: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
