package com.skilltrack.api.security;

import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        Role studentRole = new Role(RoleName.ROLE_STUDENT);
        user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>(Set.of(studentRole)))
                .build();
        user.setId("user-id-1");
    }

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(details.getUsername()).isEqualTo("test@example.com");
        assertThat(details.getPassword()).isEqualTo("hashedPassword");
        assertThat(details.getAuthorities()).hasSize(1);
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailWithRoles("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_lockedAccount_returnsLockedUserDetails() {
        user.setAccountLocked(true);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    void loadUserById_found_returnsUserDetails() {
        when(userRepository.findById("user-id-1")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserById("user-id-1");

        assertThat(details.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserById_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById("bad-id"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_multipleRoles_returnsAllAuthorities() {
        Role instructorRole = new Role(RoleName.ROLE_INSTRUCTOR);
        user.getRoles().add(instructorRole);
        when(userRepository.findByEmailWithRoles("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(details.getAuthorities()).hasSize(2);
    }
}
