package com.skilltrack.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password encoding.
 * 
 * BCrypt is the industry standard for password hashing because:
 * - It's a one-way hash (cannot be reversed)
 * - Includes automatic salt generation (prevents rainbow table attacks)
 * - Configurable work factor (can increase strength as hardware improves)
 * - Slow by design (prevents brute force attacks)
 * 
 * Learning points:
 * - Never store passwords in plain text
 * - BCrypt includes salt automatically (no need to store salt separately)
 * - Work factor 10-12 is recommended (higher = slower but more secure)
 * - Same password will produce different hashes due to random salt
 * 
 * Example usage:
 * String hashedPassword = passwordEncoder.encode("plainPassword");
 * boolean matches = passwordEncoder.matches("plainPassword", hashedPassword);
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates BCryptPasswordEncoder bean.
     * 
     * Default strength is 10 (2^10 = 1024 rounds).
     * Can be increased for better security: new BCryptPasswordEncoder(12)
     * 
     * @return PasswordEncoder instance for dependency injection
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
