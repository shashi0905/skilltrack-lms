package com.skilltrack.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for SkillTrack API module.
 * 
 * Annotations explained:
 * - @SpringBootApplication: Combines @Configuration, @EnableAutoConfiguration, @ComponentScan
 * - @EntityScan: Tells Spring Data JPA where to find entity classes (in common module)
 * - @EnableJpaRepositories: Tells Spring Data JPA where to find repository interfaces (in common module)
 * 
 * Note: @EnableJpaAuditing is configured in JpaConfig.java in common module
 * 
 * Learning points:
 * - Multi-module setup requires explicit package scanning
 * - Entities and repositories are in common module, not api module
 * - Spring Boot auto-configuration handles most setup
 */
@SpringBootApplication(scanBasePackages = {
    "com.skilltrack.api",      // API module components (controllers, services, config)
    "com.skilltrack.common"    // Common module components (repositories, entities, config)
})
@EntityScan(basePackages = "com.skilltrack.common.entity")
@EnableJpaRepositories(basePackages = "com.skilltrack.common.repository")
public class SkillTrackApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillTrackApiApplication.class, args);
    }
}
