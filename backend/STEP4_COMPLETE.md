# Step 4: User Registration API - Implementation Complete! ✅

## What We Built

This step implemented the complete user registration flow with email/password, including validation, password hashing, email verification token generation, and notification emails.

---

## Components Created

### 1. **DTOs (Data Transfer Objects)**

#### `RegisterRequest.java`
Request DTO for user registration.

**Fields:**
```java
- email: String (@NotBlank, @Email, max 255 chars)
- password: String (@NotBlank, @ValidPassword)
- fullName: String (@NotBlank, 2-100 chars)
- country: String (@NotBlank, 2-100 chars)
- organization: String (optional, max 255 chars)
- roleName: RoleName (ROLE_STUDENT or ROLE_INSTRUCTOR)
```

**Validation:**
- Bean Validation annotations (@NotBlank, @Email, @Size)
- Custom password policy validation (@ValidPassword)
- Business rule validation (role selection)

**Security:**
- `toString()` excludes password (never logged)
- `normalizeEmail()` converts to lowercase for case-insensitive comparison
- `isValidRoleForRegistration()` prevents self-assignment of ROLE_ADMIN

---

#### `UserResponse.java`
Response DTO for user data.

**Fields:**
```java
- id: UUID
- email: String
- fullName: String
- country: String
- organization: String
- roles: Set<RoleName>
- emailVerificationStatus: EmailVerificationStatus (PENDING/VERIFIED)
- instructorVerificationStatus: InstructorVerificationStatus (PENDING/APPROVED/REJECTED)
- githubId: String (null for email/password registration)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

**Security:**
- NEVER includes password hash
- NEVER includes verification tokens
- Excludes sensitive fields (failedLoginAttempts, accountLocked)

**Utility Methods:**
- `hasRole(RoleName)` - Check if user has specific role
- `isEmailVerified()` - Check if email is verified
- `isApprovedInstructor()` - Check if instructor can create courses

---

### 2. **Password Validation**

#### `@ValidPassword` Annotation
Custom validation annotation for password policy enforcement.

**Requirements:**
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (!@#$%^&*()-_+=)

**Rationale:**
- Length prevents brute force attacks
- Character diversity increases entropy
- Meets OWASP password guidelines

---

#### `PasswordValidator.java`
Implementation of @ValidPassword validation logic.

**Validation Process:**
1. Check null or blank → fail
2. Check minimum length (8 chars) → fail with custom message
3. Check uppercase letter → fail with custom message
4. Check lowercase letter → fail with custom message
5. Check digit → fail with custom message
6. Check special character → fail with custom message
7. All pass → success

**Performance:**
- Precompiled regex patterns (faster than compiling on each validation)
- Fails fast on first violation

**Error Messages:**
- Custom messages for each validation failure
- Security: Never logs password value

---

### 3. **Entity-DTO Mapping**

#### `UserMapper.java`
MapStruct mapper for User entity ↔ DTO conversions.

**Why MapStruct?**
- Generates implementation at compile time (no reflection overhead)
- Type-safe (compile-time checking)
- Easy to customize mappings
- Spring bean integration (@Mapper(componentModel = "spring"))

**Mapping:**
```java
User entity → UserResponse DTO
- Simple fields: Automatic mapping by name
- roles: Set<Role> → Set<RoleName> (custom method)
```

**Usage:**
```java
@Autowired
private UserMapper userMapper;

