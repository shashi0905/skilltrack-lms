package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.RegisterRequest;
import com.skilltrack.api.dto.response.UserResponse;
import com.skilltrack.api.mapper.UserMapper;
import com.skilltrack.common.entity.EmailVerificationToken;
import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.InstructorVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.exception.ValidationException;
import com.skilltrack.common.repository.EmailVerificationTokenRepository;
import com.skilltrack.common.repository.RoleRepository;
import com.skilltrack.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service layer for user management operations.
 * 
 * Responsibilities:
 * - User registration with email/password
 * - Email verification token generation
 * - User profile management
 * - Role assignment
 * 
 * Business Rules:
 * - Email must be unique (case-insensitive)
 * - Password must meet complexity requirements (validated by @ValidPassword)
 * - Default role is ROLE_STUDENT
 * - ROLE_INSTRUCTOR requires admin approval (instructorVerificationStatus = PENDING)
 * - ROLE_ADMIN cannot be self-assigned (must be granted by existing admin)
 * - Email verification required for full access
 * 
 * Transaction Management:
 * - All write operations are @Transactional
 * - Ensures atomicity (user creation + token creation + email sending)
 * - Rollback on exception (except email sending errors)
 * 
 * @see User Entity class
 * @see UserRepository Data access layer
 * @see EmailService Email notification service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;

    /**
     * Register a new user with email and password.
     * 
     * Registration Flow:
     * 1. Validate email uniqueness
     * 2. Validate role selection (cannot be ADMIN)
     * 3. Normalize email (lowercase, trim)
     * 4. Hash password with BCrypt
     * 5. Create User entity with default status
     * 6. Assign role(s)
     * 7. Save user to database
     * 8. Generate email verification token
     * 9. Send verification email (async)
     * 10. Return UserResponse DTO
     * 
     * Default Status:
     * - emailVerificationStatus: PENDING (must verify email)
     * - instructorVerificationStatus: PENDING if ROLE_INSTRUCTOR, null otherwise
     * - accountLocked: false
     * - failedLoginAttempts: 0
     * 
     * Error Handling:
     * - Email already exists → ValidationException (400)
     * - Invalid role selection → ValidationException (400)
     * - Database error → RuntimeException (500)
     * - Email sending error → Logged but not thrown (user can resend)
     * 
     * @param request Registration request with email, password, name, etc.
     * @return UserResponse DTO with created user data
     * @throws ValidationException if email exists or role invalid
     */
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Normalize email (lowercase, trim)
        request.normalizeEmail();

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already registered: {}", request.getEmail());
            throw new ValidationException("Email is already registered");
        }

        // Validate role selection (cannot self-assign ADMIN)
        if (!request.isValidRoleForRegistration()) {
            log.warn("Registration failed: Invalid role selection: {}", request.getRoleName());
            throw new ValidationException("Invalid role selection. Only ROLE_STUDENT and ROLE_INSTRUCTOR are allowed for self-registration");
        }

        // Build User entity
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Hash password
                .fullName(request.getFullName())
                .country(request.getCountry())
                .organization(request.getOrganization())
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .instructorVerificationStatus(
                    request.getRoleName() == RoleName.ROLE_INSTRUCTOR 
                        ? InstructorVerificationStatus.UNVERIFIED 
                        : null
                )
                .accountLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>())
                .build();

        // Assign role
        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new ValidationException("Role not found: " + request.getRoleName()));
        user.getRoles().add(role);

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        // Generate email verification token (expires in 1 hour)
        EmailVerificationToken token = EmailVerificationToken.createToken(savedUser, 1);
        tokenRepository.save(token);
        log.info("Email verification token generated for user: {}", savedUser.getEmail());

        // Send verification email (async - doesn't block response)
        emailService.sendVerificationEmail(savedUser, token.getTokenValue());

        // Convert to DTO and return
        return userMapper.toResponse(savedUser);
    }

    /**
     * Get user by ID.
     * 
     * @param id User UUID
     * @return UserResponse DTO
     * @throws com.skilltrack.common.exception.ResourceNotFoundException if user not found
     */
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.skilltrack.common.exception.ResourceNotFoundException(
                    "User", id));
        return userMapper.toResponse(user);
    }

    /**
     * Get user by email (with roles eagerly loaded).
     * 
     * Used by:
     * - Login authentication
     * - Password reset
     * - Email verification
     * 
     * @param email User email (case-insensitive)
     * @return UserResponse DTO
     * @throws com.skilltrack.common.exception.ResourceNotFoundException if user not found
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email.toLowerCase())
                .orElseThrow(() -> new com.skilltrack.common.exception.ResourceNotFoundException(
                    "User not found with email: " + email));
        return userMapper.toResponse(user);
    }

    /**
     * Verify user's email with verification token.
     * 
     * Verification Flow:
     * 1. Find token by value
     * 2. Validate token (not expired, not used, user not already verified)
     * 3. Mark user as verified (emailVerificationStatus = VERIFIED)
     * 4. Mark token as used (prevent reuse)
     * 5. Save changes to database
     * 6. Return UserResponse with updated status
     * 
     * Token Validation:
     * - Token must exist in database
     * - Token must not be expired (checked via token.isValid())
     * - Token must not be already used (used = false)
     * - User must not be already verified (idempotent operation allowed)
     * 
     * Error Cases:
     * - Token not found → ValidationException (400)
     * - Token expired → ValidationException (400) with "resend" suggestion
     * - Token already used → ValidationException (400)
     * 
     * Security:
     * - Token is single-use (marked as used)
     * - Token expires in 1 hour
     * - Transaction ensures atomicity
     * 
     * @param token Verification token from email link (UUID string)
     * @return UserResponse DTO with emailVerificationStatus = VERIFIED
     * @throws ValidationException if token invalid, expired, or used
     */
    @Transactional
    public UserResponse verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);

        // Find token
        EmailVerificationToken verificationToken = tokenRepository.findByTokenValue(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        // Validate token not expired
        if (!verificationToken.isValid()) {
            log.warn("Verification token expired for user: {}", verificationToken.getUser().getEmail());
            throw new ValidationException("Verification token has expired. Please request a new verification email.");
        }

        // Validate token not already used
        if (verificationToken.isUsed()) {
            log.warn("Verification token already used for user: {}", verificationToken.getUser().getEmail());
            throw new ValidationException("Verification token has already been used");
        }

        // Get user
        User user = verificationToken.getUser();

        // Check if already verified (idempotent - allow but don't re-verify)
        if (user.isEmailVerified()) {
            log.info("User email already verified: {}", user.getEmail());
            return userMapper.toResponse(user);
        }

        // Mark user as verified
        user.verifyEmail();
        
        // Mark token as used
        verificationToken.markAsUsed();

        // Save changes (user and token)
        userRepository.save(user);
        tokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
        return userMapper.toResponse(user);
    }

    /**
     * Resend email verification link to user.
     * 
     * Use Cases:
     * - User didn't receive original email
     * - Original token expired
     * - User deleted email
     * 
     * Resend Flow:
     * 1. Find user by email
     * 2. Validate user exists and email NOT already verified
     * 3. Invalidate old tokens (delete unused tokens for this user)
     * 4. Generate new token (fresh 1-hour expiration)
     * 5. Send verification email (async)
     * 6. Return success message
     * 
     * Business Rules:
     * - Can only resend if user exists
     * - Can only resend if email NOT already verified
     * - Old tokens are deleted (security best practice)
     * - Always return success (even if email doesn't exist) to prevent email enumeration
     * 
     * Security:
     * - Email enumeration prevention: Always return success
     * - Rate limiting should be applied at API gateway/controller level
     * - Old tokens invalidated to prevent token accumulation
     * 
     * Error Handling:
     * - User not found → Log but return success (security)
     * - Email already verified → ValidationException (400)
     * - Email sending fails → Log error but return success (user can retry)
     * 
     * @param email User email
     * @throws ValidationException if email already verified
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        log.info("Resend verification email request for: {}", email);

        // Normalize email
        String normalizedEmail = email.toLowerCase().trim();

        // Find user (silently fail if not found - security)
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        
        if (user == null) {
            log.warn("Resend verification requested for non-existent email: {}", normalizedEmail);
            // Return success to prevent email enumeration
            return;
        }

        // Check if already verified
        if (user.isEmailVerified()) {
            log.warn("Resend verification requested for already verified email: {}", normalizedEmail);
            throw new ValidationException("Email is already verified");
        }

        // Delete old unused tokens for this user (cleanup)
        tokenRepository.deleteByUserAndUsed(user, false);
        log.info("Deleted old verification tokens for user: {}", user.getEmail());

        // Generate new token
        EmailVerificationToken newToken = EmailVerificationToken.createToken(user, 1);
        tokenRepository.save(newToken);
        log.info("New verification token generated for user: {}", user.getEmail());

        // Send verification email (async)
        emailService.sendVerificationEmail(user, newToken.getTokenValue());
        log.info("Verification email resent to: {}", user.getEmail());
    }
}
