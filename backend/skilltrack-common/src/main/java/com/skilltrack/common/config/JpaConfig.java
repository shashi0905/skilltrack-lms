package com.skilltrack.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for common module.
 * 
 * Enables:
 * - JPA Auditing (@CreatedDate, @LastModifiedDate in BaseEntity)
 * 
 * This allows automatic population of created_at and updated_at timestamps.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
