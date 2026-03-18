package com.skilltrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltrack.api.dto.request.*;
import com.skilltrack.api.dto.response.LoginResponse;
import com.skilltrack.api.dto.response.UserResponse;
import com.skilltrack.api.exception.GlobalExceptionHandler;
import com.skilltrack.api.service.AuthService;
import com.skilltrack.api.service.PasswordResetService;
import com.skilltrack.api.service.UserService;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.exception.AuthenticationException;
import com.skilltrack.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private PasswordResetService passwordResetService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com").password("SecurePass1!")
                .fullName("Test User").country("US").roleName(RoleName.ROLE_STUDENT).build();

        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID()).email("test@example.com")
                .emailVerificationStatus(EmailVerificationStatus.PENDING)
                .roles(Set.of(RoleName.ROLE_STUDENT)).build();

        when(userService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com").password("SecurePass1!")
                .fullName("Test User").country("US").roleName(RoleName.ROLE_STUDENT).build();

        when(userService.registerUser(any())).thenThrow(new ValidationException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = LoginRequest.builder().email("test@example.com").password("SecurePass1!").build();

        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token").refreshToken("refresh-token")
                .tokenType("Bearer").expiresIn(86400L)
                .user(LoginResponse.UserInfo.builder().id("id").email("test@example.com")
                        .fullName("Test").roles(Set.of(RoleName.ROLE_STUDENT)).build())
                .build();

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequest request = LoginRequest.builder().email("test@example.com").password("wrong").build();
        when(authService.login(any())).thenThrow(new AuthenticationException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyEmail_returns200() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("valid-token");

        when(userService.verifyEmail("valid-token")).thenReturn(
                UserResponse.builder().email("test@example.com")
                        .emailVerificationStatus(EmailVerificationStatus.VERIFIED).build());

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resendVerification_returns200() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void refreshToken_returns200() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken("refresh-token").build();
        LoginResponse response = LoginResponse.builder()
                .accessToken("new-token").refreshToken("refresh-token").tokenType("Bearer").expiresIn(86400L).build();

        when(authService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-token"));
    }

    @Test
    void logout_returns200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void forgotPassword_returns200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_returns200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("NewSecurePass1!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
