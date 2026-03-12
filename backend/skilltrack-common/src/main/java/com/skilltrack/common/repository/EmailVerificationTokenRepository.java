package com.skilltrack.common.repository;

import com.skilltrack.common.entity.EmailVerificationToken;
import com.skilltrack.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for EmailVerificationToken entity.
 * 
 * Manages email verification tokens for user registration flow.
 * 
 * Learning points:
 * - Token lookup by value (from email link)
 * - Cleanup of expired/used tokens (data maintenance)
 * - @Modifying for custom update/delete queries
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, String> {

    /**
     * Find token by token value.
     * Used when user clicks verification link in email.
     * 
     * @param tokenValue Token UUID from email link
     * @return Optional containing token if found
     */
    Optional<EmailVerificationToken> findByTokenValue(String tokenValue);

    /**
     * Find all tokens for a specific user.
     * Useful for debugging or showing user their pending verifications.
     * 
     * @param user User entity
     * @return List of tokens for this user
     */
    Optional<EmailVerificationToken> findByUser(User user);

    /**
     * Delete expired tokens that were created before a certain date.
     * Cleanup job to prevent token table from growing indefinitely.
     * 
     * @Modifying indicates this query modifies data (not a SELECT)
     * Must be used with @Transactional in calling service
     * 
     * @param cutoffDate Delete tokens older than this date
     * @return Number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete all tokens for a specific user.
     * Used when user is deleted or during cleanup operations.
     * 
     * @param user User entity
     */
    void deleteByUser(User user);

    /**
     * Delete tokens by user and used status.
     * Used when resending verification email to clean up old unused tokens.
     * 
     * @param user User entity
     * @param used Used status (false = unused tokens)
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user = :user AND t.used = :used")
    void deleteByUserAndUsed(@Param("user") User user, @Param("used") boolean used);
}
