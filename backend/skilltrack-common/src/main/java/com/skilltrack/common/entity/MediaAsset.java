package com.skilltrack.common.entity;

import com.skilltrack.common.enums.MediaType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "media_assets")
public class MediaAsset extends BaseEntity {

    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Filename must not exceed 255 characters")
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @NotBlank(message = "Storage path is required")
    @Size(max = 500, message = "Storage path must not exceed 500 characters")
    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type must not exceed 100 characters")
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @NotNull(message = "File size is required")
    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @NotNull(message = "Media type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(name = "hls_manifest_url", length = 500)
    private String hlsManifestUrl;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Size(max = 100, message = "Watermark text must not exceed 100 characters")
    @Column(name = "watermark_text", length = 100)
    private String watermarkText;

    @NotNull(message = "Uploaded by user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    // Optional associations - media can be attached to course, module, or lesson
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private CourseModule module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    // Constructors
    public MediaAsset() {}

    public MediaAsset(String originalFilename, String storagePath, String contentType, 
                     Long fileSizeBytes, MediaType mediaType, User uploadedBy) {
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
        this.mediaType = mediaType;
        this.uploadedBy = uploadedBy;
    }

    // Business methods
    public boolean isImage() {
        return mediaType == MediaType.IMAGE;
    }

    public boolean isVideo() {
        return mediaType == MediaType.VIDEO;
    }

    public boolean isDocument() {
        return mediaType == MediaType.DOCUMENT;
    }

    public boolean isAudio() {
        return mediaType == MediaType.AUDIO;
    }

    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else if (fileSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public String getFileExtension() {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        return lastDotIndex > 0 ? originalFilename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    public boolean isVideoProcessed() {
        return isVideo() && hlsManifestUrl != null && !hlsManifestUrl.trim().isEmpty();
    }

    public boolean requiresVideoProcessing() {
        return isVideo() && !isVideoProcessed();
    }

    public String getFormattedVideoDuration() {
        if (videoDurationSeconds == null) {
            return "Unknown";
        }
        int hours = videoDurationSeconds / 3600;
        int minutes = (videoDurationSeconds % 3600) / 60;
        int seconds = videoDurationSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    // Getters and Setters
    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHlsManifestUrl() {
        return hlsManifestUrl;
    }

    public void setHlsManifestUrl(String hlsManifestUrl) {
        this.hlsManifestUrl = hlsManifestUrl;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
}