package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for forgot password request.
 * 
 * Use Cases:
 * - User forgot their password
 * - User wants to reset password (security measure)
 * - Account recovery
 * 
 * Forgot Password Flow:
 * 1. User clicks "Forgot Password?" link on login page
 * 2. User enters their email address
 * 3. Backend generates password reset token (expires in 1 hour)
 * 4. Backend sends reset link via email
 * 5. User clicks link in email
 * 6. Frontend redirects to reset password page with token
 * 7. User enters new password
 * 8. Backend validates token and updates password
 * 
 * Security Considerations:
 * - Always return success (even if email doesn't exist) to prevent email enumeration
 * - Token expires in 1 hour (short window to prevent abuse)
 * - Token is single-use (marked as used after reset)
 * - Rate limiting should be applied (prevent spam)
 * - Email contains reset link, not the password itself
 * 
 * Email Enumeration Prevention:
 * - Even if email doesn't exist, return "success" message
 * - Generic message: "If your email is registered, you will receive a reset link"
 * - Prevents attackers from discovering registered emails
 * 
 * @see com.skilltrack.api.service.PasswordResetService#forgotPassword
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;
}
