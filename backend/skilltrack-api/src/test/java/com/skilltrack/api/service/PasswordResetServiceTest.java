package com.skilltrack.api.service;

import com.skilltrack.common.entity.PasswordResetToken;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.exception.ValidationException;
import com.skilltrack.common.repository.PasswordResetTokenRepository;
import com.skilltrack.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .build();
    }

    // ==================== forgotPassword ====================

    @Test
    void forgotPassword_existingUser_sendsResetEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.forgotPassword("test@example.com");

        verify(tokenRepository).deleteByUserAndUsed(user, false);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(user), anyString());
    }

    @Test
    void forgotPassword_nonExistentEmail_silentlyReturns() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.forgotPassword("unknown@example.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), anyString());
    }

    @Test
    void forgotPassword_normalizesEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.forgotPassword("  TEST@EXAMPLE.COM  ");

        verify(userRepository).findByEmail("test@example.com");
    }

    // ==================== resetPassword ====================

    @Test
    void resetPassword_validToken_updatesPassword() {
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenValue("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewSecurePass1!")).thenReturn("newHashedPassword");

        passwordResetService.resetPassword("valid-token", "NewSecurePass1!");

        assertThat(user.getPasswordHash()).isEqualTo("newHashedPassword");
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.isAccountLocked()).isFalse();
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_invalidToken_throwsValidationException() {
        when(tokenRepository.findByTokenValue("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword("bad-token", "NewPass1!"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid password reset token");
    }

    @Test
    void resetPassword_expiredToken_throwsValidationException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenValue("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword("expired-token", "NewPass1!"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_usedToken_throwsValidationException() {
        PasswordResetToken token = PasswordResetToken.builder()
                .tokenValue("used-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(tokenRepository.findByTokenValue("used-token")).thenReturn(Optional.of(token));

        // isValid() returns false for used tokens, service throws "expired" message
        assertThatThrownBy(() -> passwordResetService.resetPassword("used-token", "NewPass1!"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_unlocksLockedAccount() {
        user.setAccountLocked(true);
        user.setFailedLoginAttempts(5);

        PasswordResetToken token = PasswordResetToken.builder()
                .tokenValue("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(tokenRepository.findByTokenValue("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        passwordResetService.resetPassword("valid-token", "NewSecurePass1!");

        assertThat(user.isAccountLocked()).isFalse();
        assertThat(user.getFailedLoginAttempts()).isZero();
    }
}
