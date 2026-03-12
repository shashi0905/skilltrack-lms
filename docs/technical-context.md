# SkillTrack – Technical Domain Context

## 1. System Overview

SkillTrack is a full-stack web application consisting of:
- An **Angular** frontend (SPA) for learners, instructors, and admins.
- A **Spring Boot** backend providing REST APIs for public user registration and login, course and enrollment workflows, assessments, batch processing, scheduling, auditing, and reporting.

The system is designed to run locally via Docker Compose (separate containers for frontend, backend, PostgreSQL, Redis) and be production-ready for deployment in containerized environments, where PostgreSQL and Redis may be either containers or managed cloud services.

The architecture emphasizes:
- Separation of concerns between API, batch processing, scheduling, shared domain model, and UI.
- A clear but simple role model (Admin, Instructor, Student) with instructor verification built into the user domain.
- Enterprise-grade use of Spring technologies (Security, Data JPA, Batch, Quartz, Caching, Actuator, Testcontainers).
- Clear boundaries for future extensions such as AI-assisted capabilities using Spring AI.

## 2. Architecture Overview

- **Architecture style:** Modular monolith backend (API, batch, scheduler, common module) plus a separate Angular SPA frontend.
- **Interaction style:** REST/JSON APIs between Angular and the backend, database-backed batch jobs, and scheduled tasks.
- **Persistence:** PostgreSQL as the primary relational database; optional Redis for caching.
- **Security:** Spring Security with JWT-based stateless authentication and role-based access control; OAuth2 login via GitHub.
- **Observability:** Spring Boot Actuator, Micrometer metrics, integration with Prometheus/Grafana (optional).

### 2.1 Repository / Module Layout

Suggested structure:

```text
skilltrack-lms/
  skilltrack-api/        # Spring Boot REST API (controllers, services, security, config)
  skilltrack-batch/      # Spring Batch jobs
  skilltrack-scheduler/  # Quartz-based scheduled jobs
  skilltrack-common/     # Shared domain entities, DTOs, and utilities
  docker-compose.yml     # Local orchestration for PostgreSQL, Redis, etc.
  README.md
```

(An additional module for AI-focused capabilities can later be added, e.g., `skilltrack-ai/`, or implemented inside `skilltrack-api`.)

## 3. Modules and Responsibilities

### 3.0 Angular Frontend (conceptual)

Purpose: Provide the user interface for learners, instructors, and admins as a separate Angular SPA.

Key aspects:
- Auth module: registration, email verification flow, login, logout, and role-aware navigation.
- Catalog module: course listing, search, filters, course details.
- Course player: lesson content, quizzes, progress indicators, AI summaries (where enabled).
- Instructor console: course and lesson authoring, quiz management, basic analytics.
- Admin console: user and instructor verification, course moderation, operational views.
- Communicates with the backend via REST/JSON APIs, using HTTP interceptors for JWT attachment and central error handling.

### 3.1 skilltrack-common

Purpose: Provide shared domain model and cross-cutting utilities to avoid duplication.

Key contents:
- **Domain entities**: `User`, `Role`, `Course`, `CourseModule`, `Enrollment`, `Notification`, `AuditRevision`, `JobExecution`/`JobHistory`, etc.
- **Shared DTOs and mappers** using MapStruct.
- **Common enums and value types**: roles, enrollment status, course difficulty, etc.
- **Base entities**: common ID, timestamps, soft-delete flag, auditing fields.
- **Shared exceptions and error codes**.

### 3.2 skilltrack-api

Purpose: Primary HTTP API for all core LMS functionality.

Responsibilities:
- **REST controllers** for:
  - User registration, login (JWT), GitHub OAuth callback handling.
  - Course and module CRUD operations.
  - Enrollment and progress tracking endpoints.
  - Reporting endpoints (activity reports, metrics access where appropriate).
  - Manual triggers for batch jobs (where exposed via API).

- **Service layer** implementing business logic:
  - User lifecycle (registration, password reset, profile updates).
  - Role management (Admin-only operations).
  - Course publication, module updates, soft deletes.
  - Enrollment orchestration and completion rules.
  - Assessment handling (quizzes as core; optional integration with external code evaluators for coding challenges).

- **Persistence layer** with Spring Data JPA:
  - Repositories for users, roles, courses, modules, enrollments, quizzes, quiz questions/answers, coding challenges (where used), notifications, job history, etc.
  - Advanced JPA/Hibernate usage: custom queries, entity graphs, second-level cache (where applicable), auditing.

- **Security configuration**:
  - Authentication: email/password with BCrypt-hashed passwords; OAuth2 login with GitHub.
  - Stateless session management with JWT.
  - Authorization: method-level security, role hierarchy, endpoint protection (e.g., `/admin/**`).