UserResponse response = userMapper.toResponse(user);
```

---

### 4. **Email Service**

#### `EmailService.java`
Service for sending HTML emails using JavaMailSender and Thymeleaf templates.

**Features:**
- **Async sending**: @Async annotations (doesn't block registration)
- **HTML templates**: Thymeleaf for professional emails
- **Error handling**: Logs errors but doesn't throw (resilience)

**Methods:**

**1. `sendVerificationEmail(User, token)`**
Sends email verification link to newly registered user.

**Email contains:**
- Welcome message with user's name
- Verification link: `{frontendUrl}/verify-email?token={token}`
- Expiration notice (1 hour)
- Security warning (ignore if not requested)

**Template:** `templates/email/verify-email.html`

**2. `sendPasswordResetEmail(User, token)`** (for Step 7)
Sends password reset link.

**Template:** `templates/email/reset-password.html`

---

**Email Architecture:**
```
Registration Flow:
1. User submits registration form
2. Backend creates user + token
3. EmailService.sendVerificationEmail() queued (async)
4. Backend returns 201 Created immediately
5. Background thread sends email via SMTP
6. User receives email
7. User clicks link → Frontend → Backend /verify-email API
```

**Configuration:**
- Development: MailHog (SMTP mock on localhost:1025)
- Production: AWS SES, SendGrid, Mailgun

---

### 5. **User Service**

#### `UserService.java`
Business logic for user management operations.

**Main Method: `registerUser(RegisterRequest)`**

**Registration Flow:**
```
1. Normalize email (lowercase, trim)
2. Check email uniqueness → throw ValidationException if exists
3. Validate role selection (cannot be ADMIN)
4. Hash password with BCrypt
5. Build User entity:
   - emailVerificationStatus: PENDING
   - instructorVerificationStatus: PENDING (if ROLE_INSTRUCTOR)
   - accountLocked: false
   - failedLoginAttempts: 0
6. Assign role from database
7. Save user to database (transaction)
8. Generate EmailVerificationToken (expires in 1 hour)
9. Save token to database
10. Queue verification email (async)
11. Return UserResponse DTO
```

**Transaction Management:**
- @Transactional ensures atomicity
- If any step fails, entire transaction rolls back
- Email sending failure doesn't rollback (happens after commit)

**Error Handling:**
- Email exists → ValidationException (400)
- Invalid role → ValidationException (400)
- Role not found → ValidationException (400)
- Database error → RuntimeException (500)

**Other Methods:**
- `getUserById(UUID)` - Get user by ID
- `getUserByEmail(String)` - Get user by email (with roles)

---

### 6. **Auth Controller**

#### `AuthController.java`
REST controller for authentication endpoints.

**Base Path:** `/api/auth`

**Endpoint: POST `/api/auth/register`**

**Request:**
```json
POST /api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "country": "United States",
  "organization": "Example Corp",
  "roleName": "ROLE_STUDENT"
}
```

**Success Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "country": "United States",
  "organization": "Example Corp",
  "roles": ["ROLE_STUDENT"],
  "emailVerificationStatus": "PENDING",
  "instructorVerificationStatus": null,
  "githubId": null,
  "createdAt": "2026-02-20T10:30:00",
  "updatedAt": "2026-02-20T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2026-02-20T10:30:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Email is already registered",
  "path": "/api/auth/register",
  "details": []
}
```

**Validation Error (400 Bad Request):**
```json
{
  "timestamp": "2026-02-20T10:30:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "details": [
    {
      "field": "password",
      "message": "Password must contain at least one uppercase letter (A-Z)"
    }
  ]
}
```

**Security:**
- Endpoint is public (permitAll in SecurityConfig)
- CORS enabled for Angular frontend
- @Valid triggers Bean Validation
- GlobalExceptionHandler formats all errors consistently

---

### 7. **Email Templates**

#### `verify-email.html`
Professional HTML email template for email verification.

