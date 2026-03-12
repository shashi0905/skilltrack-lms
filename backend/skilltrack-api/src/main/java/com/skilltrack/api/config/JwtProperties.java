package com.skilltrack.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration properties from application.yml.
 * 
 * Binds properties prefixed with "jwt" to this Java class:
 * - jwt.secret → secret field
 * - jwt.expiration → expiration field
 * - jwt.refresh-expiration → refreshExpiration field
 * 
 * Learning points:
 * - @ConfigurationProperties for type-safe configuration
 * - Properties are externalized (can change without recompiling)
 * - @Validated can be added for validation constraints
 * - Lombok @Getter/@Setter for concise code
 * 
 * Security considerations:
 * - Secret should be at least 256 bits (32 characters) for HS256
 * - Never commit real secrets to version control
 * - Use environment variables in production
 * - Rotate secrets periodically
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * Must be kept confidential and never exposed.
     * 
     * Production: Use environment variable
     * Example: ${JWT_SECRET:fallback-secret-for-dev}
     */
    private String secret;

    /**
     * Token expiration time in milliseconds.
     * Default: 86400000 = 24 hours
     * 
     * Shorter expiration = more secure but less convenient
     * Longer expiration = more convenient but higher risk if token stolen
     */
    private long expiration;

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 604800000 = 7 days
     * 
     * Refresh tokens are used to obtain new access tokens
     * without requiring user to log in again.
     */
    private long refreshExpiration;
}