- **Validation & DTO mapping**:
  - Bean Validation (Jakarta Validation API) for request DTOs.
  - MapStruct mappers to map between entities and DTOs.

- **API documentation**:
  - SpringDoc / OpenAPI 3 for API specification and interactive docs.

- **Observability**:
  - Actuator endpoints for health, metrics, and environment details.
  - Custom metrics (e.g., completed enrollments per day).

- **(Optional) AI endpoints**:
  - REST endpoints to provide AI-driven recommendations, summaries, or search (see Section 11).

### 3.3 skilltrack-batch

Purpose: Implement batch jobs for importing course metadata and archiving/maintenance tasks.

Responsibilities:
- **Spring Batch configuration** for jobs such as:
  - Course metadata import from CSV (read → process → write pattern).
  - Archival of old user or enrollment data.

- **Job definition details**:
  - **Reader**: Flat file item reader for CSV input.
  - **Processor**: Validation, transformation, deduplication.
  - **Writer**: JPA or JDBC-based writer to PostgreSQL.
  - **Error handling**: Write failed records to a separate table and/or log.

- **Job control and execution**:
  - Jobs can be scheduled automatically (via scheduler) or manually triggered via API.
  - Job status, step execution details, and errors are stored in the database.

### 3.4 skilltrack-scheduler

Purpose: Execute scheduled tasks using Quartz Scheduler, with a persistent job store.

Responsibilities:
- Quartz configuration with database-backed job store.
- Definition of jobs for:
  - Daily email reminders for users with incomplete courses.
  - Periodic health checks, report generation jobs, or data cleanups.
- Exposing job history and status via REST APIs (possibly via `skilltrack-api` or a small admin interface).

### 3.5 Code Evaluation (Optional)

Purpose: Provide an optional, decoupled mechanism for **automatic code evaluation** used by coding challenges, without affecting core learning flows.

Approach options:
- Integrate with an external/open-source judge (e.g., a code execution API like Judge0) via REST.
- Or implement an internal “runner” service that compiles and runs submissions in sandboxed containers.

Key points:
- The main platform treats code evaluation as an **asynchronous service**: it submits code plus metadata, then polls or receives callbacks with results.
- Non-coding courses and simple quizzes do not depend on this service and continue to function if it is disabled.

### 3.6 AI Capabilities (Location)

AI-related components can be implemented in one of two ways:
- Inside `skilltrack-api` under a dedicated package (e.g., `ai` or `recommendation`).
- In a separate module (e.g., `skilltrack-ai`) if we want a clearer separation for deployment or scaling.

These components will encapsulate calls to Spring AI and external LLM providers.

## 4. Domain Model Overview

Key entities (conceptual):

- **User**
  - Fields: id, email, password hash, name, roles (Admin/Instructor/Student), emailVerificationStatus (e.g., PENDING, VERIFIED), instructorVerificationStatus (e.g., UNVERIFIED, VERIFIED), GitHub ID (optional), created/updated timestamps.
  - Relationships: many-to-many with `Role`, one-to-many with `Enrollment`.

- **Role**
  - Fields: id, name (e.g., ROLE_ADMIN, ROLE_INSTRUCTOR, ROLE_STUDENT), hierarchy.

- **Course**
  - Fields: id, title, description, difficulty, tags, estimated duration, published flag, soft-delete flag, ownerInstructorId (or owning team/account), created/updated timestamps.
  - Relationships: one-to-many with `CourseModule`; one-to-many with `Enrollment`.

- **CourseModule**
  - Fields: id, courseId, title, description, orderIndex, required flag, content metadata.

- **MediaAsset**
  - Fields: id, ownerUserId, type (video, document, image, other), storagePathOrKey, originalFilename, contentType, sizeBytes, createdAt.
  - Linked to courses or lessons via foreign keys or association tables.

- **Quiz**
  - Fields: id, courseId (or moduleId), title, description, passingScore, created/updated timestamps.
  - Relationships: one-to-many with `QuizQuestion`; one-to-many with `QuizAttempt`.

- **QuizQuestion**
  - Fields: id, quizId, questionText, type (MCQ single-choice, multiple-select, true/false, short text), orderIndex.
  - Relationships: one-to-many with `QuizOption` (for choice-based questions).

- **QuizOption**
  - Fields: id, questionId, optionText, isCorrect.

- **QuizAttempt**
  - Fields: id, quizId, userId, score, passedFlag, startedAt, completedAt, rawResponsePayload (for audit/debug).

- **Enrollment**
  - Fields: id, userId, courseId, status (Not Started, In Progress, Completed, Archived), progress percentage, timestamps.

- **CodingChallenge** (optional)
  - Fields: id, courseId (or moduleId), title, description, starterCode, language, difficulty, created/updated timestamps.

