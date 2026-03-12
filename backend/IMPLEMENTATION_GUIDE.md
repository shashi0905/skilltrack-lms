# Phase 1: User Registration & Login - Implementation Guide

## Overview

This document provides a step-by-step walkthrough of implementing the user registration and login feature for SkillTrack LMS using Spring Boot and Spring Security. It serves as a learning resource to understand how modern authentication flows are built.

---

## Implementation Steps Summary

### ✅ Step 1: Project Setup (COMPLETED)

**What we built:**
- Multi-module Maven project structure
- Parent POM with dependency management
- Four modules: common, api, batch, scheduler

**Key decisions:**
- **Java 21**: Modern language features (Records, Pattern Matching, Text Blocks)
- **Spring Boot 3.2.2**: Latest stable version with Spring Security 6
- **PostgreSQL**: Relational database for transactional data
- **JWT**: Stateless authentication tokens
- **Maven**: Build tool with modular structure

**Files created:**
```
backend/
├── pom.xml (parent)
├── skilltrack-common/pom.xml
├── skilltrack-api/pom.xml
├── skilltrack-batch/pom.xml
├── skilltrack-scheduler/pom.xml
└── README.md
```

**Learning points:**
- Multi-module Maven allows code reuse and separation of concerns
- Dependency management in parent POM prevents version conflicts
- Each module has a specific responsibility (common = shared code, api = REST endpoints)

---

### ✅ Step 2: Domain Model - Common Module (COMPLETED)

**What we built:**
Domain entities representing users, roles, and authentication tokens.

#### 2.1 Base Entity (`BaseEntity.java`)
**Purpose**: Provide common fields for all entities

**Features:**
- UUID-based primary key (distributed-system friendly)
- Audit timestamps (`createdAt`, `updatedAt`) via `@CreatedDate`, `@LastModifiedDate`
- Optimistic locking with `@Version`
- Soft delete support (deleted flag instead of physical deletion)
- Proper `equals()` and `hashCode()` based on ID

**Learning points:**
- **JPA Auditing**: `@EntityListeners(AuditingEntityListener.class)` automatically populates timestamps
- **UUID vs Long ID**: UUIDs are globally unique, better for distributed systems
- **Soft deletes**: Keep data for audit trails instead of deleting rows
- **@MappedSuperclass**: Not an entity itself, but provides fields to subclasses

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    private boolean deleted = false;
}
```

#### 2.2 Enums
**Purpose**: Type-safe constants for roles and verification statuses

**RoleName.java:**
- `ROLE_ADMIN`: Full system access (backend-created only)
- `ROLE_INSTRUCTOR`: Can create courses (must be verified by admin)
- `ROLE_STUDENT`: Can enroll in courses (default role)

**EmailVerificationStatus.java:**
- `PENDING`: Just registered, verification email sent
- `VERIFIED`: Email confirmed or OAuth user

**InstructorVerificationStatus.java:**
- `UNVERIFIED`: New instructor, not yet reviewed
- `VERIFIED`: Admin approved
- `REJECTED`: Admin rejected

**Learning points:**
- Use Java enums instead of string constants (type safety, IDE autocomplete)
- Store as `@Enumerated(EnumType.STRING)` in database (readable, migration-safe)
- Document business rules in enum Javadoc

#### 2.3 Role Entity (`Role.java`)
**Purpose**: Represent user roles (seeded during app startup)

**Features:**
- Enum-based role name for type safety
- Unique constraint on role_name
- Business key equality (based on roleName, not ID)

**Learning points:**
- **Reference data**: Roles are pre-seeded, not created by users
- **Business key equality**: Use roleName for equals/hashCode (more stable than generated ID)
- **@UniqueConstraint**: Database-level uniqueness guarantee

```java
@Entity
@Table(name = "roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "role_name")
})
public class Role extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private RoleName roleName;
    
    @Override
    public boolean equals(Object o) {
        // Based on roleName, not ID
        return roleName != null && roleName == role.roleName;
    }
}
```

#### 2.4 User Entity (`User.java`)
**Purpose**: Core entity representing all users in the system

**Features:**
- **Email**: Unique identifier (indexed for fast lookup)
- **Password hash**: BCrypt-hashed password (nullable for OAuth users)
- **Full name, country, organization**: Profile data
- **GitHub ID**: For OAuth users (nullable, unique, indexed)
- **Email verification**: PENDING → VERIFIED flow
- **Instructor verification**: UNVERIFIED → VERIFIED (for instructors only)
- **Failed login tracking**: Counter + account lock flag
- **Roles**: Many-to-many relationship with Role (eager fetch for security)

**Security considerations:**
- Password is hashed, never stored in plain text
- Email uniqueness enforced at database level
- Account locking after failed attempts
- GitHub ID is nullable and indexed

**Business methods:**
```java
public void addRole(Role role) { ... }
public boolean hasRole(RoleName roleName) { ... }
public void verifyEmail() { ... }
public boolean isEmailVerified() { ... }
public void incrementFailedLoginAttempts() { ... }
public void resetFailedLoginAttempts() { ... }
public void lockAccount() { ... }
```

**Learning points:**
- **Business logic in entity**: Domain-Driven Design (DDD) pattern
- **Eager fetch for roles**: Small dataset, always needed for security checks
- **Defensive programming**: Check for null before adding/removing roles
- **Business key equality**: Use email (natural/stable identifier)
- **@ManyToMany with join table**: `user_roles` intermediary table

```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
}, indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_github_id", columnList = "github_id")
})
public class User extends BaseEntity {
    private String email;
    private String passwordHash;
    private String fullName;
    private String githubId;
    
