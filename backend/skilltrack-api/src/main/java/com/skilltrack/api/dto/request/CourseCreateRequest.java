package com.skilltrack.api.dto.request;

import com.skilltrack.common.enums.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CourseCreateRequest {

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Course title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Course description is required")
    @Size(max = 2000, message = "Course description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationHours;

    // Constructors
    public CourseCreateRequest() {}

    public CourseCreateRequest(String title, String description, DifficultyLevel difficulty) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }

    // Getters and Setters
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

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }
}