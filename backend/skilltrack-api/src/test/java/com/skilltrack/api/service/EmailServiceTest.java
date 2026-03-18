package com.skilltrack.api.service;

import com.skilltrack.common.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationEmail_success() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        User user = User.builder().email("test@example.com").fullName("Test User").build();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Verify</html>");

        emailService.sendVerificationEmail(user, "token-123");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendVerificationEmail_failure_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        User user = User.builder().email("test@example.com").fullName("Test User").build();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        emailService.sendVerificationEmail(user, "token-123");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_success() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        User user = User.builder().email("test@example.com").fullName("Test User").build();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Reset</html>");

        emailService.sendPasswordResetEmail(user, "reset-token");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_failure_doesNotThrow() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        User user = User.builder().email("test@example.com").fullName("Test User").build();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP error"));

        emailService.sendPasswordResetEmail(user, "reset-token");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
