package com.skilltrack.api.security;

import com.skilltrack.common.entity.User;
import com.skilltrack.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * Spring Security's authentication process:
 * 1. User submits credentials (email/password)
 * 2. AuthenticationManager calls UserDetailsService.loadUserByUsername()
 * 3. UserDetailsService loads user from database
 * 4. Spring Security compares submitted password with stored hash
 * 5. If match, creates Authentication object with UserDetails
 * 6. Authentication stored in SecurityContextHolder
 * 
 * Learning points:
 * - UserDetailsService is a core Spring Security interface
 * - UserDetails wraps our User entity for Spring Security
 * - GrantedAuthority represents roles/permissions
 * - Password checking is handled by Spring Security (not here)
 * - This service only loads user data, doesn't validate credentials
 * 
 * Why we need this:
 * - Bridge between Spring Security and our User entity
 * - Spring Security needs UserDetails, we have User entity
 * - Converts our roles to Spring Security authorities
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user by username (email in our case).
     * 
     * Called by Spring Security during authentication.
     * 
     * Process:
     * 1. Look up user by email in database
     * 2. If not found, throw UsernameNotFoundException
     * 3. Convert User entity to UserDetails (Spring Security format)
     * 4. Convert roles to GrantedAuthority objects
     * 5. Return UserDetails with username, password, and authorities
     * 
     * @Transactional ensures lazy-loaded roles are fetched
     * 
     * @param username User's email (we use email as username)
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load user with roles in single query (avoid N+1)
        User user = userRepository.findByEmailWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + username));

        // Convert our Role entities to Spring Security GrantedAuthority
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toSet());

        // Build Spring Security User (implements UserDetails)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())                       // Username (email)
                .password(user.getPasswordHash())                // Hashed password
                .authorities(authorities)                        // Roles as authorities
                .accountExpired(false)                          // Account not expired
                .accountLocked(user.isAccountLocked())          // Check if locked
                .credentialsExpired(false)                      // Credentials not expired
                .disabled(user.isDeleted())                     // Check if deleted/disabled
                .build();
    }

    /**
     * Helper method to load user by ID.
     * Useful for JWT authentication where we have user ID in token.
     * 
     * @param userId User's UUID
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userId));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.isAccountLocked())
                .credentialsExpired(false)
                .disabled(user.isDeleted())
                .build();
    }
}
