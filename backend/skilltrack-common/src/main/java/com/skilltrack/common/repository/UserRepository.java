package com.skilltrack.common.repository;

import com.skilltrack.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * 
 * Provides CRUD operations and custom queries for user management.
 * Spring Data JPA automatically implements these methods at runtime.
 * 
 * Learning points:
 * - Method naming conventions (findBy, existsBy) are auto-implemented
 * - @Query for complex queries with JPQL
 * - Optional<T> for potentially absent results
 * - @Param for named parameters in queries
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email address.
     * Used for login and duplicate email checking.
     * 
     * Method naming convention: findBy + PropertyName
     * Spring Data JPA generates: SELECT * FROM users WHERE email = ?
     * 
     * @param email User's email address
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by GitHub ID.
     * Used for GitHub OAuth login to find existing linked accounts.
     * 
     * @param githubId GitHub user ID
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByGithubId(String githubId);

    /**
     * Check if email already exists.
     * More efficient than findByEmail when you only need to check existence.
     * 
     * Method naming convention: existsBy + PropertyName
     * Spring Data JPA generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email with roles eagerly fetched.
     * Used for authentication to load user with all roles in one query.
     * 
     * @Query annotation allows custom JPQL queries
     * JOIN FETCH eliminates N+1 query problem (loads roles in same query)
     * 
     * @param email User's email
     * @return Optional containing user with roles loaded
     */
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /**
     * Check if GitHub ID already exists.
     * Used during GitHub OAuth to prevent duplicate accounts.
     * 
     * @param githubId GitHub user ID
     * @return true if GitHub ID exists, false otherwise
     */
    boolean existsByGithubId(String githubId);
}
