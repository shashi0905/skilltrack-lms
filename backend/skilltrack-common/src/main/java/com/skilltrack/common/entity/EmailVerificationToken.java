package com.skilltrack.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Email verification token for new user registrations.
 * 
 * Flow:
 * 1. User registers → token generated and emailed
 * 2. User clicks link → token validated and marked as used
 * 3. Token expires after configured time (e.g., 1 hour)
 * 
 * Security considerations:
 * - Tokens are single-use (marked as used after verification)
 * - Tokens expire after a short time window
 * - Token value is a secure random UUID
 * - Old tokens are not deleted (audit trail) but marked as used/expired
 * 
 * Follows coding standards:
 * - Immutable token value
 * - Business methods for validation
 * - Clear expiry logic
 */
@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "token_value"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken extends BaseEntity {

    /**
     * Secure random token value (UUID).
     * Sent in verification email link.
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
     * Called after successful email verification.
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new verification token.
     * 
     * @param user User to create token for
     * @param expiryHours Hours until token expires
     * @return New token instance
     */
    public static EmailVerificationToken createToken(User user, int expiryHours) {
        return EmailVerificationToken.builder()
                .tokenValue(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(expiryHours))
                .used(false)
                .build();
    }

    @Override
    public String toString() {
        return "EmailVerificationToken{" +
                "id='" + getId() + '\'' +
                ", userId='" + (user != null ? user.getId() : null) + '\'' +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
