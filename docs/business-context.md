# SkillTrack – Business Domain Context

## 1. Vision and Purpose

SkillTrack is a public learning management platform focused on helping learners continuously improve their skills. While the initial focus is on software engineers (Java, Spring, and related technologies), the platform is intentionally **generic** so that it can support non-programming courses as well. The platform enables any user on the internet to sign up, discover and enroll in curated courses, complete assessments (e.g., quizzes and coding challenges), and track their learning progress over time.

The primary purpose is not just to host content, but to:
- Drive structured upskilling for developers.
- Provide visibility into learning progress for teams and leadership.
- Support compliance with internal training requirements.
- Create a feedback loop between learning outcomes and engineering effectiveness.

The primary delivery channel is a web application built with **Angular**, providing:
- A learner experience for browsing, enrolling in, and taking courses.
- An instructor console for course authoring and assessment management.
- An admin console for user, content, and platform governance.

## 2. Stakeholders and Personas

- **Learner (Student)**
  - Signs up publicly to consume courses and learning paths.
  - Completes course content and assessments (quizzes, and optionally coding challenges where applicable).
  - Tracks personal progress and achievements.

- **Instructor / Content Owner**
  - Signs up publicly and indicates they want an instructor account.
  - Designs and publishes courses and modules (subject to verification status).
  - Updates course content over time.
  - Reviews learner progress and feedback on their own courses.

- **Administrator**
  - Manages users, roles, and permissions at the platform level.
  - Reviews and verifies instructor accounts (e.g., identity or quality checks).
  - Manages catalog metadata (tags, categories, difficulty levels).
  - Oversees operational aspects: batch jobs, schedules, notifications.

- **Engineering Manager / Program Owner**
  - Defines learning goals for teams (e.g., "all backend engineers complete Spring Security track").
  - Monitors completion rates, engagement, and outcomes.
  - Uses reports to support performance, promotion, or compliance processes.

## 3. Business Goals and Outcomes

- **Reduce time-to-productivity** for new engineers by providing structured onboarding learning paths.
- **Increase adoption of internal standards and best practices** (e.g., Spring Boot patterns, security guidelines) via targeted learning.
- **Improve visibility** into who has completed required trainings (e.g., security, compliance, platform changes).
- **Encourage continuous learning** by providing clear progress tracking and reminders.
- **Measure learning impact** through metrics like completion rates, active learners, and course effectiveness.

## 4. Scope Overview

In scope for the initial version:
- Backend APIs and services for public user registration, login, courses, enrollments, progress tracking, notifications, and reporting.
- Simple role model where any user can sign up as a Student or Instructor.
- An instructor verification process, where newly registered instructors start as **unverified** and must be reviewed/approved by an Admin before being fully trusted.
- Batch processing of course metadata and archival data.
- Scheduled reminders for incomplete courses.
- Auditing of key entity changes and integration with email notifications.
- Observability for health, metrics, and job status.

Out of scope for the initial version (but possible future extensions):
- Rich front-end learning experiences (video player, interactive coding IDE in the browser).
- Payment processing, billing, or subscription management.
- Organization/tenant support and enterprise account hierarchy.

## 5. Core Business Capabilities

### 5.1 User and Identity Management

- Allow users to sign up with email/password.
- Allow existing users to sign in using GitHub OAuth for convenience and integration with engineering workflows.
- During sign-up, allow users to choose whether they want a **Student** or **Instructor** account type (or both, depending on UX).
- Maintain user profiles including basic details, skills/interests, and roles (Admin, Instructor, Student), as well as an instructor **verification status**.
- Support stateless, token-based sessions to simplify scaling and deployment.

Email verification:
- Newly registered users are created in a **pending email verification** state.
- The platform sends a verification email with a one-time link or code.
- Accounts become fully active only after the user verifies their email.

Instructor account verification:
- Newly registered instructors start as **unverified** instructors.
- Admins can review and verify instructor accounts (e.g., checking profile information, sample content, or other criteria).
- Instructor profile status should clearly indicate whether an instructor is verified or unverified.
- Unverified instructors can still create and publish courses that are visible to learners, but the platform clearly labels these courses and instructor profiles as "unverified" so learners can make informed decisions.

### 5.2 Learning Catalog Management

