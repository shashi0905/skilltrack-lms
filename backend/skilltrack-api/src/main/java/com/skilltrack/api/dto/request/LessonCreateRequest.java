package com.skilltrack.api.dto.request;

import com.skilltrack.common.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class LessonCreateRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 200, message = "Lesson title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Lesson description must not exceed 1000 characters")
    private String description;

    private String content;

    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Content type is required")
    private ContentType contentType = ContentType.TEXT;

    // Constructors
    public LessonCreateRequest() {}

    public LessonCreateRequest(String title, String description, String content) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.contentType = ContentType.TEXT;
    }

    public LessonCreateRequest(String title, String description, ContentType contentType) {
        this.title = title;
        this.description = description;
        this.contentType = contentType;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}