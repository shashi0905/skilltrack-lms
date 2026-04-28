package com.skilltrack.api.service;

import com.skilltrack.api.dto.response.MediaAssetResponse;
import com.skilltrack.common.entity.*;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.exception.ResourceNotFoundException;
import com.skilltrack.common.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class LessonContentService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final FileUploadService fileUploadService;
    private final MediaAssetMapper mediaAssetMapper;

    @Autowired
    public LessonContentService(LessonRepository lessonRepository,
                              CourseModuleRepository moduleRepository,
                              CourseRepository courseRepository,
                              UserRepository userRepository,
                              MediaAssetRepository mediaAssetRepository,
                              FileUploadService fileUploadService,
                              MediaAssetMapper mediaAssetMapper) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.fileUploadService = fileUploadService;
        this.mediaAssetMapper = mediaAssetMapper;
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public MediaAssetResponse uploadLessonContent(String courseId, String moduleId, String lessonId, 
                                                MultipartFile file, String description, String instructorEmail) {
        
        // Validate access
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        // Check if lesson content type supports file upload
        if (!lesson.getContentType().requiresFileUpload()) {
            throw new BusinessException("Lesson content type does not support file upload");
        }
        
        // Check if lesson already has content for single-content types
        if (!lesson.getMediaAssets().isEmpty() && 
            (lesson.isVideoLesson() || lesson.isPdfLesson() || lesson.isImageLesson())) {
            throw new BusinessException("Lesson already has content. Delete existing content first.");
        }

        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        // Upload file
        MediaAsset mediaAsset = fileUploadService.uploadLessonContent(file, lesson, instructor);
        
        if (description != null && !description.trim().isEmpty()) {
            mediaAsset.setDescription(description.trim());
            mediaAssetRepository.save(mediaAsset);
        }

        return mediaAssetMapper.toResponse(mediaAsset);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public void deleteMediaAsset(String courseId, String moduleId, String lessonId, 
                               String mediaAssetId, String instructorEmail) {
        
        // Validate access
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        
        MediaAsset mediaAsset = mediaAssetRepository.findById(mediaAssetId)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found"));
        
        if (!mediaAsset.getLesson().getId().equals(lesson.getId())) {
            throw new BusinessException("Media asset does not belong to this lesson");
        }

        boolean wasOnlyAsset = lesson.getMediaAssets().size() == 1;

        // Remove from lesson's collection first to avoid orphan reference
        lesson.removeMediaAsset(mediaAsset);

        // Delete physical files
        fileUploadService.deleteMediaAsset(mediaAsset);

        // Reset lesson processing status if this was the only media asset
        if (wasOnlyAsset) {
            lesson.setProcessingStatus(com.skilltrack.common.enums.ProcessingStatus.PENDING);
            lesson.setHlsManifestUrl(null);
            lesson.setVideoDurationSeconds(null);
        }
        lessonRepository.save(lesson);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    public String getProcessingStatus(String courseId, String moduleId, String lessonId, String instructorEmail) {
        Lesson lesson = validateInstructorAccess(courseId, moduleId, lessonId, instructorEmail);
        return lesson.getProcessingStatus().getDisplayName();
    }

    private Lesson validateInstructorAccess(String courseId, String moduleId, String lessonId, String instructorEmail) {
        User instructor = userRepository.findByEmail(instructorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = courseRepository.findCourseByIdAndInstructor(courseId, instructor)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found or access denied"));

        CourseModule module = moduleRepository.findByIdAndCourse(moduleId, course)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        return lessonRepository.findByIdAndModule(lessonId, module)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }
}