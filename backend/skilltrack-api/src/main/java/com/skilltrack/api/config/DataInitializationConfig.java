package com.skilltrack.api.config;

import com.skilltrack.common.entity.Role;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database initialization configuration.
 * 
 * Runs once at application startup to seed reference data.
 * 
 * CommandLineRunner:
 * - Executes after Spring context is fully initialized
 * - Runs before application starts accepting requests
 * - Useful for data seeding, schema validation, etc.
 * 
 * Learning points:
 * - Reference data (roles) should be pre-populated
 * - Idempotent operations (safe to run multiple times)
 * - Check existence before creating
 * - Log initialization for visibility
 * 
 * Why seed roles at startup?
 * - Roles are fixed system data, not user data
 * - Ensures roles exist before first user registration
 * - Avoids "Role not found" errors in registration flow
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializationConfig {

    private final RoleRepository roleRepository;

    /**
     * Seeds roles into database at startup.
     * 
     * Creates three system roles:
     * - ROLE_STUDENT: Default role for all new users
     * - ROLE_INSTRUCTOR: For users who create courses
     * - ROLE_ADMIN: For platform administrators
     * 
     * Idempotent: Checks if role exists before creating
     * 
     * @return CommandLineRunner that seeds roles
     */
    @Bean
    public CommandLineRunner initializeRoles() {
        return args -> {
            log.info("Initializing system roles...");
            
            // Seed ROLE_STUDENT
            createRoleIfNotExists(
                RoleName.ROLE_STUDENT,
                "Student role - can browse and enroll in courses"
            );
            
            // Seed ROLE_INSTRUCTOR
            createRoleIfNotExists(
                RoleName.ROLE_INSTRUCTOR,
                "Instructor role - can create and manage courses"
            );
            
            // Seed ROLE_ADMIN
            createRoleIfNotExists(
                RoleName.ROLE_ADMIN,
                "Administrator role - full platform access"
            );
            
            log.info("Role initialization completed successfully");
        };
    }

    /**
     * Creates a role if it doesn't already exist.
     * 
     * Idempotent operation: Safe to call multiple times
     * 
     * @param roleName Role name enum
     * @param description Role description
     */
    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (!roleRepository.existsByRoleName(roleName)) {
            Role role = Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }
}
