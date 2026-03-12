package com.skilltrack.common.repository;

import com.skilltrack.common.entity.PasswordResetToken;
import com.skilltrack.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for PasswordResetToken entity.
 * 
 * Manages password reset tokens for forgot password flow.
 * Nearly identical structure to EmailVerificationTokenRepository.
 * 
 * Learning points:
 * - Consistent repository patterns across similar entities
 * - Token lifecycle management (create, validate, cleanup)
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    /**
     * Find token by token value.
     * Used when user clicks reset link in email.
     * 
     * @param tokenValue Token UUID from email link
     * @return Optional containing token if found
     */
    Optional<PasswordResetToken> findByTokenValue(String tokenValue);

    /**
     * Find tokens for a specific user.
     * Useful for debugging or showing pending reset requests.
     * 
     * @param user User entity
     * @return Optional containing token if found
     */
    Optional<PasswordResetToken> findByUser(User user);

    /**
     * Delete expired tokens.
     * Cleanup job to maintain database hygiene.
     * 
     * @Modifying for DELETE queries
     * @Transactional required in calling service
     * 
     * @param cutoffDate Delete tokens older than this
     * @return Number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete all tokens for a user.
     * Used during user deletion or security-related cleanups.
     * 
     * @param user User entity
     */
    void deleteByUser(User user);

    /**
     * Delete tokens by user and used status.
     * Used when generating new reset token to clean up old unused tokens.
     * 
     * @param user User entity
     * @param used Used status (false = unused tokens)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user = :user AND t.used = :used")
    void deleteByUserAndUsed(@Param("user") User user, @Param("used") boolean used);
}
