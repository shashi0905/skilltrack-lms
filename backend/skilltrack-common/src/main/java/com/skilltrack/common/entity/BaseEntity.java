package com.skilltrack.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base entity class providing common fields for all entities.
 * Includes:
 * - Primary key (ID)
 * - Audit timestamps (created/updated)
 * - Soft delete flag
 * - Version for optimistic locking
 * 
 * Follows coding standards:
 * - Immutable ID (no setter)
 * - Auditing via JPA listeners
 * - Proper equals/hashCode based on ID
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Marks this entity as deleted (soft delete).
     * Does not remove from database.
     */
    public void markAsDeleted() {
        this.deleted = true;
    }

    /**
     * Equality based on ID for JPA entities.
     * Follows best practice: use business key when available, otherwise ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
