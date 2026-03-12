package com.skilltrack.api.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Failure handler for OAuth2 authentication.
 * 
 * Handles OAuth2 authentication failures:
 * - User denies authorization on GitHub
 * - GitHub API errors
 * - Invalid OAuth2 configuration
 * - Network errors
 * 
 * Error Handling Strategy:
 * - Log error details for debugging
 * - Redirect to frontend with error parameter
 * - Frontend displays user-friendly error message
 * 
 * Common Errors:
 * - access_denied: User clicked "Cancel" on GitHub
 * - invalid_client: Wrong GitHub app credentials
 * - server_error: GitHub API down
 * 
 * @see OAuth2AuthenticationSuccessHandler Success handler
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/oauth2/redirect}")
    private String redirectUri;

    /**
     * Handle OAuth2 authentication failure.
     * 
     * Process:
     * 1. Log error details for debugging
     * 2. Build redirect URL with error parameter
     * 3. Redirect to frontend error page
     * 4. Frontend shows user-friendly error message
     * 
     * Error URL Format:
     * - http://localhost:4200/oauth2/redirect?error=access_denied
     * 
     * Frontend Error Handling:
     * - Parse error from query parameter
     * - Show appropriate error message
     * - Provide "Try again" or "Back to login" button
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param exception Authentication exception with error details
     * @throws IOException if redirect fails
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException {
        
        log.error("OAuth2 authentication failed: {}", exception.getMessage());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "authentication_failed")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
