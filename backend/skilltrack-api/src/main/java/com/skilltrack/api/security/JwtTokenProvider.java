package com.skilltrack.api.security;

import com.skilltrack.api.config.JwtProperties;
import com.skilltrack.common.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider - Core JWT utility for token generation and validation.
 * 
 * JWT (JSON Web Token) structure:
 * - Header: Algorithm and token type {"alg": "HS256", "typ": "JWT"}
 * - Payload: Claims (user data) {"sub": "user@example.com", "roles": "ROLE_STUDENT", "exp": 1234567890}
 * - Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 * 
 * Final token format: header.payload.signature (Base64 encoded)
 * Example: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.3xJg5fF_L7...
 * 
 * Learning points:
 * - JWT is stateless (server doesn't store sessions)
 * - Token is self-contained (includes all user information)
 * - Signature ensures token hasn't been tampered with
 * - Tokens cannot be revoked (must wait for expiration)
 * - Use short expiration times for security
 * 
 * Security considerations:
 * - Always use HTTPS (tokens sent in Authorization header)
 * - Store tokens securely on client (httpOnly cookies or secure storage)
 * - Never include sensitive data in payload (it's Base64, not encrypted!)
 * - Use strong secret key (256+ bits)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * Generates JWT token from authentication object.
     * 
     * Flow:
     * 1. Extract username (email) from authenticated principal
     * 2. Extract user roles from granted authorities
     * 3. Build token with claims (subject, roles, issued-at, expiration)
     * 4. Sign token with secret key
     * 5. Return compact JWT string
     * 
     * Token claims:
     * - sub (subject): User's email (unique identifier)
     * - roles: Comma-separated role names (e.g., "ROLE_STUDENT,ROLE_INSTRUCTOR")
     * - iat (issued at): Token creation timestamp
     * - exp (expiration): Token expiry timestamp
     * 
     * @param authentication Spring Security authentication object
     * @return JWT token string (header.payload.signature)
     */
    public String generateToken(Authentication authentication) {
        // Extract username (email) from authenticated user
        String username = authentication.getName();
        
        // Extract roles from GrantedAuthority objects
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        // Current timestamp for issuedAt claim
        Date now = new Date();
        
        // Expiration timestamp (current time + configured expiration)
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        
        // Build and sign JWT token
        return Jwts.builder()
                .subject(username)                          // Set subject (user email)
                .claim("roles", roles)                      // Add custom claim for roles
                .issuedAt(now)                              // Set issued-at timestamp
                .expiration(expiryDate)                     // Set expiration timestamp
                .signWith(getSigningKey())                  // Sign with secret key
                .compact();                                 // Build compact JWT string
    }

    /**
     * Extracts username (email) from JWT token.
     * 
     * Process:
     * 1. Parse token string
     * 2. Verify signature with secret key
     * 3. Extract claims payload
     * 4. Return subject claim (username/email)
     * 
     * @param token JWT token string
     * @return Username (email) from token subject
     * @throws JwtException if token is invalid, expired, or tampered
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())               // Verify signature
                .build()
                .parseSignedClaims(token)                  // Parse and verify
                .getPayload();                             // Extract claims
        
        return claims.getSubject();                        // Return subject (email)
    }

    /**
     * Get access token expiration time in milliseconds.
     * Used by AuthService to populate "expiresIn" field in login response.
     * 
     * @return Expiration time in milliseconds (e.g., 86400000 = 24 hours)
     */
    public long getExpirationTime() {
        return jwtProperties.getExpiration();
    }

    /**
     * Generate JWT access token directly for a User entity.
     * Used by OAuth2 success handler where we have User but not Authentication.
     * 
     * @param user User entity
     * @return JWT access token
     */
    public String generateTokenForUser(User user) {
        String username = user.getEmail();
        String roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate JWT refresh token directly for a User entity.
     * Used by OAuth2 success handler where we have User but not Authentication.
     * 
     * @param user User entity
     * @return JWT refresh token
     */
    public String generateRefreshTokenForUser(User user) {
        String username = user.getEmail();
        String roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates JWT token.
     * 
     * Validation checks:
     * 1. Signature is valid (token not tampered)
     * 2. Token is not expired
     * 3. Token format is correct
     * 4. Secret key matches
     * 
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Parse and verify token (throws exception if invalid)
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            
            return true;
            
        } catch (SignatureException ex) {
            // Signature doesn't match (token was tampered or wrong secret)
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // Token format is invalid
            log.error("Invalid JWT token format: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Token has expired
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // Token algorithm not supported
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Token is null or empty
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        
        return false;
    }

    /**
     * Generates signing key from secret.
     * 
     * HMAC-SHA256 requires:
     * - Minimum 256 bits (32 bytes) for security
     * - Secret as byte array
     * 
     * Learning point:
     * - HS256 is symmetric encryption (same key for sign and verify)
     * - Secret must be kept confidential on server
     * - If secret is compromised, attacker can forge tokens
     * 
     * @return SecretKey for signing/verifying JWT tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates refresh token with longer expiration.
     * 
     * Refresh tokens are used to obtain new access tokens
     * without requiring user to log in again.
     * 
     * Typically:
     * - Access token: short-lived (minutes to hours)
     * - Refresh token: long-lived (days to weeks)
     * 
     * Flow:
     * 1. User logs in → receive access token + refresh token
     * 2. Access token expires → use refresh token to get new access token
     * 3. Refresh token expires → user must log in again
     * 
     * @param authentication Spring Security authentication
     * @return Refresh token string
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
}
