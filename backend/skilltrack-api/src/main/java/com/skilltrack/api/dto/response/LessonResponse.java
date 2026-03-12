package com.skilltrack.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class LessonResponse {

    private Long id;
    private String title;
    private String description;
    private String content;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
    private Long moduleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaAssetResponse> mediaAssets;

    // Constructors
    public LessonResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
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
}