**Features:**
- Responsive design (works on mobile)
- Gradient header with branding
- Prominent CTA button
- Alternative text link (if button doesn't work)
- Expiration notice
- Security warning
- Footer with contact info

**Thymeleaf Variables:**
- `${userName}` - User's full name
- `${verificationLink}` - Full URL with token
- `${expirationHours}` - Token expiration (1 hour)

---

#### `reset-password.html`
HTML email template for password reset (used in Step 7).

**Features:**
- Similar design to verification email
- Different color scheme (red gradient)
- Security warnings
- Password tips

---

### 8. **Configuration**

#### `AsyncConfiguration.java`
Enables @Async annotation support for background tasks.

**Purpose:**
- Email sending runs in background threads
- Prevents blocking HTTP response
- Better performance and user experience

**Usage:**
```java
@Async
public void sendVerificationEmail(User user, String token) {
    // Runs in background thread
}
```

---

#### **application.yml Updates**

**Added:**
```yaml
# Thymeleaf for email templates
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    encoding: UTF-8
    cache: false  # Disable in development

# Frontend URL for email links
app:
  frontend-url: ${FRONTEND_URL:http://localhost:4200}
```

---

#### **pom.xml Updates**

**Added:**
```xml
<!-- Thymeleaf for email templates -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

---

#### **SkillTrackApiApplication.java Update**

**Added:**
```java
@EnableJpaAuditing  // Enables @CreatedDate, @LastModifiedDate
```

---

## Architecture Diagram

```
Registration Request Flow:
┌─────────────────────────────────────────────────────────────┐
│                  POST /api/auth/register                    │
│  {email, password, fullName, country, org, roleName}        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   AuthController                            │
│  - Validates request with @Valid                            │
│  - Calls UserService.registerUser()                         │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                     UserService                             │
│  1. Normalize email (lowercase)                             │
│  2. Check uniqueness (existsByEmail)                        │
│  3. Validate role selection                                 │
│  4. Hash password (BCrypt)                                  │
│  5. Build User entity                                       │
│  6. Assign role from DB                                     │
│  7. Save user (transaction)                                 │
│  8. Generate EmailVerificationToken                         │
│  9. Save token                                              │
│  10. Queue email (async)                                    │
│  11. Return UserResponse DTO                                │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ├──────────────────────────────────────┐
                       │                                      │
                       ▼                                      ▼
┌─────────────────────────────────┐    ┌──────────────────────────────┐
│         Database (PostgreSQL)    │    │      EmailService (@Async)   │
│  - users table                   │    │  1. Build Thymeleaf context  │
│  - user_roles table              │    │  2. Process HTML template    │
│  - email_verification_tokens     │    │  3. Send via JavaMailSender  │
│                                  │    │  4. MailHog (development)    │
└─────────────────────────────────┘    └──────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              201 Created Response                           │
│  {id, email, fullName, roles, emailVerificationStatus...}   │
└─────────────────────────────────────────────────────────────┘
```

---

## File Structure Created

```
skilltrack-api/src/main/java/com/skilltrack/api/
├── config/
│   └── AsyncConfiguration.java             ✅ Enable @Async
├── dto/
│   ├── request/
│   │   └── RegisterRequest.java            ✅ Registration input
│   └── response/
│       └── UserResponse.java               ✅ User data output
├── validation/
│   ├── ValidPassword.java                  ✅ Password policy annotation
│   └── PasswordValidator.java              ✅ Password validation logic
├── mapper/
│   └── UserMapper.java                     ✅ MapStruct entity-DTO mapper
├── service/
│   ├── EmailService.java                   ✅ Email sending (async)
│   └── UserService.java                    ✅ Registration business logic
└── controller/
    └── AuthController.java                 ✅ POST /api/auth/register

skilltrack-api/src/main/resources/
├── application.yml                         ✅ Updated (Thymeleaf, frontend URL)
└── templates/email/
    ├── verify-email.html                   ✅ Email verification template
    └── reset-password.html                 ✅ Password reset template (Step 7)
```

---

## Testing the Registration API

### 1. Start Infrastructure
```bash
cd backend
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Redis (port 6379)
- MailHog (SMTP: 1025, Web UI: 8025)

---

### 2. Build and Run
```bash
mvn clean install
cd skilltrack-api
mvn spring-boot:run
```

---

### 3. Test Registration

**Valid Registration (Student):**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "SecurePass123!",
    "fullName": "John Doe",
    "country": "United States",
    "organization": "Example Corp",
    "roleName": "ROLE_STUDENT"
  }'
```

**Expected Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "student@example.com",
  "fullName": "John Doe",
  "country": "United States",
  "organization": "Example Corp",
  "roles": ["ROLE_STUDENT"],
  "emailVerificationStatus": "PENDING",
  "instructorVerificationStatus": null,
  "githubId": null,
  "createdAt": "2026-02-20T10:30:00",
  "updatedAt": "2026-02-20T10:30:00"
}
```

---

**Valid Registration (Instructor):**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "instructor@example.com",
    "password": "SecurePass456!",
    "fullName": "Jane Smith",
    "country": "Canada",
    "organization": "Tech Academy",
    "roleName": "ROLE_INSTRUCTOR"
  }'
```

**Expected Response (201):**
```json
{
  ...
  "roles": ["ROLE_INSTRUCTOR"],
  "emailVerificationStatus": "PENDING",
  "instructorVerificationStatus": "PENDING"  // Admin approval required
}
```

---

**Duplicate Email (Error):**
```bash
# Register same email again
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "SecurePass789!",
    "fullName": "Another User",
    "country": "UK",
    "roleName": "ROLE_STUDENT"
  }'
