package com.skilltrack.api.service;

import com.skilltrack.common.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails (verification, password reset, notifications).
 * 
 * Email Provider:
 * - Development: MailHog (SMTP mock server on localhost:1025)
 * - Production: AWS SES, SendGrid, or other SMTP service
 * 
 * Email Templates:
 * - Uses Thymeleaf for HTML email templates
 * - Located in src/main/resources/templates/email/
 * 
 * Async Processing:
 * - All email sending methods are async (@Async)
 * - Prevents blocking user registration flow
 * - Returns immediately after queueing email
 * 
 * Error Handling:
 * - Logs errors but doesn't throw exceptions
 * - Failed emails don't block user registration
 * - Should implement retry mechanism in production
 * 
 * @see JavaMailSender Spring's email abstraction
 * @see TemplateEngine Thymeleaf template processor
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.from-email:noreply@skilltrack.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Send email verification link to newly registered user.
     * 
     * Email contains:
     * - Welcome message with user's name
     * - Verification link with token
     * - Link expiration notice (1 hour)
     * - Resend verification link option
     * 
     * Template: email/verify-email.html
     * 
     * Flow:
     * 1. User registers → Token generated
     * 2. Email sent with: {frontendUrl}/verify-email?token={token}
     * 3. User clicks link → Frontend calls backend verify API
     * 4. Backend validates token → Marks user as verified
     * 
     * @param user User entity (for name and email)
     * @param token Email verification token (UUID string)
     */
    @Async
    public void sendVerificationEmail(User user, String token) {
        try {
            log.info("Sending verification email to: {}", user.getEmail());

            // Prepare template context (variables for Thymeleaf)
            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            context.setVariable("verificationLink", buildVerificationLink(token));
            context.setVariable("expirationHours", 1);

            // Process HTML template
            String htmlContent = templateEngine.process("email/verify-email", context);

            // Send email
            sendHtmlEmail(
                    user.getEmail(),
                    "Verify Your Email - SkillTrack LMS",
                    htmlContent
            );

            log.info("Verification email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            // Log error but don't throw (email failure shouldn't block registration)
            log.error("Failed to send verification email to: {}. Error: {}", 
                     user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send password reset link to user who forgot password.
     * 
     * Email contains:
     * - Password reset link with token
     * - Link expiration notice (1 hour)
     * - Security warning (ignore if not requested)
     * 
     * Template: email/reset-password.html
     * 
     * Security:
     * - Always send email (even if account doesn't exist) to prevent email enumeration
     * - Token is single-use and expires in 1 hour
     * - Link is: {frontendUrl}/reset-password?token={token}
     * 
     * @param user User entity (for name and email)
     * @param token Password reset token (UUID string)
     */
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            log.info("Sending password reset email to: {}", user.getEmail());

            Context context = new Context();
            context.setVariable("userName", user.getFullName());
            context.setVariable("resetLink", buildPasswordResetLink(token));
            context.setVariable("expirationHours", 1);

            String htmlContent = templateEngine.process("email/reset-password", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "Reset Your Password - SkillTrack LMS",
                    htmlContent
            );

            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", 
                     user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Send HTML email using JavaMailSender.
     * 
     * @param to Recipient email address
     * @param subject Email subject line
     * @param htmlContent HTML content (from Thymeleaf template)
     * @throws MessagingException If email sending fails
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML content

        mailSender.send(message);
    }

    /**
     * Build email verification link.
     * 
     * @param token Verification token (UUID)
     * @return Full URL for frontend verification page
     */
    private String buildVerificationLink(String token) {
        return frontendUrl + "/verify-email?token=" + token;
    }

    /**
     * Build password reset link.
     * 
     * @param token Reset token (UUID)
     * @return Full URL for frontend reset password page
     */
    private String buildPasswordResetLink(String token) {
        return frontendUrl + "/reset-password?token=" + token;
    }
}
