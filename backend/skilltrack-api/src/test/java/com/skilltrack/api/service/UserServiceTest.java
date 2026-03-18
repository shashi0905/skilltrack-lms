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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private Role studentRole;
    private User savedUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("Test@Example.com")
                .password("SecurePass1!")
                .fullName("Test User")
                .country("US")
                .organization("TestOrg")
                .roleName(RoleName.ROLE_STUDENT)
                .build();

        studentRole = new Role(RoleName.ROLE_STUDENT);

        savedUser = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();

        userResponse = UserResponse.builder()
                .email("test@example.com")
                .fullName("Test User")
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .roles(Set.of(RoleName.ROLE_STUDENT))
                .build();
    }

    // ==================== registerUser ====================

    @Test
    void registerUser_success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("SecurePass1!")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        UserResponse result = userService.registerUser(registerRequest);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq(savedUser), anyString());
    }

    @Test
    void registerUser_duplicateEmail_throwsValidationException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void registerUser_adminRole_throwsValidationException() {
        registerRequest.setRoleName(RoleName.ROLE_ADMIN);

        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid role selection");
    }

    @Test
    void registerUser_instructorRole_setsUnverifiedStatus() {
        registerRequest.setRoleName(RoleName.ROLE_INSTRUCTOR);
        Role instructorRole = new Role(RoleName.ROLE_INSTRUCTOR);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ROLE_INSTRUCTOR)).thenReturn(Optional.of(instructorRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        userService.registerUser(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getInstructorVerificationStatus())
                .isEqualTo(InstructorVerificationStatus.UNVERIFIED);
    }

    @Test
    void registerUser_normalizesEmailToLowercase() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(savedUser)).thenReturn(userResponse);

        userService.registerUser(registerRequest);

        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void registerUser_roleNotFound_throwsValidationException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.ROLE_STUDENT)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        assertThatThrownBy(() -> userService.registerUser(registerRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Role not found");
    }

    // ==================== verifyEmail ====================

    @Test
    void verifyEmail_validToken_verifiesUser() {
        User unverifiedUser = User.builder()
                .email("test@example.com")
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .build();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenValue("valid-token")
                .user(unverifiedUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.save(any(User.class))).thenReturn(unverifiedUser);
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(token);
        when(userMapper.toResponse(unverifiedUser)).thenReturn(userResponse);

        userService.verifyEmail("valid-token");

        assertThat(unverifiedUser.getEmailVerificationStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(unverifiedUser);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmail_invalidToken_throwsValidationException() {
        when(tokenRepository.findByTokenValue("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail("bad-token"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid verification token");
    }

    @Test
    void verifyEmail_expiredToken_throwsValidationException() {
        User user = User.builder().email("test@example.com").build();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenValue("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.verifyEmail("expired-token"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyEmail_usedToken_throwsValidationException() {
        User user = User.builder().email("test@example.com").build();
        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenValue("used-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(tokenRepository.findByTokenValue("used-token")).thenReturn(Optional.of(token));

        // isValid() returns false for used tokens, service throws "expired" message
        assertThatThrownBy(() -> userService.verifyEmail("used-token"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void verifyEmail_alreadyVerifiedUser_returnsWithoutReVerifying() {
        User verifiedUser = User.builder()
                .email("test@example.com")
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
                .build();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .tokenValue("token")
                .user(verifiedUser)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("token")).thenReturn(Optional.of(token));
        when(userMapper.toResponse(verifiedUser)).thenReturn(userResponse);

        userService.verifyEmail("token");

        verify(userRepository, never()).save(any());
    }

    // ==================== resendVerificationEmail ====================

    @Test
    void resendVerification_validUser_sendsNewEmail() {
        User user = User.builder()
                .email("test@example.com")
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        userService.resendVerificationEmail("test@example.com");

        verify(tokenRepository).deleteByUserAndUsed(user, false);
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq(user), anyString());
    }

    @Test
    void resendVerification_nonExistentEmail_silentlyReturns() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        userService.resendVerificationEmail("unknown@example.com");

        verify(emailService, never()).sendVerificationEmail(any(), anyString());
    }

    @Test
    void resendVerification_alreadyVerified_throwsValidationException() {
        User verifiedUser = User.builder()
                .email("test@example.com")
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(verifiedUser));

        assertThatThrownBy(() -> userService.resendVerificationEmail("test@example.com"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already verified");
    }
}
