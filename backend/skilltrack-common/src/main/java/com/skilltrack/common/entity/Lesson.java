package com.skilltrack.common.entity;

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

    @NotNull(message = "Module is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    // Constructors
    public Lesson() {}

    public Lesson(String title, String description, String content, CourseModule module) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.module = module;
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
}