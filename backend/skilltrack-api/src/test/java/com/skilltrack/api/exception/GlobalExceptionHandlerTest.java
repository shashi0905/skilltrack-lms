package com.skilltrack.api.exception;

import com.skilltrack.common.dto.ErrorResponse;
import com.skilltrack.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleValidationErrors_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "email", "bad", false, null, null, "Email is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getDetails()).hasSize(1);
        assertThat(response.getBody().getDetails().get(0).getField()).isEqualTo("email");
    }

    @Test
    void handleValidationException_returns400() {
        ValidationException ex = new ValidationException("Email already registered");

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    }

    @Test
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Course", "123");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleAuthenticationException_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_ERROR");
    }

    @Test
    void handleCustomAuthenticationException_returns401() {
        AuthenticationException ex = new AuthenticationException("Account locked");

        ResponseEntity<ErrorResponse> response = handler.handleCustomAuthenticationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Account locked");
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void handleAuthorizationException_returns403() {
        AuthorizationException ex = new AuthorizationException("Not authorized");

        ResponseEntity<ErrorResponse> response = handler.handleAuthorizationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).isEqualTo("Not authorized");
    }

    @Test
    void handleBusinessException_returns400() {
        BusinessException ex = new BusinessException("Duplicate course title");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Duplicate course title");
    }

    @Test
    void handleGenericException_returns500() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("unexpected error");
    }
}
