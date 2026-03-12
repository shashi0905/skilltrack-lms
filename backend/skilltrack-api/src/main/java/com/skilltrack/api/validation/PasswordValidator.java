package com.skilltrack.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for {@link ValidPassword} annotation.
 * 
 * Implements OWASP password complexity guidelines:
 * - Minimum length: 8 characters
 * - Character diversity: uppercase, lowercase, digit, special character
 * 
 * Validation Logic:
 * 1. Check null or blank (fail)
 * 2. Check minimum length (8 chars)
 * 3. Check uppercase letter present
 * 4. Check lowercase letter present
 * 5. Check digit present
 * 6. Check special character present
 * 
 * Performance:
 * - Uses precompiled regex patterns (faster)
 * - Fails fast on first violation
 * 
 * Security:
 * - Does not log password value
 * - Does not expose password in error messages
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Precompiled patterns for performance
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()\\-_+=]");

    private static final int MIN_LENGTH = 8;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }

    /**
     * Validates password against complexity requirements.
     * 
     * @param password Password to validate
     * @param context Validation context (for custom error messages)
     * @return true if password meets all requirements, false otherwise
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null or blank check (Bean Validation handles @NotBlank separately)
        if (password == null || password.isBlank()) {
            return false;
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            buildCustomMessage(context, "Password must be at least " + MIN_LENGTH + " characters long");
            return false;
        }

        // Check uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            buildCustomMessage(context, "Password must contain at least one uppercase letter (A-Z)");
            return false;
        }

        // Check lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            buildCustomMessage(context, "Password must contain at least one lowercase letter (a-z)");
            return false;
        }

        // Check digit
        if (!DIGIT_PATTERN.matcher(password).find()) {
            buildCustomMessage(context, "Password must contain at least one digit (0-9)");
            return false;
        }

        // Check special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            buildCustomMessage(context, "Password must contain at least one special character (!@#$%^&*()-_+=)");
            return false;
        }

        return true;
    }

    /**
     * Build custom error message for specific validation failure.
     * 
     * @param context Validation context
     * @param message Custom error message
     */
    private void buildCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
