package com.skilltrack.api.security.oauth;

import com.skilltrack.api.security.JwtTokenProvider;
import com.skilltrack.common.entity.Role;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.EmailVerificationStatus;
import com.skilltrack.common.enums.RoleName;
import com.skilltrack.common.repository.RoleRepository;
import com.skilltrack.common.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/**
 * Success handler for OAuth2 authentication (GitHub login).
 * 
 * Responsibilities:
 * - Handle successful OAuth2 authentication from GitHub
 * - Create or link user account with GitHub profile
 * - Generate JWT tokens for authenticated session
 * - Redirect to frontend with tokens
 * 
 * OAuth2 Flow:
 * 1. User clicks "Login with GitHub" on frontend
 * 2. Frontend redirects to: /oauth2/authorization/github
 * 3. Spring Security redirects to GitHub authorization page
 * 4. User authorizes app on GitHub
 * 5. GitHub redirects back with authorization code
 * 6. Spring Security exchanges code for access token
 * 7. Spring Security fetches user profile from GitHub
 * 8. This handler is invoked with OAuth2User data
 * 9. Create/link user account in database
 * 10. Generate JWT tokens
 * 11. Redirect to frontend with tokens in URL fragment
 * 
 * User Account Linking:
 * - First-time GitHub login: Create new user account
 * - Existing GitHub user: Login with existing account
 * - Email match: Can link GitHub to existing email/password account
 * 
 * Security Considerations:
 * - Email from GitHub is pre-verified (emailVerificationStatus = VERIFIED)
 * - No password stored (GitHub OAuth only)
 * - GitHub user ID stored for future logins
 * - JWT tokens passed via URL fragment (not query params for security)
 * 
 * @see OAuth2UserService Custom user service for GitHub
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/oauth2/redirect}")
    private String redirectUri;

    /**
     * Handle successful OAuth2 authentication.
     * 
     * Process:
     * 1. Extract GitHub user data from OAuth2User
     * 2. Find or create user account in database
     * 3. Update user profile with GitHub data
     * 4. Generate JWT access token and refresh token
     * 5. Redirect to frontend with tokens in URL fragment
     * 
     * URL Fragment vs Query Params:
     * - Fragment (#): Client-side only, not sent to server (more secure)
     * - Query (?): Sent to server, visible in logs (less secure)
     * - We use fragment for tokens: /oauth2/redirect#access_token=...
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param authentication OAuth2 authentication with user data
     * @throws IOException if redirect fails
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.error("Unexpected authentication type: {}", authentication.getClass());
            response.sendRedirect(redirectUri + "?error=authentication_failed");
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        
        try {
            // Extract GitHub user data
            Map<String, Object> attributes = oauthUser.getAttributes();
            String githubId = String.valueOf(attributes.get("id"));
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String login = (String) attributes.get("login"); // GitHub username
            
            log.info("GitHub OAuth login: githubId={}, email={}, name={}, login={}", 
                    githubId, email, name, login);

            // Find or create user
            User user = findOrCreateUser(githubId, email, name, login);

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateTokenForUser(user);
            String refreshToken = jwtTokenProvider.generateRefreshTokenForUser(user);

            // Build redirect URL with tokens in fragment
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .fragment("access_token=" + accessToken + 
                             "&refresh_token=" + refreshToken +
                             "&token_type=Bearer")
                    .build()
                    .toUriString();

            log.info("OAuth2 authentication successful for user: {}, redirecting to: {}", 
                    email != null ? email : login, redirectUri);
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            response.sendRedirect(redirectUri + "?error=authentication_failed");
        }
    }

    /**
     * Find existing user or create new user from GitHub profile.
     * 
     * User Lookup Strategy:
     * 1. Try to find by GitHub ID (returning user)
     * 2. Try to find by email (link to existing email/password account)
     * 3. Create new user (first-time GitHub login)
     * 
     * Account Linking:
     * - If user exists with same email, link GitHub ID to that account
     * - User can then login with email/password OR GitHub
     * - GitHub ID is added to existing account
     * 
     * New User Creation:
     * - emailVerificationStatus: VERIFIED (GitHub email is trusted)
     * - Default role: ROLE_STUDENT
     * - No password (GitHub OAuth only)
     * - Full name from GitHub profile
     * 
     * @param githubId GitHub user ID
     * @param email User email from GitHub
     * @param name User name from GitHub
     * @param login GitHub username
     * @return User entity (existing or newly created)
     */
    private User findOrCreateUser(String githubId, String email, String name, String login) {
        // Try to find by GitHub ID
        User user = userRepository.findByGithubId(githubId).orElse(null);
        
        if (user != null) {
            log.info("Found existing user by GitHub ID: {}", githubId);
            // Update profile if changed
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email.toLowerCase());
            }
            if (name != null && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            return userRepository.save(user);
        }

        // Try to find by email (link to existing account)
        if (email != null) {
            user = userRepository.findByEmail(email.toLowerCase()).orElse(null);
            
            if (user != null) {
                log.info("Linking GitHub account to existing user by email: {}", email);
                user.setGithubId(githubId);
                // Mark email as verified (GitHub verified)
                if (!user.isEmailVerified()) {
                    user.verifyEmail();
                }
                return userRepository.save(user);
            }
        }

        // Create new user
        log.info("Creating new user from GitHub profile: githubId={}, email={}", githubId, email);
        
        User newUser = User.builder()
                .email(email != null ? email.toLowerCase() : login + "@github.local")
                .fullName(name != null ? name : login)
                .githubId(githubId)
                .emailVerificationStatus(EmailVerificationStatus.VERIFIED) // GitHub verified
                .instructorVerificationStatus(null) // Student by default
                .accountLocked(false)
                .failedLoginAttempts(0)
                .roles(new HashSet<>())
                .build();

        // Assign default role (ROLE_STUDENT)
        Role studentRole = roleRepository.findByRoleName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("ROLE_STUDENT not found"));
        newUser.getRoles().add(studentRole);

        return userRepository.save(newUser);
    }
}
