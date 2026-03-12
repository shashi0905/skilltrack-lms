# Step 3: JWT & Spring Security - Implementation Complete! ✅

## What We Built

This step implemented the complete JWT-based authentication infrastructure for SkillTrack LMS. 

---

## Components Created

### 1. **Repositories** (Data Access Layer)

#### `UserRepository.java`
Spring Data JPA repository for User entity.
- `findByEmail()` - Login and duplicate checking
- `findByGithubId()` - GitHub OAuth login
- `existsByEmail()` - Efficient email existence check
- `findByEmailWithRoles()` - Single query with JOIN FETCH (no N+1 problem)

**Learning points:**
- Method naming conventions auto-generate queries
- `@Query` for complex JPQL
- `JOIN FETCH` eliminates N+1 query problem
- `Optional<T>` for null-safe returns

#### `RoleRepository.java`
Repository for role reference data.
- `findByRoleName()` - Lookup role by enum
- `existsByRoleName()` - Check if role seeded

#### `EmailVerificationTokenRepository.java`
Repository for email verification tokens.
- `findByTokenValue()` - Validate token from email link
- `deleteExpiredTokens()` - Cleanup job with `@Modifying`

#### `PasswordResetTokenRepository.java`
Repository for password reset tokens (identical pattern).

---

### 2. **Security Configuration**

#### `PasswordEncoderConfig.java`
Configures BCrypt password encoder.
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Strength 10 (1024 rounds)
}
```

**Why BCrypt?**
- One-way hash (cannot be reversed)
- Automatic salt generation
- Configurable work factor
- Slow by design (prevents brute force)

**Example usage:**
```java
String hashed = passwordEncoder.encode("password123");
boolean matches = passwordEncoder.matches("password123", hashed);
```

---

#### `JwtProperties.java`
Type-safe JWT configuration from `application.yml`.
```yaml
jwt:
  secret: YourSecret...
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

**Security notes:**
- Secret must be 256+ bits for HS256
- Use environment variables in production
- Never commit secrets to VCS

---

#### `JwtTokenProvider.java`
Core JWT utility for token generation and validation.

**JWT Structure:**
```
header.payload.signature

header:   {"alg": "HS256", "typ": "JWT"}
payload:  {"sub": "user@example.com", "roles": "ROLE_STUDENT", "exp": 1234567890}
signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
```

**Methods:**
1. `generateToken(Authentication)` - Create JWT from authenticated user
   - Claims: subject (email), roles, issued-at, expiration
   - Signed with HMAC-SHA256

2. `getUsernameFromToken(String)` - Extract email from JWT
   - Parses token, verifies signature, returns subject

3. `validateToken(String)` - Validate JWT
   - Checks: signature, expiration, format
   - Returns true/false

4. `generateRefreshToken(Authentication)` - Longer-lived token
   - Used to obtain new access tokens without re-login