- **CodingSubmission** (optional)
  - Fields: id, challengeId, userId, submittedCode, language, status (QUEUED, RUNNING, PASSED, FAILED, ERROR), executionResult (stdout/stderr), createdAt, completedAt.

- **CourseReview**
  - Fields: id, courseId, userId, rating (1–5), comment, visibleFlag (for moderation), createdAt, updatedAt.
  - Only users with an enrollment for the course can create or update a review.

- **CertificateMetadata** (or fields on Enrollment)
  - Fields: id (or enrollmentId), certificateNumber, generatedAt, fileLocationKey.
  - Used to generate or retrieve a downloadable PDF certificate for completed courses.

- **Notification**
  - Fields: id, userId, type (reminder, completion, system), payload, status (pending, sent, failed), timestamps.

- **AuditRevision / AuditLog (via Envers)**
  - Captures versions of audited entities (user, course, enrollment, etc.).

- **JobHistory / JobExecution**
  - Captures Spring Batch and Quartz job executions.

The actual schema will be derived using JPA/Hibernate with annotations and possibly Flyway/Liquibase for migrations.

## 5. Persistence and Data Management

- **JPA/Hibernate**
  - Use Spring Data JPA repositories.
  - Utilize entity graphs to control lazy vs. eager loading for performance-sensitive queries.
  - Implement soft deletes for `Course` and possibly other entities using a logical deletion flag.
  - Enable second-level cache for read-heavy entities (e.g., course catalog) if beneficial.

- **Auditing**
  - Use Hibernate Envers to track create/update/delete operations for key entities.
  - Provide APIs to query audit history where needed.

- **Database**
  - PostgreSQL as the main DB in all non-test environments.
  - H2 or Testcontainers-managed PostgreSQL in tests.

- **Migrations (suggested improvement)**
  - Use Flyway or Liquibase for schema migrations to ensure repeatable deployments.

## 6. Security Design

- **Authentication**
  - Email/password login with BCrypt password hashing.
  - GitHub OAuth2 login via Spring Security OAuth2 Client.
  - Email verification flow where new accounts receive a verification token/link and remain limited or inactive until confirmed.

- **Authorization**
  - Role-based access control with role hierarchy (Admin > Instructor > Student).
  - Method-level security annotations for service-layer restrictions.
  - Endpoint protection via HTTP security config (e.g., `/admin/**` restricted to Admin).

- **Session Management**
  - Stateless sessions using JWT for API authentication.
  - Token issuing and refresh endpoints as needed.

- **Security Hardening (suggested improvements)**
  - Account lockout or throttling on repeated failed logins.
  - CSRF protection for any browser-based usage (if relevant).
  - Centralized error handling to avoid leaking sensitive information.

## 7. Integration and Messaging

- **GitHub OAuth2**
  - Use Spring Security OAuth2 Client to handle the authorization code flow.
  - Map GitHub users to SkillTrack users (linking by email or explicit mapping).

- **Email (SendGrid or similar)**
  - Use JavaMailSender or a dedicated SendGrid integration.
  - Abstract email sending behind a service interface so providers can be swapped.
  - Used for registration verification emails, password reset, notifications, and optional course-related updates.

- **AI Provider (for Spring AI)**
  - Integrate with an LLM provider (e.g., OpenAI, Azure OpenAI, or others) via Spring AI abstractions.
  - Configure models, prompts, and rate limits centrally.

- **Async events (Domain events)**
  - Use Spring events or messaging (e.g., `ApplicationEventPublisher`) for decoupling side-effects such as notifications from core write operations.

## 8. Caching and Performance

- **Caching**
  - Use Spring Cache abstraction with Caffeine (in-memory) or Redis for distributed caching.
  - Cache read-heavy data: course details, instructor profiles, common lookups.
  - Implement cache invalidation on entity updates via annotations or explicit cache eviction.
  - Make caching toggleable via Spring Profiles.

- **Performance considerations**
  - Use pagination for list endpoints.
  - Apply entity graphs and projections to reduce N+1 queries.
  - Monitor slow queries and tune indexes.

## 9. Scheduling and Background Processing

- **Spring Batch**
  - For heavy import/export/archival tasks, using chunk-oriented batch jobs.

- **Quartz Scheduler**
  - For recurring operations such as email reminders.
  - Store Quartz jobs and triggers in PostgreSQL for persistence and clustering readiness.
  - Provide APIs to list, pause, resume, and query history of scheduled jobs.

- **Interaction between Batch and Scheduler**
  - Quartz triggers may start Spring Batch jobs (e.g., nightly import or daily summary generation).

## 10. Observability and Operations

