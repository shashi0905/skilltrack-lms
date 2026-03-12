package com.skilltrack.api.dto.request;

import com.skilltrack.api.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for reset password request.
 * 
 * Used when user resets password via email link.
 * Link format: {frontendUrl}/reset-password?token={tokenValue}
 * 
 * Reset Password Flow:
 * 1. User receives password reset email
 * 2. User clicks reset link with token
 * 3. Frontend extracts token from URL query parameter
 * 4. User enters new password
 * 5. Frontend sends POST request to /api/auth/reset-password with token + new password
 * 6. Backend validates token (not expired, not used)
 * 7. Backend updates user password (hashed with BCrypt)
 * 8. Token marked as used (prevent reuse)
 * 9. User redirected to login with success message
 * 
 * Security:
 * - Token is UUID (random, unpredictable)
 * - Token expires in 1 hour
 * - Token is single-use (marked as used)
 * - New password must meet complexity requirements (@ValidPassword)
 * - Password is hashed with BCrypt before storage
 * - Old password is not required (user forgot it)
 * 
 * Password Policy:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 * 
 * @see com.skilltrack.api.service.PasswordResetService#resetPassword
 * @see com.skilltrack.common.entity.PasswordResetToken
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @ValidPassword  // Custom annotation for password policy
    private String newPassword;

    @Override
    public String toString() {
        // Never log password
        return "ResetPasswordRequest{token='" + token + "'}";
    }
}
