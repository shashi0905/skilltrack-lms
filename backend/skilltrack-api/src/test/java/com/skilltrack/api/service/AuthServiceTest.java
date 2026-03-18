package com.skilltrack.api.service;

import com.skilltrack.api.dto.request.LoginRequest;
import com.skilltrack.api.dto.request.RefreshTokenRequest;
import com.skilltrack.api.dto.response.LoginResponse;
import com.skilltrack.api.security.JwtTokenProvider;
import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.exception.AuthenticationException;
import com.skilltrack.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleName.ROLE_STUDENT);

        user = User.builder()
                .email("test@example.com")
                .fullName("Test User")
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        user.setId(UUID.randomUUID().toString());

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("SecurePass1!")
                .build();
    }

    // ==================== login ====================

    @Test
    void login_success_returnsTokens() {
        Authentication auth = mock(Authentication.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(auth)).thenReturn("refresh-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void login_success_resetsFailedAttempts() {
        user.setFailedLoginAttempts(3);
        Authentication auth = mock(Authentication.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(auth)).thenReturn("refresh");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        authService.login(loginRequest);

        assertThat(user.getFailedLoginAttempts()).isZero();
        verify(userRepository).save(user);
    }

    @Test
    void login_userNotFound_throwsAuthenticationException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_accountLocked_throwsAuthenticationException() {
        user.lockAccount();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void login_badCredentials_incrementsFailedAttempts() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository, times(2)).findByEmail("test@example.com"); // once in login, once in handleFailedLogin
    }

    @Test
    void login_fifthFailedAttempt_locksAccount() {
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class);

        assertThat(user.isAccountLocked()).isTrue();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
    }

    @Test
    void login_normalizesEmailToLowercase() {
        loginRequest.setEmail("TEST@EXAMPLE.COM");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("token");
        when(jwtTokenProvider.generateRefreshToken(auth)).thenReturn("refresh");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        authService.login(loginRequest);

        verify(userRepository).findByEmail("test@example.com");
    }

    // ==================== refreshToken ====================

    @Test
    void refreshToken_validToken_returnsNewAccessToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(86400000L);

        LoginResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    void refreshToken_invalidToken_throwsAuthenticationException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void refreshToken_lockedAccount_throwsAuthenticationException() {
        user.lockAccount();
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token");

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("test@example.com");
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class);
    }
}
