package com.skilltrack.common.entity;

import com.skilltrack.common.enums.ContentType;
import com.skilltrack.common.enums.ProcessingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Lesson title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Size(max = 1000, message = "Lesson description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "Order index is required")
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Content type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType = ContentType.TEXT;

    @NotNull(message = "Processing status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.READY;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Column(name = "hls_manifest_url", length = 500)
    private String hlsManifestUrl;

    @NotNull(message = "Module is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    // Constructors
    public Lesson() {}

    public Lesson(String title, String description, String content, CourseModule module) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.module = module;
        this.contentType = ContentType.TEXT;
        this.processingStatus = ProcessingStatus.READY;
    }

    public Lesson(String title, String description, ContentType contentType, CourseModule module) {
        this.title = title;
        this.description = description;
        this.contentType = contentType;
        this.module = module;
        this.processingStatus = contentType.requiresProcessing() ? ProcessingStatus.PENDING : ProcessingStatus.READY;
    }

    // Business methods
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean hasMediaAssets() {
        return !mediaAssets.isEmpty();
    }

    public int getMediaAssetCount() {
        return mediaAssets.size();
    }

    public boolean isVideoLesson() {
        return contentType == ContentType.VIDEO;
    }

    public boolean isPdfLesson() {
        return contentType == ContentType.PDF;
    }

    public boolean isTextLesson() {
        return contentType == ContentType.TEXT;
    }

    public boolean isQuizLesson() {
        return contentType == ContentType.QUIZ;
    }

    public boolean isImageLesson() {
        return contentType == ContentType.IMAGE;
    }

    public boolean isReadyForStudents() {
        return processingStatus.canBeAccessed();
    }

    public boolean requiresProcessing() {
        return contentType.requiresProcessing();
    }

    public boolean isProcessing() {
        return processingStatus.isInProgress();
    }

    public int getQuizQuestionCount() {
        return quizQuestions.size();
    }

    public boolean hasQuizQuestions() {
        return !quizQuestions.isEmpty();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        markModuleAsChanged();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        markModuleAsChanged();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        markModuleAsChanged();
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        markModuleAsChanged();
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
        // Reset processing status based on content type
        this.processingStatus = contentType.requiresProcessing() ? ProcessingStatus.PENDING : ProcessingStatus.READY;
        markModuleAsChanged();
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
        markModuleAsChanged();
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
        markModuleAsChanged();
    }

    public String getHlsManifestUrl() {
        return hlsManifestUrl;
    }

    public void setHlsManifestUrl(String hlsManifestUrl) {
        this.hlsManifestUrl = hlsManifestUrl;
        markModuleAsChanged();
    }

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public List<MediaAsset> getMediaAssets() {
        return mediaAssets;
    }

    public void setMediaAssets(List<MediaAsset> mediaAssets) {
        this.mediaAssets = mediaAssets;
    }

    public List<QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(List<QuizQuestion> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    // Helper methods
    private void markModuleAsChanged() {
        if (module != null && module.getCourse() != null) {
            module.getCourse().markAsDraftChanged();
        }
    }

    // Helper methods for managing media assets
    public void addMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.add(mediaAsset);
        mediaAsset.setLesson(this);
        markModuleAsChanged();
    }

    public void removeMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.remove(mediaAsset);
        mediaAsset.setLesson(null);
        markModuleAsChanged();
    }

    // Helper methods for managing quiz questions
    public void addQuizQuestion(QuizQuestion quizQuestion) {
        quizQuestions.add(quizQuestion);
        quizQuestion.setLesson(this);
        markModuleAsChanged();
    }

    public void removeQuizQuestion(QuizQuestion quizQuestion) {
        quizQuestions.remove(quizQuestion);
        quizQuestion.setLesson(null);
        markModuleAsChanged();
    }
}