**Security considerations:**
- JWT is stateless (can't be revoked until expiry)
- Use short expiration times
- Signature ensures integrity (no tampering)
- Payload is Base64 (not encrypted!) - don't store sensitive data

---

### 3. **Authentication Components**

#### `UserDetailsServiceImpl.java`
Bridges Spring Security and our User entity.

**Spring Security authentication flow:**
```
1. User submits credentials
2. AuthenticationManager calls UserDetailsService.loadUserByUsername()
3. Returns UserDetails with hashed password
4. Spring Security compares passwords
5. If match, creates Authentication object
6. Stores in SecurityContextHolder
```

**Methods:**
- `loadUserByUsername(email)` - Load user for authentication
- `loadUserById(id)` - Load user from JWT claim

**Returns:** Spring Security's `UserDetails` with:
- Username (email)
- Password hash
- Authorities (roles)
- Account status (locked, disabled, expired)

---

#### `JwtAuthenticationFilter.java`
Intercepts every HTTP request to validate JWT tokens.

**Filter chain position:**
```
Request → CORS → CSRF → JWT Filter → Auth Filter → Controller
```

**Process:**
1. Extract JWT from `Authorization: Bearer <token>` header
2. Validate token (signature, expiration)
3. Extract username from token
4. Load user details from database
5. Create `Authentication` object
6. Store in `SecurityContextHolder`
7. Continue filter chain

**Why extends OncePerRequestFilter?**
- Ensures filter runs exactly once per request
- Not twice for forward/include

**SecurityContextHolder:**
- Thread-local storage for authentication
- Available to `@PreAuthorize`, controllers, services

---

#### `SecurityConfig.java`
Main Spring Security configuration - the heart of security.

**Configures:**

1. **Authorization rules:**
```java
.requestMatchers("/api/auth/**").permitAll()     // Public: register, login
.requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin only
.anyRequest().authenticated()                     // Everything else protected
```

2. **Session management:**
```java
.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```
- No server-side sessions
- Each request must include JWT

3. **CORS configuration:**
```java
.allowedOrigins("http://localhost:4200")  // Angular
.allowedMethods("GET", "POST", "PUT", "DELETE")
.allowedHeaders("Authorization", "Content-Type")
.allowCredentials(true)
```
- Allows frontend (different domain) to call API
- Without CORS, browser blocks cross-origin requests

4. **CSRF disabled:**
- Not needed for stateless JWT API
- CSRF is for cookie-based sessions

5. **Custom filter:**
```java
.addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class)
```
- JWT filter runs before standard auth filter

6. **AuthenticationManager bean:**
- Needed by login controller to authenticate users
- Coordinates multiple authentication providers

---

### 4. **Exception Handling**

#### `GlobalExceptionHandler.java`
Centralized exception handler with `@RestControllerAdvice`.

**Handles:**
- `MethodArgumentNotValidException` → 400 (validation errors)
- `ValidationException` → 400 (custom validation)
- `ResourceNotFoundException` → 404 (not found)
- `SpringAuthException` → 401 (auth failed)
- `AccessDeniedException` → 403 (forbidden)
- `Exception` → 500 (unexpected errors)

**Returns consistent error format:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Email already registered",
  "path": "/api/auth/register",
  "details": [
    {"field": "email", "message": "must be unique"}
  ]
}
```

**Benefits:**
- Consistent error responses
- Centralized error handling (DRY)
- Proper HTTP status codes
- Hides internal errors (security)
- Logs for debugging

---

### 5. **Database Setup**

#### Flyway Migrations
Database schema versioning and migration tool.

**Migration files:**
- `V1__Create_roles_table.sql` - Roles with unique constraint
- `V2__Create_users_table.sql` - Users with indexes
- `V3__Create_verification_tokens_tables.sql` - Token tables

**Why Flyway?**
- Version control for database schema
- Repeatable deployments
- Tracks applied migrations
- Team collaboration on schema changes

**Configuration:**
```yaml
flyway:
  enabled: true
  baseline-on-migrate: true
  locations: classpath:db/migration
```

---

#### `DataInitializationConfig.java`
Seeds reference data at startup with `CommandLineRunner`.

**Seeds roles:**
- ROLE_STUDENT (default for new users)
- ROLE_INSTRUCTOR (create courses)
- ROLE_ADMIN (full access)

**Idempotent:** Checks existence before creating.

---

### 6. **Testing Endpoint**

#### `PublicController.java`
Simple health check endpoint.

```bash
GET /api/public/health
{
  "status": "UP",
  "service": "SkillTrack API",
  "timestamp": "2026-02-19T10:30:00"
}
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      HTTP Request                           │
│              Authorization: Bearer <JWT>                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    CORS Filter                              │
│         (Allow Angular localhost:4200)                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│               JwtAuthenticationFilter                       │
│  1. Extract JWT from header                                 │
│  2. Validate token (JwtTokenProvider)                       │
│  3. Load user (UserDetailsService)                          │
│  4. Set SecurityContext                                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Authorization Filter                           │
│  Check roles (@PreAuthorize, hasRole)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Controller                                │
│  Authentication available in SecurityContext                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  Business logic with authenticated user                     │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                           │
│  Database access via Spring Data JPA                        │
└─────────────────────────────────────────────────────────────┘
```

---

## File Structure Created

```
skilltrack-api/src/main/java/com/skilltrack/api/
├── SkillTrackApiApplication.java          ✅ Main class with package scanning
├── config/
│   ├── PasswordEncoderConfig.java         ✅ BCrypt configuration
│   ├── JwtProperties.java                 ✅ JWT config properties
│   └── DataInitializationConfig.java      ✅ Role seeding
├── security/
│   ├── JwtTokenProvider.java              ✅ JWT generation/validation
│   ├── UserDetailsServiceImpl.java        ✅ Load users for auth
│   ├── JwtAuthenticationFilter.java       ✅ Request filter
│   └── SecurityConfig.java                ✅ Main security config
├── exception/
│   └── GlobalExceptionHandler.java        ✅ Centralized error handling
└── controller/
    └── PublicController.java              ✅ Health check endpoint

