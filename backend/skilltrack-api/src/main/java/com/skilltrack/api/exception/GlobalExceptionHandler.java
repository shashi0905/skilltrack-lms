package com.skilltrack.api.exception;

import com.skilltrack.common.dto.ErrorResponse;
import com.skilltrack.common.exception.AuthenticationException;
import com.skilltrack.common.exception.AuthorizationException;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * 
 * @RestControllerAdvice applies to all @RestController classes
 * Catches exceptions and converts them to proper HTTP responses.
 * 
 * Benefits:
 * - Consistent error response format across all endpoints
 * - Centralized error handling (DRY principle)
 * - Proper HTTP status codes
 * - Logging of errors for debugging
 * - Hides internal error details from clients (security)
 * 
 * Learning points:
 * - @ExceptionHandler methods catch specific exception types
 * - Order matters: most specific exceptions should be handled first
 * - Always log errors for debugging
 * - Never expose stack traces to clients
 * - Use appropriate HTTP status codes
 * 
 * HTTP status code reference:
 * - 400 Bad Request: Validation errors, malformed input
 * - 401 Unauthorized: Authentication failed (no/invalid credentials)
 * - 403 Forbidden: Authenticated but not authorized
 * - 404 Not Found: Resource doesn't exist
 * - 500 Internal Server Error: Unexpected server error
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors (Bean Validation annotations).
     * 
     * Triggered when @Valid fails on request DTOs.
     * Example: @NotBlank, @Email, @Size, @Min, @Max
     * 
     * @param ex Exception containing validation errors
     * @param request HTTP request
     * @return 400 Bad Request with field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        // Extract field errors
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .details(fieldErrors)
                .build();
        
        log.warn("Validation error: {} fields failed validation", fieldErrors.size());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles custom validation exceptions.
     * 
     * Example: throw new ValidationException("Email already registered")
     * 
     * @param ex Custom validation exception
     * @param request HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.warn("Validation exception: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles resource not found exceptions.
     * 
     * Example: User not found, Course not found
     * 
     * @param ex Resource not found exception
     * @param request HTTP request
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles Spring Security authentication exceptions.
     * 
     * Includes BadCredentialsException (wrong password)
     * 
     * @param ex Spring Security authentication exception
     * @param request HTTP request
     * @return 401 Unauthorized
     */
    @ExceptionHandler({org.springframework.security.core.AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "AUTHENTICATION_ERROR",
                "Invalid credentials or authentication failed",
                request.getRequestURI()
        );
        
        log.warn("Authentication failed: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles custom authentication exceptions.
     * 
     * Example: Account locked, email not verified
     * 
     * @param ex Custom authentication exception
     * @param request HTTP request
     * @return 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.warn("Authentication exception: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handles Spring Security access denied exceptions.
     * 
     * Triggered when authenticated user lacks required role/permission.
     * Example: Student trying to access admin endpoint
     * 
     * @param ex Access denied exception
     * @param request HTTP request
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                request.getRequestURI()
        );
        
        log.warn("Access denied: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles custom authorization exceptions.
     * 
     * Example: Unverified user trying to enroll in course
     * 
     * @param ex Custom authorization exception
     * @param request HTTP request
     * @return 403 Forbidden
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationException(
            AuthorizationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.warn("Authorization exception: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handles generic business exceptions.
     * 
     * Fallback for BusinessException subclasses not handled above.
     * 
     * @param ex Business exception
     * @param request HTTP request
     * @return 400 Bad Request
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI()
        );
        
        log.warn("Business exception: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     * 
     * Catches runtime exceptions not handled by specific handlers.
     * Should be the last handler (most generic).
     * 
     * Security: Never expose internal error details to client
     * 
     * @param ex Any exception
     * @param request HTTP request
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        // Log full stack trace for debugging (not sent to client)
        log.error("Unexpected error occurred", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