- **Actuator endpoints**
  - `/actuator/health`, `/actuator/metrics`, `/actuator/loggers`, etc.
  - Custom health indicators for external dependencies (DB, Redis, email provider, AI provider).

- **Metrics**
  - Technical metrics: JVM, DB, HTTP request latencies.
  - Business metrics: number of completed enrollments per day, active users, etc.
  - Export to Prometheus via Micrometer registry; visualize with Grafana (optional).

- **Logging and Tracing (suggested improvement)**
  - Structured logging (JSON) for easier analysis.
  - Optional distributed tracing with OpenTelemetry if deployed in a distributed environment.

## 11. AI Feature Design with Spring AI (Proposed)

Spring AI can be used to integrate LLM-based capabilities into SkillTrack. Technically, this involves defining AI services that orchestrate prompts, context, and provider calls.

### 11.1 AI Use Cases

- **Content Summaries (MVP)**
  - Service that uses Spring AI to generate concise summaries of course descriptions and lesson content.
  - Exposed via REST endpoints (e.g., `/courses/{id}/summary`, `/lessons/{id}/summary`) and consumed by the Angular course player.
  - Summaries are generated on first request and cached in the database to avoid repeated AI calls.

- **Personalized Recommendations (future)**
  - Service that takes user profile, history, and catalog metadata as input and returns ranked course recommendations.
  - Uses vectorization or metadata-based context, combined with an LLM prompt.

- **Natural-Language Search and Q&A (future)**
  - Endpoint that accepts free-text queries and translates them into structured filters or semantic matches against the course catalog.

### 11.2 Technical Components

- **Spring AI configuration**
  - Define one or more `ChatClient` or `AiClient` beans pointing to the provider.
  - Configure model names, temperature, and safety/guardrail policies.

- **Prompt templates and context builders**
  - Store prompt templates for each use case (recommendation, summary, search) and dynamically inject user/catalog context.

- **Caching and rate limiting**
  - Cache AI responses for deterministic queries (e.g., course summaries) to avoid repeated calls and reduce cost.
  - Implement rate limiting and backoff to handle provider quotas.

- **Safety and privacy**
  - Ensure prompts do not leak sensitive internal information beyond what is necessary.
  - Consider anonymizing user identifiers in prompts.

## 12. Testing Strategy

- **Unit tests**
  - For services, domain logic, and utilities.

- **Integration tests**
  - Use JUnit 5 and Testcontainers (PostgreSQL, Redis) to validate:
    - Authentication flows (email/password, JWT, optionally OAuth2).
    - Course and enrollment CRUD.
    - Spring Batch job execution (import job behavior, failure handling).
    - Quartz scheduling triggers and effects (e.g., reminder generation).

- **Contract / API tests (suggested improvement)**
  - Validate API contracts using OpenAPI-generated tests or consumer-driven contract tests.

- **AI feature tests**
  - Where possible, use deterministic prompts or mock AI endpoints.
  - Validate that prompts and contexts are formed correctly and that fallbacks behave as expected.

## 13. Local and Deployment Topology

- **Local development**
  - Run backend via `./mvnw spring-boot:run` or IDE; run Angular dev server via `ng serve` during development.
  - Use Docker Compose to start separate containers for the backend API, Angular frontend, PostgreSQL, and Redis.

- **Environments**
  - Use Spring Profiles for `dev`, `test`, and `prod`.
  - Configure environment-specific DB, caching, and external integrations.
  - In higher environments, PostgreSQL and Redis may be provided as managed services instead of Docker containers, configured via environment variables (e.g., `SPRING_DATASOURCE_URL`, `SPRING_REDIS_HOST`).

- **Packaging and deployment**
  - Each module packaged as an executable JAR or container image.
  - Docker images orchestrated via Docker Compose locally; Kubernetes or similar in higher environments (future).

## 14. Non-Functional Requirements (Technical View)

- **Performance**
  - APIs should respond within acceptable time for typical workloads (e.g., < 300 ms for simple reads under normal load).

- **Scalability**
  - Stateless API nodes with shared database and cache, enabling horizontal scaling.

- **Reliability**
  - Health checks, graceful shutdown, and retry strategies for external dependencies.

- **Security**
  - Strong password hashing, secure defaults in Spring Security, regular updates of dependencies.

- **Maintainability**
  - Clear module boundaries, consistent coding standards, comprehensive automated tests.

## 15. Open Technical Questions

- Should AI-related capabilities be deployed as part of `skilltrack-api` or as a separate `skilltrack-ai` service/module for isolation and scaling?
- What is the expected scale (number of users, courses, requests per second) to size database, cache, and infrastructure?
- Are there specific compliance requirements (e.g., data residency, retention policies) that affect data modeling and logging?
- Should we standardize on Flyway or Liquibase for schema migrations from the start?