skilltrack-api/src/main/resources/
├── application.yml                        ✅ Configuration
└── db/migration/
    ├── V1__Create_roles_table.sql         ✅ Flyway migration
    ├── V2__Create_users_table.sql         ✅ Flyway migration
    └── V3__Create_verification_tokens_tables.sql  ✅ Flyway migration

skilltrack-common/src/main/java/com/skilltrack/common/
└── repository/
    ├── UserRepository.java                ✅ User data access
    ├── RoleRepository.java                ✅ Role data access
    ├── EmailVerificationTokenRepository.java  ✅ Token data access
    └── PasswordResetTokenRepository.java  ✅ Token data access
```

---

## How to Test

### 1. Start PostgreSQL
```bash
cd backend
docker-compose up -d postgres
```

### 2. Build project
```bash
mvn clean install
```

### 3. Run API
```bash
cd skilltrack-api
mvn spring-boot:run
```

### 4. Test health endpoint
```bash
curl http://localhost:8080/api/public/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "SkillTrack API",
  "timestamp": "2026-02-19T10:30:00",
  "message": "Service is running"
}
```

### 5. Verify database
```bash
# Connect to PostgreSQL
docker exec -it skilltrack-postgres psql -U skilltrack -d skilltrack

# Check tables created
\dt

# Check roles seeded
SELECT * FROM roles;
```

Expected output:
```
 id   |    role_name     |           description
------+------------------+----------------------------------
 ...  | ROLE_STUDENT     | Student role - can browse...
 ...  | ROLE_INSTRUCTOR  | Instructor role - can create...
 ...  | ROLE_ADMIN       | Administrator role - full...
```

---

## Key Learning Outcomes

### 1. JWT Authentication
- How JWT works (header.payload.signature)
- Token generation with claims
- Token validation and expiry
- Stateless session management

### 2. Spring Security
- Filter chain architecture
- UserDetailsService integration
- AuthenticationManager usage
- Role-based access control
- CORS configuration

### 3. Password Security
- BCrypt hashing algorithm
- Salt generation and storage
- Password verification
- Work factor tuning

### 4. Database Migrations
- Flyway version control
- Idempotent migrations
- Schema evolution
- Team collaboration

### 5. Repository Pattern
- Spring Data JPA conventions
- Query method naming
- Custom JPQL queries
- JOIN FETCH optimization

### 6. Exception Handling
- Global exception handler
- Consistent error responses
- HTTP status code mapping
- Security considerations

---

## Security Best Practices Applied

✅ **Password hashing** - BCrypt with automatic salts  
✅ **JWT signing** - HMAC-SHA256 with secret key  
✅ **Stateless sessions** - No server-side session storage  
✅ **CORS configuration** - Restrict allowed origins  
✅ **CSRF disabled** - Not needed for JWT API  
✅ **Token expiration** - Short-lived access tokens  
✅ **Error handling** - Don't leak internal details  
✅ **Database indexes** - Performance on common queries  
✅ **Idempotent operations** - Safe to run multiple times  

---

## Next Steps

With authentication infrastructure complete, we can now implement:

**Step 4: User Registration API** 🔄 NEXT
- POST `/api/auth/register` - Register new user
- Email verification token generation
- Send verification email
- DTOs and validation rules

---

## Summary

**Completed:**
- ✅ JWT token generation and validation
- ✅ Spring Security configuration
- ✅ User authentication with BCrypt
- ✅ Role-based authorization
- ✅ CORS for Angular frontend
- ✅ Global exception handling
- ✅ Database migrations with Flyway
- ✅ Role seeding at startup
- ✅ Repository layer with optimized queries

**Ready for:**
- User registration endpoints
- Email verification flow
- Login/logout functionality
- Password reset flow

**Application is now:**
- Secure by default
- Stateless (scalable)
- Production-ready authentication
- Type-safe configuration
- Database version controlled
- Error handling consistent