    @Enumerated(EnumType.STRING)
    private EmailVerificationStatus emailVerificationStatus;
    
    @Enumerated(EnumType.STRING)
    private InstructorVerificationStatus instructorVerificationStatus;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", ...)
    private Set<Role> roles;
}
```

#### 2.5 Email Verification Token (`EmailVerificationToken.java`)
**Purpose**: One-time tokens for email verification

**Flow:**
1. User registers → token created and emailed
2. User clicks link with token → token validated
3. Token marked as used → user's email marked as verified

**Features:**
- **Token value**: Secure random UUID (single-use)
- **Expiry**: Configurable (default 1 hour)
- **Used flag**: Prevents reuse
- **User relationship**: Many-to-one (lazy fetch)

**Business methods:**
```java
public boolean isExpired() { ... }
public boolean isValid() { ... }  // Not used AND not expired
public void markAsUsed() { ... }
public static EmailVerificationToken createToken(User, hours) { ... }
```

**Learning points:**
- **Factory method**: `createToken()` encapsulates creation logic
- **UUID tokens**: Cryptographically secure, unpredictable
- **Lazy fetch for user**: Only load when needed (performance)
- **Audit trail**: Old tokens stay in database (not deleted)

#### 2.6 Password Reset Token (`PasswordResetToken.java`)
**Purpose**: One-time tokens for password reset

**Identical structure to EmailVerificationToken:**
- Separate entity for clear domain modeling
- Same security properties (UUIDs, expiry, single-use)
- Same business methods

**Flow:**
1. User requests reset → token created and emailed
2. User clicks link → token validated
3. User sets new password → token marked as used

#### 2.7 Exception Classes
**Purpose**: Consistent error handling across the application

**Exception hierarchy:**
```
RuntimeException
└── BusinessException (base class with errorCode)
    ├── ResourceNotFoundException (HTTP 404)
    ├── ValidationException (HTTP 400)
    ├── AuthenticationException (HTTP 401)
    └── AuthorizationException (HTTP 403)
