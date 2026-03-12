package com.skilltrack.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic DTO for simple success message responses.
 * 
 * Used for operations that don't return domain data:
 * - Email verification success
 * - Password reset email sent
 * - Logout success
 * - Account deletion
 * 
 * Provides consistent structure for user feedback messages.
 * 
 * Example Response:
 * {
 *   "message": "Email verified successfully",
 *   "success": true,
 *   "timestamp": "2026-02-23T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /**
     * Human-readable success or info message.
     */
    private String message;

    /**
     * Success flag (typically true for 2xx responses).
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Timestamp of response.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Convenience constructor for success message only.
     * 
     * @param message Success message
     */
    public MessageResponse(String message) {
        this.message = message;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
}
