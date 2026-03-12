package com.skilltrack.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter - Intercepts every HTTP request to validate JWT tokens.
 * 
 * Spring Security Filter Chain:
 * Request → [Various Filters] → JwtAuthenticationFilter → [Auth Filters] → Controller
 * 
 * This filter:
 * 1. Extracts JWT from Authorization header
 * 2. Validates token (signature, expiration)
 * 3. Loads user from token claims
 * 4. Sets authentication in SecurityContext
 * 5. Passes request to next filter
 * 
 * Learning points:
 * - OncePerRequestFilter ensures filter runs exactly once per request
 * - SecurityContextHolder stores authentication for current thread
 * - Authentication object enables @PreAuthorize, @Secured, etc.
 * - Filter chain must always be called (even on error)
 * 
 * Flow diagram:
 * ```
 * Client Request
 *     ↓
 * [Authorization: Bearer eyJhbGc...]
 *     ↓
 * JwtAuthenticationFilter
 *     ↓
 * Extract token → Validate → Load user → Set SecurityContext
 *     ↓
 * Next Filter / Controller (with authentication)
 * ```
 * 
 * Security considerations:
 * - Always validate token before trusting claims
 * - Clear SecurityContext if token invalid (don't authenticate)
 * - Log security events for auditing
 * - Never expose token validation errors to client (information leakage)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Core filter method - called for every HTTP request.
     * 
     * Process:
     * 1. Extract JWT from Authorization header
     * 2. Validate token format and signature
     * 3. Extract username from token claims
     * 4. Load user details from database
     * 5. Create Authentication object
     * 6. Store in SecurityContextHolder
     * 7. Continue filter chain
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Chain of filters to continue
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Step 1: Extract JWT from Authorization header
            String jwt = getJwtFromRequest(request);
            
            // Step 2-3: Validate token and extract username
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                
                // Step 4: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Step 5: Create authentication object
                // UsernamePasswordAuthenticationToken is Spring Security's auth implementation
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,              // Principal (logged-in user)
                                null,                     // Credentials (not needed, already authenticated)
                                userDetails.getAuthorities()  // Roles/permissions
                        );
                
                // Add additional details (IP address, session ID, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Step 6: Store authentication in SecurityContext
                // This makes user available to @PreAuthorize, controllers, etc.
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set authentication in security context for user: {}", username);
            }
            
        } catch (Exception ex) {
            // Log error but don't stop request processing
            // This allows public endpoints to work even if token is invalid
            log.error("Could not set user authentication in security context", ex);
        }
        
        // Step 7: Always continue filter chain (critical!)
        // Even if authentication fails, request should proceed to next filter
        // Public endpoints should still be accessible
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from Authorization header.
     * 
     * Expected header format:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * 
     * Process:
     * 1. Get Authorization header value
     * 2. Check if it starts with "Bearer "
     * 3. Remove "Bearer " prefix
     * 4. Return token string
     * 
     * Why "Bearer"?
     * - Standard OAuth 2.0 token type
     * - Indicates token should be used "as-is" without additional processing
     * - Alternative types: Basic (username:password), Digest, etc.
     * 
     * @param request HTTP request
     * @return JWT token string, or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if Authorization header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix (7 characters)
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
