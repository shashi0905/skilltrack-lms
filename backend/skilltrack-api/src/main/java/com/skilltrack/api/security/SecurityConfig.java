package com.skilltrack.api.security;

import com.skilltrack.api.security.oauth.OAuth2AuthenticationFailureHandler;
import com.skilltrack.api.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration - The heart of application security.
 * 
 * This class configures:
 * 1. Authentication (who are you?)
 * 2. Authorization (what can you do?)
 * 3. Session management (stateless JWT)
 * 4. CORS (Cross-Origin Resource Sharing)
 * 5. CSRF protection (disabled for stateless API)
 * 6. Custom filters (JWT authentication)
 * 
 * Learning points:
 * - SecurityFilterChain is the modern way to configure Spring Security (replaces WebSecurityConfigurerAdapter)
 * - Filter chain processes requests before they reach controllers
 * - Stateless sessions mean no server-side session storage (JWT contains all info)
 * - CORS allows frontend (different domain) to call API
 * - Method security (@PreAuthorize, @Secured) enables fine-grained access control
 * 
 * Security architecture:
 * ```
 * Request → CORS Filter → CSRF Filter → JWT Filter → Auth Filter → Controller
 *            ↓             ↓             ↓            ↓
 *         Allow origin   Skip (API)   Validate    Check roles
 * ```
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    /**
     * Configures the main security filter chain.
     * 
     * This is where we define:
     * - Which endpoints are public vs protected
     * - Session management strategy
     * - CORS and CSRF settings
     * - Custom filter placement
     * 
     * @param http HttpSecurity configuration builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS: Allow frontend (Angular) to call API from different domain
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF: Disabled for stateless API (JWT in header, not cookie)
            // CSRF protection is for cookie-based sessions, not needed for JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Authorization rules: Define which endpoints require authentication
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/auth/**").permitAll()           // Registration, login, password reset
                .requestMatchers("/api/public/**").permitAll()         // Public course catalog
                .requestMatchers("/actuator/health").permitAll()       // Health check
                .requestMatchers("/v3/api-docs/**").permitAll()        // OpenAPI docs
                .requestMatchers("/swagger-ui/**").permitAll()         // Swagger UI
                .requestMatchers("/oauth2/**").permitAll()             // OAuth2 authorization
                .requestMatchers("/login/oauth2/**").permitAll()       // OAuth2 login callback
                .requestMatchers("/swagger-ui.html").permitAll()       // Swagger UI entry
                
                // Protected endpoints (require authentication)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Admin only
                .requestMatchers(HttpMethod.POST, "/api/courses/**")
                    .hasAnyRole("INSTRUCTOR", "ADMIN")                 // Create courses
                .anyRequest().authenticated()                          // Everything else requires auth
            )
            
            // OAuth2 Login Configuration
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorize")                      // Custom authorization endpoint
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")                  // OAuth2 redirect endpoint
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)    // Custom success handler
                .failureHandler(oAuth2AuthenticationFailureHandler)    // Custom failure handler
            )
            
            // Session management: Stateless (no server-side sessions)
            // Each request must include JWT token
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Add JWT filter before Spring Security's authentication filter
            // This ensures JWT is processed first, then standard auth happens
            .addFilterBefore(jwtAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing).
     * 
     * CORS allows frontend application (e.g., Angular on localhost:4200)
     * to make requests to backend API (e.g., Spring Boot on localhost:8080).
     * 
     * Without CORS:
     * - Browser blocks requests from different origin (security)
     * - "No 'Access-Control-Allow-Origin' header" error
     * 
     * With CORS:
     * - Server explicitly allows specific origins
     * - Browser permits cross-origin requests
     * 
     * Learning points:
     * - CORS is a browser security feature, not server security
     * - Preflight requests (OPTIONS) check CORS before actual request
     * - Production: Restrict allowed origins to specific domains
     * - Development: Allow localhost for testing
     * 
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow these origins to call API
        // Production: Replace with actual frontend domain
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",    // Angular dev server
            "http://localhost:3000"     // Alternative frontend
        ));
        
        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow these headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",    // For JWT token
            "Content-Type",     // For JSON payloads
            "Accept",
            "X-Requested-With"
        ));
        
        // Expose these headers to client
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        // Apply CORS to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Configures authentication provider.
     * 
     * DaoAuthenticationProvider:
     * - DAO = Data Access Object (loads user from database)
     * - Uses UserDetailsService to load user
     * - Uses PasswordEncoder to verify password
     * 
     * Authentication flow:
     * 1. User submits email + password
     * 2. DaoAuthenticationProvider calls UserDetailsService.loadUserByUsername()
     * 3. UserDetailsService returns UserDetails with hashed password
     * 4. DaoAuthenticationProvider uses PasswordEncoder.matches() to compare
     * 5. If match, authentication succeeds
     * 6. If no match, throws BadCredentialsException
     * 
     * @return Configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // Set UserDetailsService to load users
        authProvider.setUserDetailsService(userDetailsService);
        
        // Set PasswordEncoder to verify passwords
        authProvider.setPasswordEncoder(passwordEncoder);
        
        return authProvider;
    }

    /**
     * Exposes AuthenticationManager as a bean.
     * 
     * AuthenticationManager is needed by login controller to authenticate users.
     * It coordinates multiple AuthenticationProviders (we have DaoAuthenticationProvider).
     * 
     * Usage in controller:
     * ```
     * Authentication auth = authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(email, password)
     * );
     * ```
     * 
     * @param config Spring's authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
