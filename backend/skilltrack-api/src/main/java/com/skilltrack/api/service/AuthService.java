package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.LoginRequest;
import com.skilltrack.api.dto.request.RefreshTokenRequest;
import com.skilltrack.api.dto.response.LoginResponse;
import com.skilltrack.api.security.JwtTokenProvider;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.exception.AuthenticationException;
import com.skilltrack.common.exception.ValidationException;
import com.skilltrack.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Service for authentication operations (login, logout, token refresh).
 * 
 * Responsibilities:
 * - User authentication (email + password)
 * - JWT token generation (access + refresh)
 * - Token refresh (obtain new access token)
 * - Failed login attempt tracking
 * - Account lockout enforcement
 * 
 * Authentication Flow:
 * 1. User submits email + password
 * 2. AuthenticationManager validates credentials
 * 3. Spring Security calls UserDetailsService to load user
 * 4. PasswordEncoder verifies password hash
 * 5. On success: Generate JWT tokens, reset failed attempts
 * 6. On failure: Increment failed attempts, lock account if threshold reached
 * 
 * Account Lockout Policy:
 * - Lock account after 5 failed login attempts
 * - Locked accounts cannot login (even with correct password)
 * - Admin must unlock account (future: auto-unlock after time period)
 * 
 * Token Strategy:
 * - Access Token: 24 hours, for API authentication
 * - Refresh Token: 7 days, for obtaining new access tokens
 * - Stateless JWT (no server-side session)
 * 
 * @see JwtTokenProvider JWT generation and validation
 * @see com.skilltrack.api.security.UserDetailsServiceImpl User loading
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * Authenticate user and generate JWT tokens.
     * 
     * Login Flow:
     * 1. Normalize email (lowercase)
     * 2. Check if account exists and not locked
     * 3. Authenticate with Spring Security (validates password)
     * 4. On success: Generate tokens, reset failed attempts
     * 5. On failure: Increment failed attempts, lock if threshold reached
     * 6. Return tokens + user data
     * 
     * Account Lockout:
     * - After 5 failed attempts, account is locked
     * - Locked accounts cannot login (throws LockedException)
     * - Failed attempts reset on successful login
     * 
     * Error Cases:
     * - Invalid credentials → AuthenticationException (401)
     * - Account locked → AuthenticationException (401)
     * - User not found → AuthenticationException (401)
     * 
     * @param request Login credentials (email, password)
     * @return LoginResponse with access token, refresh token, and user data
     * @throws AuthenticationException if authentication fails
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Login attempt for email: {}", email);

        try {
            // Find user (for failed attempt tracking)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            // Check if account is locked
            if (user.isAccountLocked()) {
                log.warn("Login attempt for locked account: {}", email);
                throw new AuthenticationException("Account is locked due to multiple failed login attempts. Please contact support.");
            }

            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            // Authentication successful - reset failed attempts
            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedLoginAttempts();
                userRepository.save(user);
                log.info("Reset failed login attempts for user: {}", email);
            }

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            // Build response
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime() / 1000) // Convert ms to seconds
                    .user(buildUserInfo(user))
                    .build();

            log.info("Login successful for user: {}", email);
            return response;

        } catch (BadCredentialsException e) {
            // Invalid password - increment failed attempts
            handleFailedLogin(email);
            throw new AuthenticationException("Invalid email or password");

        } catch (LockedException e) {
            // Account locked (shouldn't reach here due to check above, but safety net)
            throw new AuthenticationException("Account is locked due to multiple failed login attempts. Please contact support.");
        }
    }

    /**
     * Refresh access token using refresh token.
     * 
     * Token Refresh Flow:
     * 1. Validate refresh token (signature, expiration)
     * 2. Extract username from refresh token
     * 3. Load user from database
     * 4. Generate new access token
     * 5. Optionally generate new refresh token (token rotation)
     * 6. Return new tokens
     * 
     * Token Rotation:
     * - Currently returns same refresh token
     * - Production: Generate new refresh token on each refresh (more secure)
     * - Production: Invalidate old refresh token (blacklist in Redis)
     * 
     * Error Cases:
     * - Invalid refresh token → AuthenticationException (401)
     * - Expired refresh token → AuthenticationException (401)
     * - User not found → AuthenticationException (401)
     * 
     * @param request Refresh token request
     * @return LoginResponse with new access token
     * @throws AuthenticationException if refresh token invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        log.info("Token refresh request received");

        try {
            // Validate refresh token
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthenticationException("Invalid or expired refresh token");
            }

            // Extract username from token
            String email = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // Load user
            User user = userRepository.findByEmailWithRoles(email)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Check if account is locked
            if (user.isAccountLocked()) {
                throw new AuthenticationException("Account is locked");
            }

            // Create authentication object for token generation
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email, null, 
                    user.getRoles().stream()
                            .map(role -> (org.springframework.security.core.GrantedAuthority) 
                                () -> role.getRoleName().name())
                            .collect(Collectors.toList())
            );

            // Generate new access token
            String newAccessToken = jwtTokenProvider.generateToken(authentication);

            // Build response (reuse same refresh token for simplicity)
            // Production: Generate new refresh token and invalidate old one
            LoginResponse response = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Reuse same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime() / 1000)
                    .user(buildUserInfo(user))
                    .build();

            log.info("Token refreshed successfully for user: {}", email);
            return response;

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid or expired refresh token");
        }
    }

    /**
     * Handle failed login attempt.
     * 
     * Failed Login Flow:
     * 1. Load user by email
     * 2. Increment failed login attempts counter
     * 3. If attempts >= MAX_FAILED_ATTEMPTS (5), lock account
     * 4. Save user
     * 
     * Account Lockout:
     * - Locked after 5 failed attempts
     * - Requires admin intervention to unlock
     * - Future: Auto-unlock after time period (e.g., 30 minutes)
     * 
     * @param email User email
     */
    private void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount();
                log.warn("Account locked due to {} failed login attempts: {}", 
                        MAX_FAILED_ATTEMPTS, email);
            } else {
                log.info("Failed login attempt {} of {} for user: {}", 
                        user.getFailedLoginAttempts(), MAX_FAILED_ATTEMPTS, email);
            }
            
            userRepository.save(user);
        });
    }

    /**
     * Build user info for login response.
     * 
     * @param user User entity
     * @return UserInfo DTO
     */
    private LoginResponse.UserInfo buildUserInfo(User user) {
        return LoginResponse.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()))
                .emailVerificationStatus(user.getEmailVerificationStatus().name())
                .instructorVerificationStatus(
                    user.getInstructorVerificationStatus() != null 
                        ? user.getInstructorVerificationStatus().name() 
                        : null
                )
                .build();
    }
}
