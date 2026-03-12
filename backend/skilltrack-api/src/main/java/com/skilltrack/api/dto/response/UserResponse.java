package com.skilltrack.api.dto.response;

import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.InstructorVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for user data in API responses.
 * 
 * This DTO is returned for:
 * - User registration (201 Created)
 * - Get current user (200 OK)
 * - Get user profile (200 OK)
 * - Update profile (200 OK)
 * 
 * Security Considerations:
 * - Password hash is NEVER included
 * - Email verification token is NEVER included
 * - Sensitive fields (failedLoginAttempts, accountLocked) excluded
 * 
 * Business Rules:
 * - emailVerificationStatus shows if user can access full features
 * - instructorVerificationStatus shows if instructor can create courses
 * - Roles determine authorization (checked by Spring Security)
 * 
 * @see com.skilltrack.common.entity.User Source entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * User's unique identifier (UUID).
     */
    private UUID id;

    /**
     * User's email address (unique, case-insensitive).
     */
    private String email;

    /**
     * User's full name (display name).
     */
    private String fullName;

    /**
     * User's country (for localization and compliance).
     */
    private String country;

    /**
     * User's organization (optional).
     */
    private String organization;

    /**
     * Set of role names assigned to the user.
     * Example: ["ROLE_STUDENT"], ["ROLE_INSTRUCTOR", "ROLE_STUDENT"]
     */
    private Set<RoleName> roles;

    /**
     * Email verification status.
     * - PENDING: User registered but hasn't verified email (limited access)
     * - VERIFIED: User clicked verification link (full access)
     */
    private EmailVerificationStatus emailVerificationStatus;

    /**
     * Instructor verification status (only relevant if user has ROLE_INSTRUCTOR).
     * - PENDING: Admin review required before creating courses
     * - APPROVED: Can create and publish courses
     * - REJECTED: Cannot create courses (can request re-review)
     */
    private InstructorVerificationStatus instructorVerificationStatus;

    /**
     * GitHub user ID (if account linked via OAuth).
     * Null if user registered with email/password only.
     */
    private String githubId;

    /**
     * Account creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp (profile, password, etc.).
     */
    private LocalDateTime updatedAt;

    /**
     * Check if user has a specific role.
     * 
     * @param roleName Role to check
     * @return true if user has the role
     */
    public boolean hasRole(RoleName roleName) {
        return roles != null && roles.contains(roleName);
    }

    /**
     * Check if user's email is verified.
     * 
     * @return true if email verification status is VERIFIED
     */
    public boolean isEmailVerified() {
        return emailVerificationStatus == EmailVerificationStatus.VERIFIED;
    }

    /**
     * Check if user is an approved instructor.
     * 
     * @return true if user has ROLE_INSTRUCTOR and is approved
     */
    public boolean isApprovedInstructor() {
        return hasRole(RoleName.ROLE_INSTRUCTOR) 
            && instructorVerificationStatus == InstructorVerificationStatus.VERIFIED;
    }
}