```

**Learning points:**
- **Unchecked exceptions**: Extends RuntimeException (no forced try-catch)
- **Error codes**: Machine-readable codes for API clients
- **HTTP mapping**: Each exception maps to specific HTTP status

**ErrorResponse DTO:**
Standardized API error response format
```json
{
  "timestamp": "2026-02-11T10:30:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Email already registered",
  "path": "/api/auth/register",
  "details": [
    {"field": "email", "message": "must be unique"}
  ]
}
```

---

## Next Steps

### 🔄 Step 3: Security Configuration & JWT (IN PROGRESS)

**What we'll build:**
- JWT token generation and validation utility
- Spring Security configuration
- Custom UserDetailsService
- Authentication filters
- CORS configuration

**Key components:**
1. **JwtTokenProvider**: Generate and validate JWT tokens
2. **SecurityConfig**: Configure Spring Security filter chain
3. **JwtAuthenticationFilter**: Extract and validate JWT from requests
4. **UserDetailsServiceImpl**: Load user by username for authentication

**Learning objectives:**
- How JWT authentication works (stateless)
- Spring Security filter chain
- Token-based session management
- Password encoding with BCrypt

---

### Step 4: User Registration API

**Endpoints to build:**
- `POST /api/auth/register` - Email/password registration
- `POST /api/auth/verify-email` - Verify email token
- `POST /api/auth/resend-verification` - Resend verification email

**Components:**
- `AuthController`: REST endpoints
- `UserService`: Business logic
- `UserRepository`: Data access
- `EmailService`: Send verification emails
- DTOs: `RegisterRequest`, `UserResponse`

**Flow diagram:**
```
Client                  Controller              Service              Repository          EmailService
  |                         |                      |                      |                    |
  |-- POST /register ------>|                      |                      |                    |
  |                         |-- registerUser() --->|                      |                    |
  |                         |                      |-- validate email --->|                    |
  |                         |                      |<-- email unique -----|                    |
  |                         |                      |-- hash password      |                    |
  |                         |                      |-- create user ------>|                    |
  |                         |                      |<-- saved user -------|                    |
  |                         |                      |-- create token ----->|                    |
  |                         |                      |<-- saved token ------|                    |
  |                         |                      |-- send email --------|------------------>|
  |                         |<-- UserResponse -----|                      |                    |
  |<-- 201 Created ---------|                      |                      |                    |
```

---

### Step 5: Email Verification Flow

**Learning objectives:**
- Token validation patterns
- Transaction management with `@Transactional`
- Email template rendering
- Error handling (expired tokens, already used)

---

### Step 6: Login & Logout

**Endpoints:**
- `POST /api/auth/login` - Email/password login
- `POST /api/auth/logout` - Invalidate token (client-side)
- `POST /api/auth/refresh` - Refresh JWT token

**Learning objectives:**
- Authentication vs Authorization
- Stateless JWT (no server-side session)
- Failed login tracking
- Account locking logic

---

### Step 7: Password Reset Flow

**Endpoints:**
- `POST /api/auth/forgot-password` - Request reset token
- `POST /api/auth/reset-password` - Reset with token

**Learning objectives:**
- Security considerations (don't reveal if email exists)
- Time-limited tokens
- Password validation rules

---

### Step 8: GitHub OAuth Integration

**Configuration:**
- OAuth2 client setup
- GitHub app registration
- Callback handling
- First-time profile completion

**Learning objectives:**
- OAuth 2.0 flow (Authorization Code Grant)
- Spring Security OAuth2 Client
- Linking OAuth users to internal users
- Trusted identity (no email verification needed)

---

## Coding Standards Applied

### 1. Entity Design
✅ **Business logic in entities** (DDD):
```java
// ✅ GOOD - Entity has behavior
public class User {
    public void verifyEmail() {
        this.emailVerificationStatus = VERIFIED;
    }
}

