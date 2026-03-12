package com.skilltrack.api.service;

import com.skilltrack.common.entity.PasswordResetToken;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.exception.ValidationException;
import com.skilltrack.common.repository.PasswordResetTokenRepository;
import com.skilltrack.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for password reset operations.
 * 
 * Responsibilities:
 * - Generate password reset tokens
 * - Send password reset emails
 * - Validate reset tokens
 * - Update user passwords
 * 
 * Password Reset Flow:
 * 1. User requests password reset (forgot password)
 * 2. Generate reset token (expires in 1 hour)
 * 3. Send email with reset link
 * 4. User clicks link and enters new password
 * 5. Validate token (not expired, not used)
 * 6. Update password (hash with BCrypt)
 * 7. Mark token as used
 * 8. User can login with new password
 * 
 * Security Measures:
 * - Email enumeration prevention (always return success)
 * - Token expiration (1 hour)
 * - Single-use tokens (marked as used)
 * - Password complexity validation (@ValidPassword)
 * - BCrypt password hashing
 * - Rate limiting (should be implemented at API gateway)
 * 
 * @see com.skilltrack.common.entity.PasswordResetToken
 * @see EmailService Email notification service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Initiate password reset process (forgot password).
     * 
     * Forgot Password Flow:
     * 1. Normalize email (lowercase, trim)
     * 2. Find user by email
     * 3. If user not found, log but return success (email enumeration prevention)
     * 4. Delete old unused tokens for user (cleanup)
     * 5. Generate new reset token (expires in 1 hour)
     * 6. Send reset email with token link (async)
     * 7. Return success (always, even if user doesn't exist)
     * 
     * Business Rules:
     * - Can request reset for any email (even non-existent)
     * - Always returns success message
     * - Old tokens are deleted when new token generated
     * - Token expires in 1 hour
     * - Email contains link: {frontendUrl}/reset-password?token={token}
     * 
     * Email Enumeration Prevention:
     * - Returns success even if email doesn't exist
     * - Generic message: "If your email is registered, you will receive a reset link"
     * - Prevents attackers from discovering registered emails
     * 
     * Rate Limiting:
     * - Should be implemented at API gateway/controller level
     * - Prevent spam (e.g., max 3 requests per hour per IP)
     * 
     * @param email User email
     */
    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);

        // Normalize email
        String normalizedEmail = email.toLowerCase().trim();

        // Find user (silently fail if not found - security)
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", normalizedEmail);
            // Return success to prevent email enumeration
            return;
        }

        // Delete old unused tokens for this user (cleanup)
        tokenRepository.deleteByUserAndUsed(user, false);
        log.info("Deleted old password reset tokens for user: {}", user.getEmail());

        // Generate new reset token (expires in 1 hour)
        PasswordResetToken resetToken = PasswordResetToken.createToken(user, 1);
        tokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getEmail());

        // Send reset email (async - doesn't block response)
        emailService.sendPasswordResetEmail(user, resetToken.getTokenValue());
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    /**
     * Reset user password with valid reset token.
     * 
     * Reset Password Flow:
     * 1. Find token by value
     * 2. Validate token exists
     * 3. Validate token not expired (token.isValid())
     * 4. Validate token not already used
     * 5. Get associated user
     * 6. Hash new password with BCrypt
     * 7. Update user password
     * 8. Mark token as used (prevent reuse)
     * 9. Save user and token
     * 10. User can login with new password
     * 
     * Token Validation:
     * - Token must exist in database
     * - Token must not be expired (checked via token.isValid())
     * - Token must not be already used (used = false)
     * 
     * Password Update:
     * - New password validated by @ValidPassword annotation
     * - Password hashed with BCrypt (strength 10)
     * - Old password NOT required (user forgot it)
     * - Failed login attempts reset (fresh start)
     * - Account unlocked if was locked
     * 
     * Error Cases:
     * - Token not found → ValidationException (400)
     * - Token expired → ValidationException (400) with "request new reset" message
     * - Token already used → ValidationException (400)
     * 
     * Security:
     * - Token is single-use (marked as used)
     * - Password complexity enforced (@ValidPassword)
     * - BCrypt hashing (one-way, salted)
     * - Transaction ensures atomicity
     * 
     * @param token Reset token from email link (UUID string)
     * @param newPassword New password (already validated by @ValidPassword)
     * @throws ValidationException if token invalid, expired, or used
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token: {}", token);

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByTokenValue(token)
                .orElseThrow(() -> new ValidationException("Invalid password reset token"));

        // Validate token not expired
        if (!resetToken.isValid()) {
            log.warn("Password reset token expired for user: {}", resetToken.getUser().getEmail());
            throw new ValidationException("Password reset token has expired. Please request a new password reset.");
        }

        // Validate token not already used
        if (resetToken.isUsed()) {
            log.warn("Password reset token already used for user: {}", resetToken.getUser().getEmail());
            throw new ValidationException("Password reset token has already been used");
        }

        // Get user
        User user = resetToken.getUser();

        // Update password (hash with BCrypt)
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(hashedPassword);

        // Reset failed login attempts and unlock account (fresh start)
        user.resetFailedLoginAttempts();
        user.unlockAccount();

        // Mark token as used
        resetToken.markAsUsed();

        // Save changes (user and token)
        userRepository.save(user);
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}
