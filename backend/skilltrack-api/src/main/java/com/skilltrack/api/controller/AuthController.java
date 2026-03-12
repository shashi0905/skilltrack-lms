package com.skilltrack.api.controller;

import com.skilltrack.api.dto.request.ForgotPasswordRequest;
import com.skilltrack.api.dto.request.LoginRequest;
import com.skilltrack.api.dto.request.RefreshTokenRequest;
import com.skilltrack.api.dto.request.RegisterRequest;
import com.skilltrack.api.dto.request.ResendVerificationRequest;
import com.skilltrack.api.dto.request.ResetPasswordRequest;
import com.skilltrack.api.dto.request.VerifyEmailRequest;
import com.skilltrack.api.dto.response.LoginResponse;
import com.skilltrack.api.dto.response.MessageResponse;
import com.skilltrack.api.dto.response.UserResponse;
import com.skilltrack.api.service.AuthService;
import com.skilltrack.api.service.PasswordResetService;
import com.skilltrack.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * 
 * Base Path: /api/auth
 * 
 * Endpoints:
 * - POST /register - Register new user
 * - POST /login - Authenticate user (Step 6)
 * - POST /verify-email - Verify email with token (Step 5)
 * - POST /resend-verification - Resend verification email (Step 5)
 * - POST /forgot-password - Request password reset (Step 7)
 * - POST /reset-password - Reset password with token (Step 7)
 * - POST /logout - Invalidate refresh token (Step 6)
 * 
 * Security:
 * - All endpoints in /api/auth/** are public (permitAll in SecurityConfig)
 * - CORS enabled for Angular frontend (localhost:4200)
 * - Request validation with @Valid (automatic 400 on validation errors)
 * - Response DTOs never include password or sensitive data
 * 
 * Error Handling:
 * - Validation errors (400) handled by GlobalExceptionHandler
 * - Business logic errors (400, 404) handled by GlobalExceptionHandler
 * - Internal errors (500) handled by GlobalExceptionHandler
 * 
 * @see UserService Business logic layer
 * @see com.skilltrack.api.exception.GlobalExceptionHandler Error responses
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    /**
     * Register a new user with email and password.
     * 
     * Endpoint: POST /api/auth/register
     * 
     * Request Body:
     * {
     *   "email": "john.doe@example.com",
     *   "password": "SecurePass123!",
     *   "fullName": "John Doe",
     *   "country": "United States",
     *   "organization": "Example Corp",
     *   "roleName": "ROLE_STUDENT"
     * }
     * 
     * Success Response (201 Created):
     * {
     *   "id": "550e8400-e29b-41d4-a716-446655440000",
     *   "email": "john.doe@example.com",
     *   "fullName": "John Doe",
     *   "country": "United States",
     *   "organization": "Example Corp",
     *   "roles": ["ROLE_STUDENT"],
     *   "emailVerificationStatus": "PENDING",
     *   "instructorVerificationStatus": null,
     *   "githubId": null,
     *   "createdAt": "2026-02-20T10:30:00",
     *   "updatedAt": "2026-02-20T10:30:00"
     * }
     * 
     * Error Responses:
     * - 400 Bad Request: Validation error or email already exists
     *   {
     *     "timestamp": "2026-02-20T10:30:00",
     *     "status": 400,
     *     "errorCode": "VALIDATION_ERROR",
     *     "message": "Email is already registered",
     *     "path": "/api/auth/register"
     *   }
     * 
     * Side Effects:
     * - User created in database with PENDING email verification status
     * - Email verification token generated (expires in 1 hour)
     * - Verification email sent to user (async)
     * 
     * Validation:
     * - Email: Valid format, unique (max 255 chars)
     * - Password: Min 8 chars, uppercase, lowercase, digit, special char
     * - Full Name: 2-100 characters
     * - Country: Required, 2-100 characters
     * - Role: ROLE_STUDENT or ROLE_INSTRUCTOR only
     * 
     * Business Rules:
     * - Email is case-insensitive (normalized to lowercase)
     * - Instructors start with PENDING verification status (admin approval required)
     * - Students have full access after email verification
     * - Cannot self-assign ROLE_ADMIN through this endpoint
     * 
     * @param request Registration request with user details
     * @return ResponseEntity with UserResponse (201 Created)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());

        UserResponse response = userService.registerUser(request);

        log.info("User registered successfully: {}", response.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verify user's email with verification token.
     * 
     * Endpoint: POST /api/auth/verify-email
     * 
     * Request Body:
     * {
     *   "token": "550e8400-e29b-41d4-a716-446655440000"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "id": "...",
     *   "email": "user@example.com",
     *   "emailVerificationStatus": "VERIFIED",
     *   ...
     * }
     * 
     * Error Responses:
     * - 400 Bad Request: Invalid token, expired token, or already used
     *   {
     *     "status": 400,
     *     "errorCode": "VALIDATION_ERROR",
     *     "message": "Verification token has expired. Please request a new verification email.",
     *     ...
     *   }
     * 
     * Frontend Flow:
     * 1. User clicks link in email: http://localhost:4200/verify-email?token=abc-123
     * 2. Frontend extracts token from URL
     * 3. Frontend calls this endpoint with token
     * 4. Backend validates and verifies user
     * 5. Frontend shows success message and redirects to login
     * 
     * Security:
     * - Token is UUID (unpredictable)
     * - Token expires in 1 hour
     * - Token is single-use (marked as used)
     * - Idempotent (safe to call multiple times if already verified)
     * 
     * @param request VerifyEmailRequest with token
     * @return ResponseEntity with updated UserResponse
     */
    @PostMapping("/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Email verification request received for token: {}", request.getToken());

        UserResponse response = userService.verifyEmail(request.getToken());

        log.info("Email verified successfully for user: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Resend email verification link to user.
     * 
     * Endpoint: POST /api/auth/resend-verification
     * 
     * Request Body:
     * {
     *   "email": "user@example.com"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "message": "If your email is registered and not verified, you will receive a verification link shortly",
     *   "success": true,
     *   "timestamp": "2026-02-23T10:30:00"
     * }
     * 
     * Error Response:
     * - 400 Bad Request: Email already verified
     *   {
     *     "status": 400,
     *     "errorCode": "VALIDATION_ERROR",
     *     "message": "Email is already verified",
     *     ...
     *   }
     * 
     * Use Cases:
     * - User didn't receive original email
     * - Original token expired (1 hour)
     * - User deleted email
     * 
     * Security:
     * - Always returns success (even if email doesn't exist) to prevent email enumeration
     * - Only fails if email is already verified
     * - Old unused tokens are deleted
     * - New token generated with fresh expiration
     * - Rate limiting should be applied (TODO: implement in API gateway)
     * 
     * Frontend Flow:
     * 1. User clicks "Resend verification email" button
     * 2. User enters email
     * 3. Frontend calls this endpoint
     * 4. Backend sends new verification email (if eligible)
     * 5. Frontend shows success message
     * 
     * @param request ResendVerificationRequest with email
     * @return ResponseEntity with MessageResponse
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("Resend verification request received for email: {}", request.getEmail());

        userService.resendVerificationEmail(request.getEmail());

        MessageResponse response = new MessageResponse(
            "If your email is registered and not verified, you will receive a verification link shortly"
        );

        log.info("Resend verification processed for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticate user and generate JWT tokens.
     * 
     * Endpoint: POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "SecurePass123!"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 86400,
     *   "user": {
     *     "id": "550e8400-e29b-41d4-a716-446655440000",
     *     "email": "user@example.com",
     *     "fullName": "John Doe",
     *     "roles": ["ROLE_STUDENT"],
     *     "emailVerificationStatus": "VERIFIED",
     *     "instructorVerificationStatus": null
     *   }
     * }
     * 
     * Error Responses:
     * - 401 Unauthorized: Invalid credentials or account locked
     *   {
     *     "status": 401,
     *     "errorCode": "AUTHENTICATION_ERROR",
     *     "message": "Invalid email or password",
     *     ...
     *   }
     * 
     * Frontend Flow:
     * 1. User submits login form
     * 2. Frontend calls this endpoint
     * 3. Backend validates credentials
     * 4. Backend returns JWT tokens + user data
     * 5. Frontend stores tokens (localStorage or cookie)
     * 6. Frontend includes access token in Authorization header for API calls
     * 
     * Token Usage:
     * - Access Token: Include in Authorization header as "Bearer {token}"
     * - Refresh Token: Send to /api/auth/refresh when access token expires
     * 
     * Account Lockout:
     * - Account locked after 5 failed login attempts
     * - Locked accounts cannot login (even with correct password)
     * - Contact support to unlock (future: auto-unlock after time period)
     * 
     * Security:
     * - Password is validated by Spring Security PasswordEncoder (BCrypt)
     * - Failed login attempts are tracked
     * - Account locked after threshold (5 attempts)
     * - JWT tokens are signed (cannot be tampered)
     * - Short-lived access token (24 hours)
     * - Longer-lived refresh token (7 days)
     * 
     * @param request Login credentials
     * @return ResponseEntity with LoginResponse (tokens + user data)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        log.info("Login successful for user: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token.
     * 
     * Endpoint: POST /api/auth/refresh
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // New token
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // Same token
     *   "tokenType": "Bearer",
     *   "expiresIn": 86400,
     *   "user": { ... }
     * }
     * 
     * Error Response:
     * - 401 Unauthorized: Invalid or expired refresh token
     *   {
     *     "status": 401,
     *     "errorCode": "AUTHENTICATION_ERROR",
     *     "message": "Invalid or expired refresh token",
     *     ...
     *   }
     * 
     * Use Cases:
     * - Access token expired (24 hours)
     * - User wants to stay logged in without re-entering password
     * 
     * Frontend Flow:
     * 1. API call returns 401 Unauthorized (access token expired)
     * 2. Frontend calls this endpoint with refresh token
     * 3. Backend validates refresh token and generates new access token
     * 4. Frontend updates stored access token
     * 5. Frontend retries failed API call with new access token
     * 
     * Token Rotation (Future Enhancement):
     * - Currently returns same refresh token
     * - Production: Generate new refresh token on each refresh
     * - Production: Invalidate old refresh token (blacklist in Redis)
     * - Improves security (limits impact if refresh token stolen)
     * 
     * @param request Refresh token request
     * @return ResponseEntity with LoginResponse (new access token)
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        LoginResponse response = authService.refreshToken(request);

        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user (client-side operation for stateless JWT).
     * 
     * Endpoint: POST /api/auth/logout
     * 
     * Success Response (200 OK):
     * {
     *   "message": "Logged out successfully",
     *   "success": true,
     *   "timestamp": "2026-02-23T10:30:00"
     * }
     * 
     * Logout Strategy for Stateless JWT:
     * - There is NO server-side session to invalidate
     * - JWT tokens are self-contained and stateless
     * - Tokens remain valid until expiration (cannot be revoked)
     * 
     * Frontend Logout (Required):
     * 1. Delete access token from storage (localStorage/cookie)
     * 2. Delete refresh token from storage
     * 3. Clear any user data from state/memory
     * 4. Redirect to login page
     * 
     * Backend Logout (Optional):
     * This endpoint is optional for stateless JWT. It can:
     * - Log audit trail (user logged out at X time)
     * - Add token to blacklist in Redis (if implementing token revocation)
     * - Trigger cleanup of user-specific cache
     * 
     * Token Revocation (Future Enhancement):
     * - Maintain blacklist of revoked tokens in Redis
     * - JwtAuthenticationFilter checks blacklist on each request
     * - Add token to blacklist on logout
     * - Remove from blacklist when token expires naturally
     * 
     * Security Note:
     * - Stolen tokens remain valid until expiration
     * - Keep access token expiration short (24 hours)
     * - Implement token revocation for sensitive applications
     * 
     * @return ResponseEntity with MessageResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        log.info("Logout request received");

        // For stateless JWT, logout is primarily client-side
        // This endpoint is for audit logging and future token blacklisting

        MessageResponse response = new MessageResponse(
            "Logged out successfully. Please delete your tokens from client storage."
        );

        log.info("Logout processed");
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset email (forgot password).
     * 
     * Endpoint: POST /api/auth/forgot-password
     * 
     * Request Body:
     * {
     *   "email": "user@example.com"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "message": "If your email is registered, you will receive a password reset link shortly",
     *   "success": true,
     *   "timestamp": "2026-02-23T10:30:00"
     * }
     * 
     * Use Cases:
     * - User forgot their password
     * - User wants to reset password for security
     * - Account recovery
     * 
     * Frontend Flow:
     * 1. User clicks "Forgot Password?" on login page
     * 2. User enters email address
     * 3. Frontend calls this endpoint
     * 4. Backend sends reset email (if email exists)
     * 5. Frontend shows success message
     * 6. User receives email with reset link
     * 7. User clicks link → Frontend → Reset password page
     * 
     * Email Content:
     * - Subject: "Reset Your Password - SkillTrack LMS"
     * - Reset link: http://localhost:4200/reset-password?token=abc-123
     * - Token expires in 1 hour
     * - Security warning if not requested
     * 
     * Security:
     * - Always returns success (even if email doesn't exist) to prevent email enumeration
     * - Token expires in 1 hour (short window)
     * - Token is single-use (marked as used after reset)
     * - Old unused tokens deleted when new token generated
     * - Rate limiting should be applied (prevent spam)
     * 
     * Email Enumeration Prevention:
     * - Generic success message regardless of email existence
     * - Prevents attackers from discovering registered emails
     * - Best practice for security
     * 
     * @param request Forgot password request with email
     * @return ResponseEntity with MessageResponse
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.getEmail());

        passwordResetService.forgotPassword(request.getEmail());

        MessageResponse response = new MessageResponse(
            "If your email is registered, you will receive a password reset link shortly"
        );

        log.info("Forgot password processed for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Reset password with reset token.
     * 
     * Endpoint: POST /api/auth/reset-password
     * 
     * Request Body:
     * {
     *   "token": "550e8400-e29b-41d4-a716-446655440000",
     *   "newPassword": "NewSecurePass123!"
     * }
     * 
     * Success Response (200 OK):
     * {
     *   "message": "Password reset successfully. You can now login with your new password.",
     *   "success": true,
     *   "timestamp": "2026-02-23T10:30:00"
     * }
     * 
     * Error Responses:
     * - 400 Bad Request: Invalid token, expired token, or already used
     *   {
     *     "status": 400,
     *     "errorCode": "VALIDATION_ERROR",
     *     "message": "Password reset token has expired. Please request a new password reset.",
     *     ...
     *   }
     * 
     * - 400 Bad Request: Password validation error
     *   {
     *     "status": 400,
     *     "errorCode": "VALIDATION_ERROR",
     *     "message": "Validation failed",
     *     "details": [
     *       {
     *         "field": "newPassword",
     *         "message": "Password must contain at least one uppercase letter (A-Z)"
     *       }
     *     ]
     *   }
     * 
     * Frontend Flow:
     * 1. User clicks reset link in email
     * 2. Frontend extracts token from URL: /reset-password?token=abc-123
     * 3. Frontend shows reset password form
     * 4. User enters new password
     * 5. Frontend calls this endpoint with token + new password
     * 6. Backend validates token and updates password
     * 7. Frontend shows success message
     * 8. Frontend redirects to login page
     * 
     * Password Requirements:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character (!@#$%^&*()-_+=)
     * 
     * Security:
     * - Token validated (not expired, not used)
     * - Password complexity enforced (@ValidPassword)
     * - Password hashed with BCrypt before storage
     * - Token marked as used (single-use)
     * - Failed login attempts reset
     * - Account unlocked if was locked
     * 
     * Post-Reset:
     * - User can login with new password
     * - Old password is permanently replaced
     * - Token cannot be reused
     * - Failed login counter reset to 0
     * 
     * @param request Reset password request with token and new password
     * @return ResponseEntity with MessageResponse
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset request received for token: {}", request.getToken());

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        MessageResponse response = new MessageResponse(
            "Password reset successfully. You can now login with your new password."
        );

        log.info("Password reset successfully");
        return ResponseEntity.ok(response);
    }

    // TODO: Step 8 - GitHub OAuth endpoints
    // @PostMapping("/login")
    // @PostMapping("/logout")

    // TODO: Step 7 - Password reset endpoints
    // @PostMapping("/forgot-password")
    // @PostMapping("/reset-password")

    // TODO: Step 8 - OAuth endpoints
    // OAuth handled by Spring Security OAuth2 client (redirect flow)
}
