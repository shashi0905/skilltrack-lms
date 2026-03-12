package com.skilltrack.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password reset token for forgot password flow.
 * 
 * Flow:
 * 1. User requests password reset → token generated and emailed
 * 2. User clicks link → token validated
 * 3. User sets new password → token marked as used
 * 4. Token expires after configured time (e.g., 1 hour)
 * 
 * Security considerations:
 * - Tokens are single-use (marked as used after password reset)
 * - Tokens expire after a short time window
 * - Token value is a secure random UUID
 * - Old tokens remain in database (audit trail) but marked as used/expired
 * - Requesting new reset token does not invalidate old ones (they expire naturally)
 * 
 * Follows coding standards:
 * - Immutable token value
 * - Business methods for validation
 * - Clear expiry logic
 * - Similar structure to EmailVerificationToken for consistency
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_reset_token_value", columnList = "token_value"),
    @Index(name = "idx_reset_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {

    /**
     * Secure random token value (UUID).
     * Sent in password reset email link.
     */
    @Column(name = "token_value", nullable = false, unique = true, updatable = false)
    private String tokenValue;

    /**
     * User this token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Token expiry timestamp.
     * Typically 1 hour from creation.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Flag indicating token has been used.
     * Prevents token reuse.
     */
    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    /**
     * Timestamp when token was used.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // ==================== Business Methods ====================

    /**
     * Checks if token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if token is valid (not used and not expired).
     */
    public boolean isValid() {
        return !used && !isExpired();
    }

    /**
     * Marks token as used.
     * Called after successful password reset.
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new password reset token.
     * 
     * @param user User to create token for
     * @param expiryHours Hours until token expires
     * @return New token instance
     */
    public static PasswordResetToken createToken(User user, int expiryHours) {
        return PasswordResetToken.builder()
                .tokenValue(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .used(false)
                .build();
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id='" + getId() + '\'' +
                ", userId='" + (user != null ? user.getId() : null) + '\'' +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
