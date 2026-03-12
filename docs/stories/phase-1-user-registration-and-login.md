# SkillTrack – Phase 1 Feature Story – User Registration and Login

## 1. Business Context

SkillTrack is a public learning management platform focused on helping engineers and other learners continuously upskill. A core foundation for all other capabilities is the ability for users to register, authenticate, and access the platform as either learners (students) or instructors.

In Phase 1, the goal is to provide a simple, frictionless way for new users on the internet to:

- Create an account with minimal required information.
- Choose whether they want to use the platform as a student or an instructor.
- Sign in using email/password or their GitHub account.
- Access the course catalog immediately in a limited state, and unlock full participation once their email is verified (for email/password accounts).

Admin users are not created via self-service registration; they are provisioned through an internal backend process and are outside the scope of this feature, except that the system must correctly recognise admin accounts created elsewhere during login.

This feature directly supports the broader business goals of enabling self-directed learning and instructor-led content creation by ensuring that the right personas can reliably and easily get into the system.

## 2. Story Text

**Primary Story**

As a learner or instructor using SkillTrack,
I want to register and sign in using email/password or my GitHub account,
so that I can access the course catalog, manage my learning or teaching activities, and participate in courses once my account is verified.

## 3. Acceptance Criteria

### 3.1 Email/Password Registration with Role Selection

**AC1 – Successful registration with minimal mandatory fields**

- Given I am a new user on the public registration page,
  And I provide my full name, a unique email address, a password that meets the configured policy, and a selected role (student or instructor) where student is pre-selected by default,
  And country and organisation fields are optional,
  When I submit the registration form,
  Then my account is created in a pending email verification state,
  And I receive a clear confirmation message that a verification email has been sent,
  And I can proceed to sign in with limited access as described in later criteria.

**AC2 – Duplicate email address is rejected**

- Given an account already exists with a specific email address,
  When a new user attempts to register with the same email,
  Then the registration is rejected,
  And I see a friendly message indicating that the email is already registered and suggesting that I sign in or use password reset.

**AC3 – Instructor registration captured as unverified instructor**

- Given I select the instructor role during registration,
  When my account is created,
  Then my role is recorded as instructor,
  And my instructor status is marked as unverified,
  And the system is ready to apply instructor-specific behaviour in later features (such as course creation and instructor verification).

### 3.2 Email Verification and Limited Access

**AC4 – Verification email with time-limited link**

- Given I successfully register with email and password,
  When my account is created,
  Then the platform sends a verification email to the address I provided,
  And the email contains a single-use verification link that expires after a defined time window (for example, 1 hour),
  And the email content clearly explains that verification is required to fully activate my account.

**AC5 – Successful email verification activates the account**

- Given I have received a valid verification email,
  When I click the verification link before it expires,
  Then my account status is updated from pending verification to verified,
  And I see a confirmation message that my email has been verified,
  And from this point onward I can fully use the platform according to my role (for example, students can enroll in courses, instructors can later create courses in future features).

**AC6 – Expired or invalid verification link shows clear guidance**

- Given my verification link has expired or has already been used,
  When I click the link,
  Then I see a clear message that the verification link is no longer valid,
  And I am provided with a simple option to request a new verification email.

**AC7 – Resend verification from the application**

- Given I have an account in pending email verification state,
  And I am signed in with email and password,
  When I choose to resend the verification email from within the application,
  Then a new verification email is sent to my registered address,
  And I see a confirmation message that the email has been resent,
  And previously issued verification links that are no longer valid do not interfere with the new verification.

**AC8 – Limited access for unverified accounts**

- Given I have an account that is pending email verification,
  And I sign in with valid email and password,
  When I access the application,
  Then I can browse the public course catalog and view high-level course details,
  But I cannot enroll in any course or perform actions that require a fully verified account,
  And I see prominent but non-blocking prompts reminding me to verify my email.

**AC9 – Enrollment blocked for unverified accounts**

- Given I am signed in with an unverified account (student or instructor),
  When I attempt to enroll in a course,
  Then the enrollment does not proceed,
  And I see a clear message indicating that I must first verify my email to enroll in courses,
  And a shortcut is provided to resend the verification email if needed.

### 3.3 Email/Password Login and Logout

**AC10 – Successful login with verified or unverified account**

- Given I have registered with email and password,
  And I provide the correct email and password on the login screen,
  When I submit the login form,
  Then I am signed in successfully,
  And my post-login experience reflects my account state:
    - If my email is verified, I have full access according to my role.
    - If my email is not yet verified, I have limited access as described in the limited access criteria.

**AC11 – Validation of incorrect credentials**

- Given I am on the login screen,
  When I enter an email address that does not exist or a wrong password for an existing account,
  Then I am not signed in,
  And I see a clear, generic error message that does not reveal which part of the credentials is incorrect,
  And I am encouraged to try again or use the password reset option.

**AC12 – Prompt to reset password after repeated failures**

- Given I repeatedly attempt to log in with incorrect credentials,
  When the number of consecutive failures crosses a reasonable threshold,
  Then I see a stronger prompt to use the password reset flow,
  And further attempts remain possible but clearly emphasise account recovery as the recommended next step.

**AC13 – Logout**

- Given I am signed in (with any role or verification status),
  When I choose to sign out from the application,
  Then my active session is ended,
  And I am redirected to a public view such as the home or login page,
  And subsequent protected actions require me to sign in again.

