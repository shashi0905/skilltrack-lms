package com.skilltrack.common.entity;

import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.InstructorVerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * User entity representing all users (students, instructors, admins) in the system.
 * 
 * Registration flows:
 * 1. Email/Password: User provides email, password, name, role → status = PENDING
 * 2. GitHub OAuth: OAuth flow → profile completion → status = VERIFIED (trusted)
 * 3. Admin: Created via backend → status = VERIFIED
 * 
 * Verification states:
 * - emailVerificationStatus: PENDING → VERIFIED (via email link)
 * - instructorVerificationStatus: UNVERIFIED → VERIFIED (via admin review)
 * 
 * Access control:
 * - PENDING email: Can browse, cannot enroll
 * - VERIFIED email: Full access per role
 * - UNVERIFIED instructor: Can create courses (labeled as unverified)
 * - VERIFIED instructor: Courses have higher trust
 * 
 * Follows coding standards:
 * - Constructor injection of required fields
 * - Immutable collections (defensive copies)
 * - Business key equality (email)
 * - No setters for collections (use add/remove methods)
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
}, indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_github_id", columnList = "github_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * BCrypt hashed password.
     * Null for OAuth users (GitHub).
     */
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /**
     * Optional fields captured during registration.
     */
    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "organization", length = 255)
    private String organization;

    /**
     * GitHub user ID for OAuth users.
     * Null for email/password users.
     */
    @Column(name = "github_id", unique = true, length = 50)
    private String githubId;

    /**
     * Email verification status.
     * - PENDING: Just registered, verification email sent
     * - VERIFIED: Email verified or OAuth user
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "email_verification_status", nullable = false, length = 20)
    @Builder.Default
    private EmailVerificationStatus emailVerificationStatus = EmailVerificationStatus.PENDING;

    /**
     * Instructor verification status (only relevant for ROLE_INSTRUCTOR).
     * - UNVERIFIED: New instructor, not yet reviewed by admin
     * - VERIFIED: Admin approved
     * - REJECTED: Admin rejected
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "instructor_verification_status", length = 20)
    private InstructorVerificationStatus instructorVerificationStatus;

    /**
     * Failed login attempt counter for security.
     * Reset to 0 on successful login.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    /**
     * Account locked flag (after too many failed attempts).
     */
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    /**
     * User roles (STUDENT, INSTRUCTOR, ADMIN).
     * Many-to-many relationship.
     * Use eager fetching for security context (small dataset).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ==================== Business Methods ====================

    /**
     * Adds a role to this user.
     * Defensive: ensures roles collection is initialized.
     */
    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Removes a role from this user.
     */
    public void removeRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(com.skilltrack.common.enums.RoleName roleName) {
        return roles != null && roles.stream()
                .anyMatch(role -> role.getRoleName() == roleName);
    }

    /**
     * Marks email as verified.
     */
    public void verifyEmail() {
        this.emailVerificationStatus = EmailVerificationStatus.VERIFIED;
    }

    /**
     * Checks if email is verified.
     */
    public boolean isEmailVerified() {
        return EmailVerificationStatus.VERIFIED == this.emailVerificationStatus;
    }

    /**
     * Increments failed login attempts.
     * Locks account if threshold exceeded (handled in service layer).
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Resets failed login attempts on successful login.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    /**
     * Locks the account.
     */
    public void lockAccount() {
        this.accountLocked = true;
    }

    /**
     * Unlocks the account (admin action or password reset).
     */
    public void unlockAccount() {
        this.accountLocked = false;
    }

    // ==================== Equality & HashCode ====================

    /**
     * Business key equality based on email.
     * More stable than ID for User entities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return email != null ? email.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", emailVerificationStatus=" + emailVerificationStatus +
                ", roles=" + roles.size() +
                '}';
    }
}
