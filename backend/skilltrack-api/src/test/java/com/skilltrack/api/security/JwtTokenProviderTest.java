package com.skilltrack.api.security;

import com.skilltrack.api.config.JwtProperties;
import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "ThisIsAVeryLongSecretKeyForTestingJwtTokenProviderWith256Bits!!";
    private static final long EXPIRATION = 86400000L; // 24 hours
    private static final long REFRESH_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpiration(EXPIRATION);
        props.setRefreshExpiration(REFRESH_EXPIRATION);
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    private Authentication createAuthentication(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void generateToken_returnsValidJwt() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");

        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void getUsernameFromToken_returnsCorrectEmail() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");
        String token = jwtTokenProvider.generateToken(auth);

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_tamperedToken_returnsFalse() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");
        String token = jwtTokenProvider.generateToken(auth);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpiration(0L); // expires immediately
        props.setRefreshExpiration(0L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(props);

        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");
        String token = expiredProvider.generateToken(auth);

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_nullToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    void generateRefreshToken_returnsValidJwt() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");

        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        assertThat(refreshToken).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo("test@example.com");
    }

    @Test
    void generateTokenForUser_returnsValidJwt() {
        Role role = new Role();
        role.setRoleName(RoleName.ROLE_INSTRUCTOR);
        User user = User.builder()
                .email("instructor@example.com")
                .roles(Set.of(role))
                .build();

        String token = jwtTokenProvider.generateTokenForUser(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("instructor@example.com");
    }

    @Test
    void generateRefreshTokenForUser_returnsValidJwt() {
        Role role = new Role();
        role.setRoleName(RoleName.ROLE_STUDENT);
        User user = User.builder()
                .email("student@example.com")
                .roles(Set.of(role))
                .build();

        String token = jwtTokenProvider.generateRefreshTokenForUser(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getExpirationTime_returnsConfiguredValue() {
        assertThat(jwtTokenProvider.getExpirationTime()).isEqualTo(EXPIRATION);
    }

    @Test
    void tokenSignedWithDifferentSecret_failsValidation() {
        Authentication auth = createAuthentication("test@example.com", "ROLE_STUDENT");
        String token = jwtTokenProvider.generateToken(auth);

        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("ACompletelyDifferentSecretKeyThatIsAlsoLongEnoughForHS256!!");
        otherProps.setExpiration(EXPIRATION);
        otherProps.setRefreshExpiration(REFRESH_EXPIRATION);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        assertThat(otherProvider.validateToken(token)).isFalse();
    }
}