- Maintain a catalog of courses, each with metadata such as title, description, difficulty level, tags, estimated time, and prerequisites.
- Each course consists of one or more modules (e.g., lessons, units, chapters).
- Support marking courses/modules as draft vs. published.
- Allow instructors to create and update their own courses and maintain version history through auditing.

Course content and media:
- Lessons can include rich text content, embedded media, and links.
- Instructors can **upload files** (e.g., PDFs, images, slide decks, videos) to be hosted by the platform, in addition to referencing external links.
- The platform enforces basic constraints on uploaded files (type, size) and associates them with courses or lessons.

### 5.3 Enrollment and Participation

- Allow learners to browse available courses and enroll.
- Allow learners to un-enroll (subject to business rules) if no longer relevant.
- Track enrollment state (e.g., Not Started, In Progress, Completed, Archived).
- Associate learners' progress with individual modules and overall course completion.

### 5.4 Progress Tracking and Achievements

- Track completed modules and completion percentage for each course.
- Derive course completion based on completion of all required modules.
- Optionally track time spent or completion timestamps for analytics and reporting.
- Provide learners with a view of current enrollments, historical completions, and achievements.

Completion certificates:
- When course completion criteria are met, the platform generates a **downloadable completion certificate** (PDF) for the learner.
- Certificates include learner name, course title, instructor name, completion date, and a course-specific identifier.
- Certificates are private to the learner and available for download from their learning history or the completed course page.

### 5.5 Assessments (Quizzes and Coding Challenges)

- Support **simple quizzes** (e.g., multiple-choice questions, single-choice, true/false, short text answers) associated with courses or modules.
- Allow instructors to define quiz questions, correct answers, and basic scoring rules.
- Track quiz attempts and scores as part of the learner’s course progress.
- Support optional **coding challenges** for programming-focused courses, modeled as a separate feature that can be enabled without impacting non-coding courses.
- For coding challenges, completion is based on passing automated evaluations (where that feature is enabled) or simpler self-assessed criteria in the absence of an evaluator.

Quizzes are **single-attempt** in the initial version: each learner can attempt a given quiz once, and that score is used for pass/fail and course completion.

### 5.6 Communication and Notifications

- Send reminder emails to learners about:
  - Courses they enrolled in but have not started.
  - Courses in progress with long inactivity.
  - Approaching due dates for mandatory trainings (if defined).
- Notify instructors or admins when important events occur (e.g., batch import failures, high number of failed enrollments).
- Provide hooks to trigger notifications after significant entity changes (e.g., course updated, enrollment completed).

### 5.7 Reporting and Insights

- Provide APIs to answer questions such as:
  - How many enrollments were completed per day/week/month?
  - Which courses are most/least popular?
  - Which learners have outstanding mandatory courses?
- Provide health and operational reports about batch jobs, scheduled jobs, and notification delivery.

### 5.8 Governance, Compliance, and Security

- Ensure that only authorized users can access sensitive data (e.g., admin endpoints, enrollment data across teams).
- Provide audit trails for key business entities (users, courses, enrollments, notifications).
- Support data retention policies for user and learning data (e.g., anonymization or archival after a certain period).

### 5.9 AI-Enhanced Learning (Proposed)

(See Section 9 for details.)

## 6. Key Business Processes / User Journeys

### 6.1 Developer Onboarding Journey

1. A new developer joins the company.
2. The admin (or HR/manager) registers the developer or invites them to self-register.
3. The developer logs in (email/password or GitHub) and sees recommended onboarding tracks.
4. The developer enrolls in a curated set of onboarding courses.
5. The system sends reminders until mandatory onboarding courses are completed.
6. Managers can verify that the developer completed the onboarding track.

### 6.2 Self-Directed Learning Journey

1. A developer wants to deepen knowledge in a specific topic (e.g., Spring Security).
2. They browse or search the catalog using filters (topic, difficulty, duration).
3. They enroll in one or more courses.
4. They complete modules at their own pace; progress is tracked and visible.
5. Periodic reminders nudge the developer to continue where they left off.
6. Completion is reflected in reports and possibly in their internal skill profile.

### 6.3 Course Creation and Maintenance Journey

1. An instructor designs a new course outline and defines modules.
2. They create the course in the platform (draft state), including metadata and modules.
3. After review, they publish the course, making it discoverable in the catalog.
4. Over time, they update content; changes are audited for traceability.
5. They review reports on enrollments, completion rates, and learner feedback to refine content.

