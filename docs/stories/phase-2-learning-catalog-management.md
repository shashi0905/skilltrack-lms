# Phase 2: Learning Catalog Management

## Business Context

Instructors are subject matter experts who want to share their knowledge and help others develop new skills. Currently, they can register and log into SkillTrack, but they have no way to create and organize their learning content. Without the ability to create courses, add structured modules and lessons, and publish their content, instructors cannot fulfill their primary purpose on the platform - teaching and enabling learner growth.

This feature enables instructors to create comprehensive courses, structure them into modules and lessons, enrich them with media content, and control when their courses become visible to learners. The iterative workflow allows instructors to work on courses over multiple sessions, saving progress as drafts, and only publishing when they're confident the content meets their quality standards.

## Story

**As an** instructor  
**I want to** create, structure, and publish courses with modules, lessons, and media content  
**So that** I can share my expertise with learners and help them develop new skills through well-organized learning experiences

## Acceptance Criteria

### AC1: Create a New Course (Draft Mode)
**Given** I am a logged-in instructor  
**When** I create a new course by providing title, description, difficulty level, tags, and estimated duration  
**Then** the course is saved in draft mode and is not visible to learners  
**And** I can see the course in my instructor dashboard with a "Draft" status indicator

### AC2: Add Modules to a Course
**Given** I have created a course in draft mode  
**When** I add one or more modules by providing title and description for each module  
**Then** the modules are saved and associated with the course  
**And** I can see all modules listed within the course in the order I created them

### AC3: Add Lessons to Modules
**Given** I have created at least one module in a course  
**When** I add one or more lessons to a module by providing title, description, and content  
**Then** the lessons are saved and associated with the module  
**And** I can see all lessons listed within the module in the order I created them

### AC4: Upload and Attach Media to Course Content
**Given** I am editing a course, module, or lesson  
**When** I upload media files (PDFs, images, videos, documents)  
**Then** the files are stored securely and associated with the respective course element  
**And** I can see the uploaded media displayed in the course content  
**And** the system enforces file type and size restrictions

### AC5: Edit Course, Module, or Lesson Content (Draft Changes)
**Given** I have a course in any state (draft or published)  
**When** I edit the course details, module content, or lesson content  
**Then** the changes are saved in draft mode  
**And** if the course is published, learners continue to see the previously published version until I publish the updates

### AC6: Publish a Course for the First Time
**Given** I have a course in draft mode with at least one module containing at least one lesson  
**When** I choose to publish the course  
**Then** the course becomes visible to learners in the course catalog  
**And** the course status changes from "Draft" to "Published"  
**And** learners can browse and enroll in the course

### AC7: Prevent Publishing Empty or Incomplete Courses
**Given** I have a course in draft mode that has no modules or has modules with no lessons  
**When** I attempt to publish the course  
**Then** the system prevents publication and displays an error message  
**And** the message indicates that the course must have at least one module with at least one lesson

### AC8: Publish Updates to an Already Published Course
**Given** I have a published course with draft changes (edits made after initial publication)  
**When** I choose to publish the updates  
**Then** the draft changes become the new published version visible to learners  
**And** the course remains in "Published" status  
**And** learners now see the updated content

### AC9: View Course in Instructor Dashboard
**Given** I am a logged-in instructor  
**When** I view my instructor dashboard  
**Then** I can see all courses I have created  
**And** each course displays its current status (Draft or Published)  
**And** I can identify which published courses have unpublished draft changes

### AC10: Delete Draft Courses
**Given** I have a course in draft mode that has never been published  
**When** I choose to delete the course  
**Then** the course and all its modules, lessons, and associated media are removed from the system  
**And** the course no longer appears in my instructor dashboard

## Out of Scope

- Instructor verification status and labeling of courses from unverified instructors (separate story)
- Course ratings and reviews by learners
- Course prerequisites and learning path dependencies
- Versioning history or rollback to previous published versions
- Collaborative course authoring (multiple instructors on one course)
- Course duplication or templates
- Bulk import of course content
- Advanced media features (video transcoding, streaming optimization)
- Course analytics and learner progress visibility for instructors
- Soft delete for published courses (archival)
- Reordering of modules and lessons after creation
- Course categories or organizational taxonomy beyond tags

## Dependencies

- Phase 1: User Registration & Login must be completed
- Instructors must have the INSTRUCTOR role assigned during registration or by an admin
- Media storage infrastructure must be available (file system or cloud storage like S3)
- Database schema for courses, modules, lessons, and media assets must be implemented

## Assumptions

- Instructors have basic technical literacy to upload files and structure content
- File upload limits (size and type) are defined and enforced at the application level
- The system supports common media formats (PDF, JPG, PNG, MP4, etc.)
- Course content is primarily text-based with supplementary media attachments
- Modules and lessons are created in a linear sequence (ordering can be enhanced later)
- Only the course owner (instructor who created it) can edit or publish the course
- Published courses remain accessible to already-enrolled learners even if the instructor makes draft changes
- The platform has sufficient storage capacity for uploaded media files
- Media files are scanned for security threats before storage (antivirus/malware scanning)

