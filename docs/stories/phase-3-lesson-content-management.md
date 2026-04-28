# Phase 3: Lesson Content Management with Media Upload

## Business Context

Instructors need to create engaging, multimedia lessons to effectively teach their students. While Phase 2 established the basic course structure (courses, modules, lessons), instructors currently can only add text content to lessons. To compete with modern learning platforms like Udemy and Coursera, SkillTrack must support rich media content, particularly video lectures which are the primary content type for online learning.

Each lesson should be optimized for a single content type (video, PDF, text, etc.) rather than mixing multiple types within one lesson. This approach provides clarity for both instructors creating content and students consuming it. Video content requires special handling including transcoding for optimal streaming, content protection to prevent unauthorized downloads, and watermarking for brand protection.

Additionally, instructors need the ability to add quizzes as standalone curriculum items that can be placed after any lesson or module, providing flexible assessment options throughout the learning journey.

## Story

**As an** instructor  
**I want to** upload and manage different types of media content for my lessons, with optimized handling for video content and the ability to add quizzes  
**So that** I can create engaging, professional learning experiences that protect my intellectual property while providing flexible assessment options

## Acceptance Criteria

### AC1: Upload Video Content to Lessons
**Given** I am editing a lesson in my course  
**When** I upload a video file (MP4, AVI, MOV, etc.)  
**Then** the video is uploaded and queued for background processing  
**And** I see a "processing" status indicator  
**And** the lesson is marked as video content type  
**And** students cannot access the lesson until processing is complete

### AC2: Video Processing and Transcoding
**Given** I have uploaded a video to a lesson  
**When** the background processing completes successfully  
**Then** the video is transcoded into multiple resolutions (360p, 720p, 1080p)  
**And** the video is prepared for encrypted HLS streaming  
**And** a platform watermark is burned into the video during transcoding  
**And** the lesson status changes from "processing" to "ready"  
**And** I receive a notification that my video is ready for students

### AC3: Video Content Protection
**Given** I have a published lesson with video content  
**When** a student accesses the lesson  
**Then** the video is streamed via encrypted HLS (not direct file access)  
**And** video URLs are signed with expiration timestamps  
**And** no download button or right-click save option is available  
**And** the watermark is visible throughout playback  
**And** the video player prevents common download attempts

### AC4: Watermark Configuration
**Given** I am an admin user  
**When** I configure platform settings  
**Then** I can set a default watermark text (default: "SkillTrack")  
**And** I can override the watermark for specific courses  
**And** the watermark appears in a consistent position on all videos  
**And** watermark changes apply to newly uploaded videos only

### AC5: Upload PDF and Document Content
**Given** I am editing a lesson in my course  
**When** I upload a PDF or document file  
**Then** the file is stored securely  
**And** the lesson is marked as document content type  
**And** students can view the document in an embedded viewer  
**And** students can download the document (unlike videos)  
**And** the document is immediately available after upload

### AC6: Upload Image Content
**Given** I am editing a lesson in my course  
**When** I upload an image file (JPG, PNG, GIF, etc.)  
**Then** the image is stored and optimized for web display  
**And** the lesson is marked as image content type  
**And** the image is displayed inline within the lesson  
**And** students can view but not directly download the image

### AC7: Text-Based Lessons
**Given** I am editing a lesson in my course  
**When** I choose to create a text-based lesson  
**Then** I can use a rich text editor to format content  
**And** the lesson is marked as text content type  
**And** I can include basic formatting (headings, lists, links, emphasis)  
**And** the content is immediately available to students

### AC8: Create Quiz Lessons
**Given** I am adding content to a module  
**When** I choose to create a quiz  
**Then** I can add multiple choice, single choice, and true/false questions  
**And** I can set correct answers and explanations  
**And** the quiz appears as a standalone item in the module curriculum  
**And** I can position the quiz after any lesson or at the end of the module  
**And** students must complete the quiz to progress (if configured)

### AC9: Content Type Validation and Restrictions
**Given** I am uploading content to a lesson  
**When** the upload is processed  
**Then** file types are validated against allowed extensions  
**And** file sizes are checked against configured limits (100MB for videos, 50MB for documents)  
**And** malware scanning is performed on all uploads  
**And** rejected files show clear error messages with guidance

### AC10: Lesson Content Management
**Given** I have lessons with different content types  
**When** I view my course curriculum  
**Then** each lesson shows its content type icon (video, PDF, text, quiz)  
**And** I can see processing status for video lessons  
**And** I can replace content while preserving lesson metadata  
**And** I can preview how students will see each lesson type

### AC11: Student Lesson Experience
**Given** I am a student enrolled in a course  
**When** I access lessons with different content types  
**Then** video lessons play in a secure, adaptive streaming player  
**And** PDF lessons open in an embedded document viewer  
**And** text lessons display with proper formatting  
**And** quiz lessons present questions with immediate feedback  
**And** my progress is tracked for each lesson type

### AC12: Content Analytics for Instructors
**Given** I am an instructor with published lessons  
**When** I view lesson analytics  
**Then** I can see completion rates by lesson type  
**And** I can see average time spent on video lessons  
**And** I can see quiz performance statistics  
**And** I can identify lessons where students commonly drop off

## Out of Scope