// ❌ BAD - Anemic domain model
public class User {
    // Only getters/setters
}
```

### 2. Constructor Injection
✅ **Immutable dependencies**:
```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class UserService {
    private final UserRepository userRepository;  // final = immutable
    private final PasswordEncoder passwordEncoder;
}
```

### 3. Optional for Return Types
✅ **Explicit null handling**:
```java
// ✅ GOOD - Optional signals "may be absent"
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// Usage
User user = findByEmail(email)
    .orElseThrow(() -> new ResourceNotFoundException("User", email));
```

### 4. Validation
✅ **Fail fast with validation**:
```java
@PostMapping("/register")
public ResponseEntity<UserResponse> register(
    @Valid @RequestBody RegisterRequest request) {  // @Valid triggers validation
    // ...
}
```

### 5. Exception Handling
✅ **Unchecked business exceptions**:
```java
// No need for try-catch everywhere
public void register(RegisterRequest request) {
    if (emailExists(request.getEmail())) {
        throw new ValidationException("Email already registered");
    }
    // ...
}
```

---

## Database Schema

**Tables to be created** (via Flyway migrations in next steps):

```sql
-- Roles (pre-seeded)
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Users
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(100),
    full_name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    organization VARCHAR(255),
    github_id VARCHAR(50) UNIQUE,
    email_verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    instructor_verification_status VARCHAR(20),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_github_id ON users(github_id);

-- User-Role many-to-many
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Email verification tokens
CREATE TABLE email_verification_tokens (
    id VARCHAR(36) PRIMARY KEY,
    token_value VARCHAR(36) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_token_value ON email_verification_tokens(token_value);
CREATE INDEX idx_user_id ON email_verification_tokens(user_id);

-- Password reset tokens (similar structure)
CREATE TABLE password_reset_tokens (
    id VARCHAR(36) PRIMARY KEY,
    token_value VARCHAR(36) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_reset_token_value ON password_reset_tokens(token_value);
CREATE INDEX idx_reset_user_id ON password_reset_tokens(user_id);
```

---

## Testing Strategy

### Unit Tests
- Service layer business logic
- Entity business methods
- Validation rules

### Integration Tests
- Repository layer with Testcontainers (PostgreSQL)
- Controller endpoints with MockMvc
- Security configuration

### Example unit test:
```java
@Test
void verifyEmail_ShouldUpdateStatus() {
    // Given
    User user = User.builder()
        .email("test@example.com")
        .emailVerificationStatus(EmailVerificationStatus.PENDING)
        .build();
    
    // When
    user.verifyEmail();
    
    // Then
    assertThat(user.isEmailVerified()).isTrue();
}
```

---

## Running the Application

### Prerequisites
1. Install Java 21
2. Install PostgreSQL 15+
3. Install Maven 3.9+

### Build
```bash
cd backend
mvn clean install
```

### Run API module
```bash
cd skilltrack-api
mvn spring-boot:run
```

### Access
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console (dev): http://localhost:8080/h2-console

---

## Summary

**Completed:**
✅ Multi-module project structure
✅ Domain model with entities, enums, exceptions
✅ Base infrastructure (auditing, soft deletes, error handling)

**Next:**
🔄 JWT & Spring Security configuration
⏳ User registration API
⏳ Email verification flow
⏳ Login/logout endpoints
⏳ Password reset flow
⏳ GitHub OAuth integration

**Learning outcomes so far:**
- Multi-module Maven project organization
- JPA entity design with auditing and relationships
- Domain-Driven Design (business logic in entities)
- Exception hierarchy for clean error handling
- Coding standards: constructor injection, Optional, immutability

---

## References

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JWT.io](https://jwt.io/) - JWT debugger
- Project documents:
  - [Phase 1 Feature Story](../docs/stories/phase-1-user-registration-and-login.md)
  - [Technical Context](../docs/technical-context.md)
  - [Core Standards](../docs/guidelines/core-standards.md)
  - [Java & Spring Boot Standards](../docs/guidelines/java-spring-boot.md)
