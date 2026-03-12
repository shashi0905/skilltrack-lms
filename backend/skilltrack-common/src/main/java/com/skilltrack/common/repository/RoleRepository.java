package com.skilltrack.common.repository;

import com.skilltrack.common.entity.Role;
import com.skilltrack.common.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * 
 * Roles are reference data (pre-seeded during application startup).
 * This repository is primarily used for looking up roles by name
 * when assigning roles to users.
 * 
 * Learning points:
 * - Repository for reference/lookup data
 * - Simple queries based on business key (roleName)
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * Find role by role name.
     * Used during user registration to assign default role.
     * 
     * Example: roleRepository.findByRoleName(RoleName.ROLE_STUDENT)
     * 
     * @param roleName Role name enum
     * @return Optional containing role if found
     */
    Optional<Role> findByRoleName(RoleName roleName);

    /**
     * Check if role exists by name.
     * Used for validation or checking if roles are properly seeded.
     * 
     * @param roleName Role name enum
     * @return true if role exists
     */
    boolean existsByRoleName(RoleName roleName);
}
