package com.skilltrack.api.mapper;

import com.skilltrack.api.dto.response.UserResponse;
import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.RoleName;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity to DTO conversions.
 * 
 * MapStruct generates implementation at compile time (no reflection overhead).
 * Configured as Spring bean for dependency injection.
 * 
 * Mapping Strategy:
 * - Simple fields: Mapped by name automatically
 * - Complex fields: Custom @Named methods
 * - Collections: Transformed via helper methods
 * 
 * Security:
 * - Never maps password or sensitive fields to response DTOs
 * - Only maps fields explicitly defined in DTOs
 * 
 * Usage in Service Layer:
 * <pre>
 * UserResponse response = userMapper.toResponse(user);
 * </pre>
 * 
 * @see User Source entity
 * @see UserResponse Target DTO
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert User entity to UserResponse DTO.
     * 
     * Transformations:
     * - roles: Set<Role> → Set<RoleName> (via extractRoleNames)
     * - Other fields: Direct mapping by name
     * 
     * @param user User entity from database
     * @return UserResponse DTO for API response
     */
    @Mapping(target = "roles", source = "roles", qualifiedByName = "extractRoleNames")
    UserResponse toResponse(User user);

    /**
     * Extract role names from Role entities.
     * 
     * Converts: Set<Role> → Set<RoleName>
     * Example: [Role(ROLE_STUDENT), Role(ROLE_INSTRUCTOR)] → [ROLE_STUDENT, ROLE_INSTRUCTOR]
     * 
     * @param roles Set of Role entities
     * @return Set of RoleName enums
     */
    @Named("extractRoleNames")
    default Set<RoleName> extractRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
    }
}