```

**Expected Response (400):**
```json
{
  "timestamp": "2026-02-20T10:35:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Email is already registered",
  "path": "/api/auth/register",
  "details": []
}
```

---

**Weak Password (Error):**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "weak",
    "fullName": "Test User",
    "country": "USA",
    "roleName": "ROLE_STUDENT"
  }'
```

**Expected Response (400):**
```json
{
  "timestamp": "2026-02-20T10:36:00",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "details": [
    {
      "field": "password",
      "message": "Password must be at least 8 characters long"
    }
  ]
}
```

---

### 4. Check Email (MailHog)

Open MailHog web UI: http://localhost:8025

You should see:
- Subject: "Verify Your Email - SkillTrack LMS"
- Beautiful HTML email with verification button
- Link like: `http://localhost:4200/verify-email?token=abc-123-def-456`

---

### 5. Verify Database

```bash
# Connect to PostgreSQL
docker exec -it skilltrack-postgres psql -U skilltrack -d skilltrack

# Check users
SELECT id, email, full_name, email_verification_status FROM users;

# Check user roles
SELECT u.email, r.role_name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;

# Check verification tokens
SELECT token_value, expires_at, used 
FROM email_verification_tokens 
WHERE user_id = (SELECT id FROM users WHERE email = 'student@example.com');
```

---

## Key Learning Outcomes

### 1. DTO Pattern
- Separation of API contract (DTO) from domain model (Entity)
- Security: Control what data is exposed
- Flexibility: API changes don't affect database schema

### 2. Bean Validation
- Declarative validation with annotations
- @Valid triggers automatic validation
- ConstraintValidator for custom rules
- Custom error messages

### 3. Password Security
- BCrypt hashing (one-way, salted)
- Password complexity policy (OWASP guidelines)
- Never log or expose passwords
- Work factor tuning (trades computation time vs security)

### 4. MapStruct
- Compile-time code generation (no reflection)
- Type-safe mappings
- Custom mapping methods
- Spring integration

### 5 Async Processing
- @Async for background tasks
- Non-blocking email sending
- Better user experience (fast API response)
- Error isolation (email failure doesn't fail registration)

### 6. Email Templates
- Thymeleaf for HTML emails
- Professional design
- Variable interpolation
- Responsive layout

### 7. Transaction Management
- @Transactional ensures atomicity
- Rollback on exception
- Database consistency guarantees

### 8. Service Layer Pattern
- Business logic in service classes
- Controllers delegate to services
- Keeps controllers thin
- Easier to test

---

## Security Best Practices Applied

✅ **Password hashing** - BCrypt with salt  
✅ **Password policy** - 8+ chars, complexity requirements  
✅ **Email normalization** - Lowercase, trim  
✅ **Role validation** - Cannot self-assign ADMIN  
✅ **Token expiration** - 1 hour for verification  
✅ **Never log passwords** - toString() excludes password  
✅ **DTO sanitization** - Response never includes password  
✅ **Email enumeration prevention** - Consistent error messages  
✅ **Input validation** - @Valid with Bean Validation  
✅ **CORS configuration** - Restrict allowed origins  

---

## Next Steps

**Step 5: Email Verification Flow** 🔄 NEXT
- POST `/api/auth/verify-email` - Verify email with token
- POST `/api/auth/resend-verification` - Resend verification email
- Token validation (expiration, single-use)
- Mark user as verified
- Handle edge cases (expired, already verified, not found)

---

## Summary

**Completed:**
- ✅ Registration API endpoint (POST /api/auth/register)
- ✅ Request validation (email, password, required fields)
- ✅ Password complexity validation (custom @ValidPassword)
- ✅ Email uniqueness check
- ✅ Role assignment (ROLE_STUDENT, ROLE_INSTRUCTOR)
- ✅ Password hashing with BCrypt
- ✅ EmailVerificationToken generation
- ✅ Email sending with HTML templates (async)
- ✅ MapStruct entity-DTO mapping
- ✅ Comprehensive error handling
- ✅ Professional email templates

**Ready for:**
- Email verification flow (Step 5)
- Login/logout (Step 6)
- Password reset (Step 7)

**User Experience:**
1. User submits registration form ✅
2. Backend validates and creates account ✅
3. User receives verification email ✅
4. User clicks link → Frontend → Backend verify API (Step 5)
5. User logs in (Step 6)