- Live streaming or real-time video features
- Interactive video features (clickable hotspots, branching scenarios)
- Automatic video transcription or closed captions
- Advanced quiz types (drag-and-drop, coding challenges, essay questions)
- Collaborative editing of lesson content
- Version history for lesson content beyond basic audit trails
- Integration with external video hosting platforms (YouTube, Vimeo)
- Mobile app-specific video download for offline viewing
- Advanced DRM beyond encrypted HLS streaming
- Custom video player branding beyond watermarks
- Bulk content import/export tools

## Dependencies

- Phase 2: Learning Catalog Management must be completed
- Video transcoding infrastructure (FFmpeg or cloud service like AWS Elemental)
- Encrypted HLS streaming capability
- File storage infrastructure with CDN support
- Background job processing system for video transcoding
- Malware scanning service for uploaded files
- Email notification system for processing completion

## Assumptions

- Instructors have reliable internet connections for large video uploads
- Students have modern browsers supporting HLS video streaming
- Platform has sufficient storage and bandwidth for video content
- Video processing can take 5-10 minutes per hour of content
- Most lessons will be video-based (80%+), with text and PDF as supplements
- Quiz questions are primarily multiple choice with simple correct/incorrect scoring
- Watermarks are text-based overlays, not complex logos or graphics
- Content protection focuses on casual piracy prevention, not enterprise DRM
- File upload progress indication is handled by the frontend framework

## Non-Functional Requirements (NFRs)

### Performance
- Video uploads should support resumable uploads for files over 100MB
- Video processing should complete within 10 minutes per hour of content
- Streaming video should adapt to user's bandwidth within 5 seconds
- Lesson content should load within 2 seconds for non-video types

### Security
- All uploaded files must pass malware scanning before storage
- Video streaming URLs must expire within 24 hours
- Only course-enrolled students can access lesson content
- Instructor content is isolated by user permissions

### Scalability
- System should handle 100 concurrent video uploads
- Video CDN should support 1000+ concurrent streams
- Storage should scale to accommodate growing content library

### Usability
- Upload progress must be clearly visible with time estimates
- Error messages for failed uploads must be specific and actionable
- Video processing status must be real-time updated
- Content type switching should preserve lesson metadata where possible

---

## Supporting Documentation

### Content Type Flow Diagram

```
Instructor creates lesson
    ↓
Selects content type:
    ├── Video → Upload → Queue Processing → Transcode + Watermark → Ready
    ├── PDF → Upload → Scan → Store → Ready
    ├── Image → Upload → Optimize → Store → Ready
    ├── Text → Rich Editor → Save → Ready
    └── Quiz → Question Builder → Configure → Ready
    ↓
Student accesses lesson:
    ├── Video → Encrypted HLS Player
    ├── PDF → Embedded Viewer
    ├── Image → Inline Display
    ├── Text → Formatted Content
    └── Quiz → Interactive Questions
```

### Video Processing Sequence

```
Instructor → API: POST /api/lessons/{id}/content (video file)
API → Storage: Store original video
API → Queue: Add transcoding job
API → Instructor: Upload complete, processing started

Background Worker → FFmpeg: Transcode to multiple resolutions
Background Worker → FFmpeg: Add watermark overlay
Background Worker → Storage: Store HLS segments + manifest
Background Worker → Database: Update lesson status = READY
Background Worker → Notification: Email instructor

Student → API: GET /api/lessons/{id}/stream
API → Auth: Verify enrollment + permissions
API → CDN: Generate signed HLS URL (24hr expiry)
CDN → Student: Encrypted HLS stream
```

### Entity Relationship Updates

```
┌─────────────────┐
│     Lesson      │
├─────────────────┤
│ id (PK)         │
│ module_id (FK)  │
│ title           │
│ description     │
│ content_type    │ (VIDEO, PDF, IMAGE, TEXT, QUIZ)
│ processing_status│ (PENDING, PROCESSING, READY, FAILED)
│ order_index     │
│ created_at      │
│ updated_at      │
└─────────────────┘
        │
        │ 1:1
        ▼
┌─────────────────┐
│  LessonContent  │
├─────────────────┤
│ id (PK)         │
│ lesson_id (FK)  │
│ content_type    │
│ text_content    │ (for TEXT type)
│ media_asset_id  │ (for VIDEO/PDF/IMAGE)
│ quiz_config     │ (JSON for QUIZ type)
│ created_at      │
│ updated_at      │
└─────────────────┘

┌─────────────────┐
│   MediaAsset    │ (Enhanced)
├─────────────────┤
│ id (PK)         │
│ original_filename│
│ storage_path    │
│ hls_manifest_url│ (for videos)
│ processing_status│
│ watermark_text  │
│ content_type    │
│ file_size_bytes │
│ media_type      │
│ uploaded_by     │
│ lesson_id (FK)  │
│ created_at      │
└─────────────────┘
```

---

## Implementation Notes (For Development Team Reference Only)

- Use chunked/resumable upload for large video files (tus.io protocol recommended)
- Implement video transcoding with FFmpeg or AWS Elemental MediaConvert
- Use HLS with AES-128 encryption for video streaming
- Store video segments in CDN-backed storage (S3 + CloudFront)
- Implement job queue for background processing (Redis + Sidekiq or AWS SQS)
- Use signed URLs with short expiration for video access
- Consider implementing video thumbnail generation during transcoding
- Add comprehensive logging for video processing pipeline debugging
- Implement cleanup jobs for failed/orphaned video processing artifacts