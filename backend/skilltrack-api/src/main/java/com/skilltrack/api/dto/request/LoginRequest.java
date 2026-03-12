package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login request.
 * 
 * Authentication Methods:
 * - Email + Password (this DTO)
 * - GitHub OAuth (handled by Spring Security OAuth2 client)
 * 
 * Login Flow:
 * 1. User submits email and password
 * 2. Backend authenticates with Spring Security AuthenticationManager
 * 3. On success, generate JWT access token and refresh token
 * 4. Return tokens + user data
 * 5. Frontend stores tokens (localStorage or httpOnly cookie)
 * 6. Frontend includes access token in Authorization header for API calls
 * 
 * Security Considerations:
 * - Password is never logged or exposed in responses
 * - Failed login attempts are tracked (account lockout after threshold)
 * - Email is case-insensitive for lookup
 * - Rate limiting should be applied at API gateway level
 * 
 * @see com.skilltrack.api.service.AuthService#login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Override
    public String toString() {
        // Never log password
        return "LoginRequest{email='" + email + "'}";
    }
}
