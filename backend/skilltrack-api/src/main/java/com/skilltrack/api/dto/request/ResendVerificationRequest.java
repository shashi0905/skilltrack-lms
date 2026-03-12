package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for resending email verification link.
 * 
 * Use Cases:
 * - User didn't receive original email
 * - Original verification token expired (1 hour)
 * - Email was deleted accidentally
 * 
 * Business Rules:
 * - Can only resend if user exists
 * - Can only resend if email NOT already verified
 * - Invalidates old tokens (security best practice)
 * - Generates new token with fresh expiration
 * 
 * Security Considerations:
 * - Always return success (even if email doesn't exist) to prevent email enumeration
 * - Rate limiting should be applied (prevent spam)
 * - Track resend attempts (detect abuse)
 * 
 * @see com.skilltrack.api.service.UserService#resendVerificationEmail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;
}
