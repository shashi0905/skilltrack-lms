package com.skilltrack.api.dto.request;

import com.skilltrack.api.validation.ValidPassword;
import com.skilltrack.common.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request.
 * 
 * Validation Rules:
 * - Email: Required, valid format, unique (checked in service layer)
 * - Password: Required, min 8 chars, must meet complexity requirements
 * - Full Name: Required, 2-100 characters
 * - Country: Required for compliance and localization
 * - Organization: Optional, useful for B2B features
 * - Role: Required, defaults to ROLE_STUDENT
 * 
 * Security Considerations:
 * - Password is never logged or exposed in responses
 * - Email is case-insensitive for uniqueness check
 * - Role cannot be ADMIN (must be assigned by existing admin)
 * 
 * @see ValidPassword Custom password validation annotation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword  // Custom annotation for password policy
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;

    @Size(max = 255, message = "Organization name must not exceed 255 characters")
    private String organization;

    @NotNull(message = "Role is required")
    private RoleName roleName;

    /**
     * Normalize email to lowercase for case-insensitive comparison.
     * Called by service layer before processing.
     */
    public void normalizeEmail() {
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
    }

    /**
     * Validate role selection.
     * Users cannot register as ADMIN through public registration.
     * 
     * @return true if role is valid for self-registration
     */
    public boolean isValidRoleForRegistration() {
        return roleName == RoleName.ROLE_STUDENT || roleName == RoleName.ROLE_INSTRUCTOR;
    }

    @Override
    public String toString() {
        // Never log password
        return "RegisterRequest{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", country='" + country + '\'' +
                ", organization='" + organization + '\'' +
                ", roleName=" + roleName +
                '}';
    }
}
