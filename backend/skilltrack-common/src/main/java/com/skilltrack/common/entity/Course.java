package com.skilltrack.common.entity;

import com.skilltrack.common.enums.CourseStatus;
import com.skilltrack.common.enums.DifficultyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Course title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Course description is required")
    @Size(max = 2000, message = "Course description must not exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull(message = "Difficulty level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DifficultyLevel difficulty;

    @Column(length = 500)
    private String tags;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @NotNull(message = "Course status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status = CourseStatus.DRAFT;

    @NotNull(message = "Instructor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MediaAsset> mediaAssets = new ArrayList<>();

    @Column(name = "published_at")
    private java.time.LocalDateTime publishedAt;

    @Column(name = "has_draft_changes")
    private Boolean hasDraftChanges = false;

    // Constructors
    public Course() {}

    public Course(String title, String description, DifficultyLevel difficulty, User instructor) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.instructor = instructor;
        this.status = CourseStatus.DRAFT;
        this.hasDraftChanges = false;
    }

    // Business methods
    public boolean canBePublished() {
        return !modules.isEmpty() && 
               modules.stream().anyMatch(module -> !module.getLessons().isEmpty());
    }

    public void publish() {
        if (!canBePublished()) {
            throw new IllegalStateException("Course cannot be published without at least one module with one lesson");
        }
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = java.time.LocalDateTime.now();
        this.hasDraftChanges = false;
    }

    public void markAsDraftChanged() {
        if (this.status == CourseStatus.PUBLISHED) {
            this.hasDraftChanges = true;
        }
    }

    public boolean isDraft() {
        return this.status == CourseStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == CourseStatus.PUBLISHED;
    }

    public int getTotalLessons() {
        return modules.stream()
                .mapToInt(module -> module.getLessons().size())
                .sum();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        markAsDraftChanged();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        markAsDraftChanged();
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        markAsDraftChanged();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
        markAsDraftChanged();
    }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
        markAsDraftChanged();
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public List<CourseModule> getModules() {
        return modules;
    }

    public void setModules(List<CourseModule> modules) {
        this.modules = modules;
    }

    public List<MediaAsset> getMediaAssets() {
        return mediaAssets;
    }

    public void setMediaAssets(List<MediaAsset> mediaAssets) {
        this.mediaAssets = mediaAssets;
    }

    public java.time.LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(java.time.LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getHasDraftChanges() {
        return hasDraftChanges;
    }

    public void setHasDraftChanges(Boolean hasDraftChanges) {
        this.hasDraftChanges = hasDraftChanges;
    }

    // Helper methods for managing modules
    public void addModule(CourseModule module) {
        modules.add(module);
        module.setCourse(this);
        module.setOrderIndex(modules.size());
        markAsDraftChanged();
    }

    public void removeModule(CourseModule module) {
        modules.remove(module);
        module.setCourse(null);
        // Reorder remaining modules
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setOrderIndex(i + 1);
        }
        markAsDraftChanged();
    }

    // Helper methods for managing media assets
    public void addMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.add(mediaAsset);
        mediaAsset.setCourse(this);
        markAsDraftChanged();
    }

    public void removeMediaAsset(MediaAsset mediaAsset) {
        mediaAssets.remove(mediaAsset);
        mediaAsset.setCourse(null);
        markAsDraftChanged();
    }
}