### 3.4 Forgot Password / Reset Password

**AC14 – Initiate password reset**

- Given I have an existing email/password account,
  And I am unable to remember my password,
  When I use the "forgot password" option and provide my registered email address,
  Then I see a confirmation message that a password reset email has been sent (if the email exists),
  And I do not see any information that confirms whether the email is registered or not.

**AC15 – Password reset email with time-limited link**

- Given I have requested a password reset,
  When the system processes the request,
  Then I receive an email containing a secure, time-limited link to reset my password,
  And the email clearly explains that the link will expire after a defined time window (for example, 1 hour).

**AC16 – Successful password change and future logins**

- Given I click a valid, non-expired password reset link,
  When I provide a new password that meets the configured password policy and confirm it,
  Then my password is updated,
  And I see a confirmation message that my password has been changed,
  And future logins with my email require the new password.

**AC17 – Handling expired or invalid reset links**

- Given my password reset link has expired or has already been used,
  When I click the link,
  Then I see a clear message that the reset link is no longer valid,
  And I am directed to initiate a new password reset request if I still need to change my password.

### 3.5 GitHub OAuth Sign-In and Profile Completion

**AC18 – First-time GitHub sign-in creates a new account**

- Given I am a new user and choose to sign in with my GitHub account,
  And GitHub successfully authenticates me and returns me to SkillTrack,
  When I arrive back on SkillTrack for the first time,
  Then I am taken to a simple profile completion screen,
  And my full name and email are pre-filled where available from GitHub,
  And the role selector is shown with student pre-selected by default but allowing me to choose instructor instead if desired,
  And I can optionally provide country and organisation.

**AC19 – Completing profile after GitHub sign-in**

- Given I have just returned from GitHub authentication for the first time,
  And I am on the profile completion screen,
  When I confirm my details and submit the form,
  Then a new SkillTrack account is created and associated with my GitHub identity,
  And I am signed in to the application,
  And I can immediately access the application according to my chosen role.

**AC20 – Returning GitHub users sign in seamlessly**

- Given I have previously completed my profile after signing in with GitHub,
  When I later choose to sign in with the same GitHub account again,
  And GitHub successfully authenticates me,
  Then I am signed in to my existing SkillTrack account without needing to complete my profile again,
  And I land in the appropriate post-login experience for my role.

**AC21 – Behaviour for GitHub-based accounts**

- Given my account was created through GitHub sign-in,
  When I access the application after signing in successfully,
  Then I am treated as having a trusted identity based on my GitHub account,
  And I can access the platform according to my role without requiring a separate email verification step.

## 4. Out of Scope

- Self-service registration or conversion to admin accounts; admin accounts are created and managed through internal processes.
- Multi-factor authentication or additional strong authentication methods.
- Integration with social login providers other than GitHub.
- Detailed management of user profiles beyond the minimal fields specified (for example, advanced skills, extensive biography, profile pictures).
- Complex session management features such as remembered devices, cross-device session listings, and explicit session revocation tools.
- Organisation- or tenant-specific registration workflows, enterprise onboarding, or invitation-based flows.
- Course enrollment logic beyond the simple rule that unverified users cannot enroll.
- Platform-wide non-functional requirements such as detailed security hardening, rate limiting, and global performance targets, which will be addressed at the application level.

## 5. Dependencies

- Availability of an email delivery mechanism to send verification and password reset emails, including basic observability for failures.
- Configuration of a GitHub OAuth application and basic trust relationship so that SkillTrack can accept GitHub-based sign-ins.
- A role and authorisation model that distinguishes between student, instructor, and admin roles.
- A minimal course catalog experience that allows browsing published courses without enrollment.
- A shared design system or UX guidelines for form layouts, validation messages, and error states to keep the registration and login experience consistent with the rest of the application.

## 6. Assumptions

- Email addresses are the unique public identifier for accounts in the system.
- Email/password and GitHub sign-in can coexist for the same person, but each login method is treated consistently within this feature.
- Verification and reset links use a standard expiry time window (for example, 1 hour) that can be fine-tuned later without changing the user-facing behaviour described here.
- Users primarily access SkillTrack through modern web browsers; mobile responsiveness is desirable but a dedicated mobile app is not required for this phase.
- Platform-wide non-functional requirements (for example, security baselines, logging, monitoring, and performance budgets) are documented separately and applied across features.

## 7. Mockups and Supporting Documents

The following artefacts are recommended to support implementation and alignment across teams:

- Low-fidelity wireframes for:
  - Registration page with minimal mandatory fields and role selection.
  - Login page showing both email/password and GitHub sign-in options.
  - Limited-access landing view for unverified accounts with clear prompts.
  - Profile completion screen after first-time GitHub sign-in.
  - Forgot password and reset password screens.
- Example email templates for:
  - Email verification (including link expiry notice).
  - Password reset (including link expiry notice).
- A simple flow diagram or sequence diagram covering:
  - Email/password registration and verification.
  - Email/password login (verified vs unverified paths).
  - Forgot password / reset password.
  - GitHub OAuth sign-in and first-time profile completion.

These artefacts can be created in the design tooling preferred by the team (for example, a shared design system or diagramming tool) and linked to this story document for reference.
