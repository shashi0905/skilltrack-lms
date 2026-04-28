package com.skilltrack.api.service;

import com.skilltrack.common.entity.Lesson;
import com.skilltrack.common.entity.MediaAsset;
import com.skilltrack.common.entity.User;
import com.skilltrack.common.enums.MediaType;
import com.skilltrack.common.enums.ProcessingStatus;
import com.skilltrack.common.exception.BusinessException;
import com.skilltrack.common.repository.MediaAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    private final MediaAssetRepository mediaAssetRepository;
    private final VideoProcessingService videoProcessingService;

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${app.upload.max-file-size:104857600}") // 100MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/quicktime", "video/x-msvideo", "video/webm"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    @Autowired
    public FileUploadService(MediaAssetRepository mediaAssetRepository, 
                           VideoProcessingService videoProcessingService) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.videoProcessingService = videoProcessingService;
    }

    public MediaAsset uploadLessonContent(MultipartFile file, Lesson lesson, User uploadedBy) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Copy file to upload directory
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Determine media type
            MediaType mediaType = MediaType.fromContentType(file.getContentType());
            
            // Create MediaAsset entity
            MediaAsset mediaAsset = new MediaAsset(
                    originalFilename,
                    filePath.toString(),
                    file.getContentType(),
                    file.getSize(),
                    mediaType,
                    uploadedBy
            );
            
            mediaAsset.setLesson(lesson);
            
            // Set watermark for videos
            if (mediaType == MediaType.VIDEO) {
                mediaAsset.setWatermarkText("SkillTrack"); // Default watermark
            }

            MediaAsset savedAsset = mediaAssetRepository.save(mediaAsset);

            // Videos are served directly via MediaController, no HLS processing needed
            lesson.setProcessingStatus(ProcessingStatus.READY);

            logger.info("File uploaded successfully: {} for lesson: {}", originalFilename, lesson.getId());
            return savedAsset;

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", originalFilename, e);
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException("File content type is not specified");
        }

        if (!isAllowedContentType(contentType)) {
            throw new BusinessException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return ALLOWED_VIDEO_TYPES.contains(contentType) ||
               ALLOWED_DOCUMENT_TYPES.contains(contentType) ||
               ALLOWED_IMAGE_TYPES.contains(contentType);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public void deleteMediaAsset(MediaAsset mediaAsset) {
        try {
            // Delete physical file
            Path filePath = Paths.get(mediaAsset.getStoragePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Delete HLS files if video
            if (mediaAsset.isVideo() && mediaAsset.getHlsManifestUrl() != null) {
                videoProcessingService.deleteHlsFiles(mediaAsset);
            }
            
            logger.info("Media asset files deleted successfully: {}", mediaAsset.getOriginalFilename());
        } catch (IOException e) {
            logger.error("Failed to delete media asset files: {}", mediaAsset.getOriginalFilename(), e);
            throw new BusinessException("Failed to delete media asset: " + e.getMessage());
        }
    }
}