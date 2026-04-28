package com.skilltrack.api.dto.response;

import com.skilltrack.common.enums.ContentType;
import com.skilltrack.common.enums.ProcessingStatus;

import java.time.LocalDateTime;
import java.util.List;

public class LessonResponse {

    private String id;
    private String title;
    private String description;
    private String content;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
    private ContentType contentType;
    private ProcessingStatus processingStatus;
    private Integer videoDurationSeconds;
    private String hlsManifestUrl;
    private String moduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaAssetResponse> mediaAssets;
    private List<QuizQuestionResponse> quizQuestions;

    // Constructors
    public LessonResponse() {}

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public void setVideoDurationSeconds(Integer videoDurationSeconds) {
        this.videoDurationSeconds = videoDurationSeconds;
    }

    public String getHlsManifestUrl() {
        return hlsManifestUrl;
    }

    public void setHlsManifestUrl(String hlsManifestUrl) {
        this.hlsManifestUrl = hlsManifestUrl;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MediaAssetResponse> getMediaAssets() {
        return mediaAssets;
    }

    public void setMediaAssets(List<MediaAssetResponse> mediaAssets) {
        this.mediaAssets = mediaAssets;
    }

    public List<QuizQuestionResponse> getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(List<QuizQuestionResponse> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }
}