package com.skilltrack.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for common module.
 * 
 * Enables:
 * - JPA Auditing (@CreatedDate, @LastModifiedDate in BaseEntity)
 * 
 * This allows automatic population of created_at and updated_at timestamps.
 * Note: Repository scanning is configured in the main application class.
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnMissingBean(name = "jpaAuditingHandler")
public class JpaConfig {
}