### 6.4 Compliance / Mandatory Training Journey

1. An admin defines a set of mandatory courses for a particular audience (e.g., all backend developers).
2. Targeted developers are enrolled or prompted to enroll.
3. The scheduler sends reminders before a due date.
4. Reports show who is compliant/non-compliant.
5. After the period ends, data can be archived or kept for audit/compliance needs.

## 7. Roles and Access Rules (Business View)

- **Student**
  - Can view published courses and enroll.
  - Can view and update their own profile and learning data.
  - Cannot modify courses or other users.

- **Instructor**
  - All Student capabilities.
  - Can create and manage courses and modules they own.
  - Can view aggregate stats for their courses.
  - Have a **verification status** (unverified or verified) that affects how their profile and courses are labeled and may influence search/recommendation ranking.

- **Admin**
  - Manages users, roles, and courses at the platform level.
  - Reviews and changes instructor verification status.
  - Can configure system-wide settings (e.g., reminder frequencies, feature flags).
  - Can access all reports and operational dashboards.

## 8. Policies and Business Rules

- Passwords must adhere to an internal password policy and be stored securely.
- Students cannot modify course structure or content.
- Only Admins can change user roles or grant Instructor/Admin status.
- Newly registered instructors are considered **unverified** until approved by an Admin, but can still publish courses.
- Courses created by unverified instructors, and their profiles, must be clearly labeled as "unverified" in the UI.
- Courses can only be enrolled if they are in published state and not soft-deleted.
- Course completion is defined as completion of all required modules.
- Soft deletes are used to hide deprecated courses while preserving historical data.
- Email notifications must respect opt-out and frequency limits where applicable.
- Audit logs for sensitive entities must not be tampered with and must be queryable for compliance.

## 9. Reporting, KPIs, and Success Metrics

Example KPIs for business stakeholders:

- Course-level metrics:
  - Enrollments over time.
  - Completion rate.
  - Average time-to-completion.

- User-level metrics:
  - Number of active learners per period.
  - Average number of courses completed per user.
  - Distribution of difficulty levels among completed courses.

- Program-level metrics:
  - Compliance percentage for mandatory tracks.
  - Adoption of newly introduced courses.
  - Drop-off points (modules where learners tend to stop).

## 10. AI-Enhanced Learning Capabilities (Proposed)

While not mandatory for the initial baseline, AI capabilities can significantly increase the value of the platform. Potential AI features include:

- **AI-generated summaries and highlights (MVP AI feature)**
  - Provide concise summaries of long courses or modules to help learners decide whether to enroll and to quickly review content.
  - Surface these summaries in the course and lesson views in the Angular UI.

- **Personalized course recommendations (future)**
  - Recommend courses based on a learner’s role, skills, past course history, and stated interests.
  - Suggest next steps (e.g., advanced courses after completing a foundational course).

- **Natural language course search and discovery (future)**
  - Allow learners to search the catalog using plain language queries (e.g., "show me beginner-friendly Spring Security courses").

- **AI feedback on coding challenges (optional, future)**
  - Provide qualitative, human-like feedback on code submissions, in addition to deterministic test-based evaluation.
  - Highlight improvement areas or suggest relevant course content based on weaknesses.

- **Engagement risk detection**
  - Identify learners who are likely to abandon courses (e.g., long inactivity, multiple unfinished courses) and trigger tailored nudges.

These features would be implemented using Spring AI and an underlying LLM provider, but they are positioned here as **business capabilities**: personalization, better discovery, and richer feedback.

## 11. Risks and Constraints (Business View)

- **Data privacy and compliance**
  - Learning history and performance data may be considered sensitive.
  - If external AI services are used, prompt and response data must be carefully controlled.

- **Change management and adoption**
  - Success depends on good content and organizational buy-in, not just the platform itself.

- **Content quality**
  - Poorly designed courses will undermine the value of the system, regardless of technical robustness.

## 12. Open Questions (for Clarification)

- Are there defined **mandatory training programs** or learning paths that must be explicitly modeled as first-class objects (tracks, programs)?
- Should the system support **feedback and rating** of courses by learners (e.g., 1–5 stars, comments) as part of the MVP, or is this a later enhancement?
