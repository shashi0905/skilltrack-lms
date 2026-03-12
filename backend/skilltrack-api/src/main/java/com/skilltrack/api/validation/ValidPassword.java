package com.skilltrack.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for password policy enforcement.
 * 
 * Password Requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - At least one special character (!@#$%^&*()-_+=)
 * 
 * Usage:
 * <pre>
 * public class RegisterRequest {
 *     {@literal @}ValidPassword
 *     private String password;
 * }
 * </pre>
 * 
 * Security Rationale:
 * - Length requirement prevents brute force attacks
 * - Character diversity increases entropy
 * - Special chars protect against dictionary attacks
 * - Meets OWASP password complexity guidelines
 * 
 * @see PasswordValidator Implementation class
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                             "one lowercase letter, one digit, and one special character (!@#$%^&*()-_+=)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
