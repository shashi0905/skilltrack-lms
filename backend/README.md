# SkillTrack LMS Backend

A multi-module Spring Boot application for the SkillTrack Learning Management System.

## Project Structure

```
skilltrack-lms/
├── skilltrack-common/      # Shared domain model, entities, DTOs, utilities
├── skilltrack-api/         # REST API, controllers, services, security
├── skilltrack-batch/       # Spring Batch jobs for data processing
└── skilltrack-scheduler/   # Quartz scheduled tasks
```

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.2.2
- **Database**: PostgreSQL
- **Cache**: Redis (optional)
- **Security**: Spring Security + JWT
- **Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven
- **Testing**: JUnit 5, Testcontainers

## Prerequisites

- JDK 21 or higher
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+ (optional)

## Build & Run

### Build all modules
```bash
mvn clean install
```

### Run API module
```bash
cd skilltrack-api
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

## Phase 1: User Registration & Login

This implementation follows the feature story documented in:
`docs/stories/phase-1-user-registration-and-login.md`

### Implementation Steps Summary

1. **Project Setup** ✅
   - Multi-module Maven structure
   - Dependencies configuration
   - Coding standards adherence

2. **Common Module - Domain Entities**
   - User entity with roles and verification status
   - Role entity (STUDENT, INSTRUCTOR, ADMIN)
   - Email verification token entity
   - Password reset token entity
   - Base entity classes and audit support

3. **Security Configuration**
   - JWT token generation and validation
   - Spring Security configuration
   - Custom UserDetailsService
   - Authentication filters

4. **User Registration Flow**
   - Email/password registration endpoint
   - Role selection (Student/Instructor)
   - Email verification token generation
   - Email sending service

5. **Email Verification**
   - Token validation endpoint
   - Account activation logic
   - Token expiry handling
   - Resend verification email

6. **Login & Logout**
   - Email/password authentication
   - JWT token issuance
   - Token refresh mechanism
   - Failed login tracking

7. **Password Reset Flow**
   - Forgot password endpoint
   - Reset token generation and validation
   - Password update logic

8. **GitHub OAuth Integration**
   - OAuth2 client configuration
   - First-time profile completion
   - GitHub account linking

9. **Limited Access Control**
   - Browse catalog without verification
   - Block enrollment for unverified users
   - Role-based access control

## API Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Configuration

See `skilltrack-api/src/main/resources/application.yml` for configuration options.

## Coding Standards

This project follows:
- [Core Coding Standards](../docs/guidelines/core-standards.md)
- [Java & Spring Boot Standards](../docs/guidelines/java-spring-boot.md)
- [Angular Style Guide](../docs/guidelines/angular-style-guide.md) (for frontend)
