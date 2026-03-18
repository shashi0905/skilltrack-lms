package com.skilltrack.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    private PasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    }

    @Test
    void validPassword_returnsTrue() {
        assertThat(validator.isValid("SecurePass1!", context)).isTrue();
    }

    @Test
    void nullPassword_returnsFalse() {
        assertThat(validator.isValid(null, context)).isFalse();
    }

    @Test
    void blankPassword_returnsFalse() {
        assertThat(validator.isValid("   ", context)).isFalse();
    }

    @Test
    void shortPassword_returnsFalse() {
        assertThat(validator.isValid("Ab1!xyz", context)).isFalse();
    }

    @Test
    void noUppercase_returnsFalse() {
        assertThat(validator.isValid("securepass1!", context)).isFalse();
    }

    @Test
    void noLowercase_returnsFalse() {
        assertThat(validator.isValid("SECUREPASS1!", context)).isFalse();
    }

    @Test
    void noDigit_returnsFalse() {
        assertThat(validator.isValid("SecurePass!", context)).isFalse();
    }

    @Test
    void noSpecialChar_returnsFalse() {
        assertThat(validator.isValid("SecurePass1", context)).isFalse();
    }

    @Test
    void exactlyMinLength_returnsTrue() {
        assertThat(validator.isValid("Abcde1!x", context)).isTrue();
    }
}
