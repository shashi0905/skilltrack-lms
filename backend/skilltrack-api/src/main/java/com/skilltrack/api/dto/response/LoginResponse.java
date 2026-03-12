package com.skilltrack.api.dto.response;

import com.skilltrack.common.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for login response containing JWT tokens and user data.
 * 
 * Response Structure:
 * - accessToken: Short-lived JWT (24 hours) for API authentication
 * - refreshToken: Longer-lived JWT (7 days) for obtaining new access tokens
 * - tokenType: Always "Bearer" (for Authorization header)
 * - expiresIn: Access token expiration in seconds (86400 = 24h)
 * - user: Basic user data (id, email, name, roles, verification status)
 * 
 * Frontend Token Storage:
 * Option 1: localStorage (vulnerable to XSS but easier to implement)
 * Option 2: httpOnly cookie (secure but requires CSRF protection)
 * Option 3: Combination (access in memory, refresh in httpOnly cookie)
 * 
 * Frontend Usage:
 * ```javascript
 * // Store tokens
 * localStorage.setItem('accessToken', response.accessToken);
 * localStorage.setItem('refreshToken', response.refreshToken);
 * 
 * // Include in API calls
 * fetch('/api/protected', {
 *   headers: {
 *     'Authorization': `Bearer ${accessToken}`
 *   }
 * });
 * ```
 * 
 * Security:
 * - Tokens are JWT (signed, cannot be tampered)
 * - Short expiration for access token (limits impact if stolen)
 * - Refresh token for better UX (no frequent re-login)
 * - User data includes verification status (frontend can show warnings)
 * 
 * @see com.skilltrack.api.service.AuthService#login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT access token for API authentication.
     * Include in Authorization header: "Bearer {accessToken}"
     */
    private String accessToken;

    /**
     * JWT refresh token for obtaining new access tokens.
     * Send to /api/auth/refresh when access token expires.
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access token expiration time in seconds (e.g., 86400 = 24 hours).
     */
    private Long expiresIn;

    /**
     * Authenticated user data.
     */
    private UserInfo user;

    /**
     * Nested DTO for user information in login response.
     * Subset of UserResponse with only essential fields.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String fullName;
        private Set<RoleName> roles;
        private String emailVerificationStatus;
        private String instructorVerificationStatus;
    }
}
