package com.skilltrack.common.entity;

import com.skilltrack.common.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity representing user roles in the system.
 * 
 * Roles are pre-seeded during application startup:
 * - ROLE_ADMIN
 * - ROLE_INSTRUCTOR
 * - ROLE_STUDENT
 * 
 * Users can have multiple roles (many-to-many relationship).
 * 
 * Follows coding standards:
 * - Immutable after creation
 * - Enum-based role names for type safety
 * - Natural business key (roleName) for equality
 */
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "role_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private RoleName roleName;

    @Column(name = "description")
    private String description;

    /**
     * Convenience constructor for role creation.
     */
    public Role(RoleName roleName) {
        this.roleName = roleName;
    }

    /**
     * Business key equality based on roleName.
     * More stable than ID-based equality for reference data.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return roleName != null && roleName == role.roleName;
    }

    @Override
    public int hashCode() {
        return roleName != null ? roleName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleName=" + roleName +
                ", id='" + getId() + '\'' +
                '}';
    }
}
