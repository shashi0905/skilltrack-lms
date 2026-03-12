package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for refresh token request.
 * 
 * Purpose:
 * - Obtain new access token without re-entering credentials
 * - Access tokens are short-lived (24 hours)
 * - Refresh tokens are longer-lived (7 days)
 * 
 * Token Refresh Flow:
 * 1. Access token expires (24 hours)
 * 2. Frontend detects 401 Unauthorized
 * 3. Frontend calls /api/auth/refresh with refresh token
 * 4. Backend validates refresh token
 * 5. Backend generates new access token (and optionally new refresh token)
 * 6. Frontend updates stored tokens
 * 7. Frontend retries failed request with new access token
 * 
 * Security:
 * - Refresh token is JWT (signed, cannot be tampered)
 * - Refresh token expires after 7 days (configurable)
 * - Refresh token can be revoked (blacklist in Redis/DB)
 * - Refresh token rotation: Generate new refresh token on each refresh
 * 
 * @see com.skilltrack.api.service.AuthService#refreshToken
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
