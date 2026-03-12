package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email verification request.
 * 
 * Used when user clicks verification link in email.
 * Link format: {frontendUrl}/verify-email?token={tokenValue}
 * 
 * Frontend Flow:
 * 1. User clicks link in email
 * 2. Frontend extracts token from URL query parameter
 * 3. Frontend sends POST request to /api/auth/verify-email with this DTO
 * 4. Backend validates token and marks user as verified
 * 
 * Security:
 * - Token is UUID (random, unpredictable)
 * - Token expires in 1 hour
 * - Token is single-use (marked as used after verification)
 * - Token is linked to specific user (cannot be used by others)
 * 
 * @see com.skilltrack.common.entity.EmailVerificationToken
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {

    @NotBlank(message = "Verification token is required")
    private String token;
}