## Non-Functional Requirements (NFRs)

The following NFRs are relevant to this feature:

### Performance
- Course creation and updates should complete within 2 seconds under normal load
- Media file uploads should support files up to 100MB with progress indication
- Course listing in instructor dashboard should load within 1 second for up to 100 courses

### Security
- Only authenticated instructors can create and manage courses
- Instructors can only edit/delete their own courses
- Uploaded media files must be validated for type and scanned for malware
- Media files should be stored with access controls to prevent unauthorized access

### Usability
- The course creation workflow should be intuitive with clear guidance on required fields
- Error messages for validation failures should be specific and actionable
- Draft auto-save should occur to prevent data loss during editing sessions

### Data Integrity
- Course, module, and lesson relationships must maintain referential integrity
- Deletion of a course should cascade to remove all associated modules, lessons, and media
- Published course content must remain consistent and available to learners during instructor edits

### Scalability
- The system should support up to 10,000 courses without performance degradation
- Media storage should scale to accommodate growing content library

---

## Question for Stakeholder

**Should the NFRs listed above be:**
1. **Included as additional acceptance criteria** in this story (making it a more comprehensive feature story with quality attributes)?
2. **Separated into one or more dedicated NFR stories** to be prioritized and implemented independently?

Please advise on your preference so we can finalize the story accordingly.

---

## Supporting Documentation

### Sequence Diagram: Course Creation and Publishing Flow

```
Instructor -> API: POST /api/courses (title, description, difficulty, tags, duration)
API -> Database: Insert course record (status=DRAFT)
Database -> API: Course created
API -> Instructor: Course ID, status=DRAFT

Instructor -> API: POST /api/courses/{id}/modules (title, description)
API -> Database: Insert module record
Database -> API: Module created
API -> Instructor: Module ID

Instructor -> API: POST /api/modules/{id}/lessons (title, description, content)
API -> Database: Insert lesson record
Database -> API: Lesson created
API -> Instructor: Lesson ID

Instructor -> API: POST /api/courses/{id}/media (file upload)
API -> Storage: Store media file
Storage -> API: File URL/path
API -> Database: Insert media record
Database -> API: Media created
API -> Instructor: Media ID, URL

Instructor -> API: POST /api/courses/{id}/publish
API -> Database: Check course has ≥1 module with ≥1 lesson
Database -> API: Validation passed
API -> Database: Update course status=PUBLISHED
Database -> API: Course published
API -> Instructor: Course status=PUBLISHED

Learners -> API: GET /api/courses (browse catalog)
API -> Database: Fetch published courses
Database -> API: Course list
API -> Learners: Display published courses
```

### Entity Relationship Diagram

```
┌─────────────────┐
│     Course      │
├─────────────────┤
│ id (PK)         │
│ instructor_id   │
│ title           │
│ description     │
│ difficulty      │
│ tags            │
│ duration        │
│ status          │ (DRAFT, PUBLISHED)
│ published_ver   │ (version number or snapshot reference)
│ draft_ver       │ (version number or snapshot reference)
│ created_at      │
│ updated_at      │
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐
│  CourseModule   │
├─────────────────┤
│ id (PK)         │
│ course_id (FK)  │
│ title           │
│ description     │
│ order_index     │
│ created_at      │
│ updated_at      │
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐
│     Lesson      │
├─────────────────┤
│ id (PK)         │
│ module_id (FK)  │
│ title           │
│ description     │
│ content         │
│ order_index     │
│ created_at      │
│ updated_at      │
└─────────────────┘

┌─────────────────┐
│   MediaAsset    │
├─────────────────┤
│ id (PK)         │
│ course_id (FK)  │ (nullable)
│ module_id (FK)  │ (nullable)
│ lesson_id (FK)  │ (nullable)
│ file_name       │
│ file_type       │
│ file_size       │
│ storage_path    │
│ uploaded_by     │
│ created_at      │
└─────────────────┘
```

### State Diagram: Course Status Flow

```
        [Create Course]
              │
              ▼
         ┌─────────┐
         │  DRAFT  │◄─────────┐
         └─────────┘          │
              │               │
              │ Publish       │ Edit Published
              │ (has ≥1       │ Course
              │  module+      │
              │  lesson)      │
              ▼               │
       ┌───────────┐          │
       │ PUBLISHED │──────────┘
       └───────────┘
              │
              │ (Draft changes exist)
              ▼
       ┌───────────────────┐
       │ PUBLISHED (with   │
       │ unpublished       │
       │ draft changes)    │
       └───────────────────┘
              │
              │ Publish Updates
              ▼
       ┌───────────┐
       │ PUBLISHED │
       └───────────┘
```

---

## Implementation Notes (For Development Team Reference Only)

- Consider using a versioning strategy (snapshot or copy-on-write) to maintain separate draft and published versions
- Implement soft deletes for published courses to preserve enrollment history
- Use transaction boundaries to ensure course-module-lesson integrity
- Consider implementing draft auto-save functionality for better UX
- Media upload should use chunked/multipart upload for large files
- Implement proper indexing on course status and instructor_id for dashboard queries

