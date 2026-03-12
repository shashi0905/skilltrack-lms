package com.skilltrack.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ModuleCreateRequest {

    @NotBlank(message = "Module title is required")
    @Size(max = 200, message = "Module title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Module description is required")
    @Size(max = 1000, message = "Module description must not exceed 1000 characters")
    private String description;

    // Constructors
    public ModuleCreateRequest() {}

    public ModuleCreateRequest(String title, String description) {
        this.title = title;
        this.description = description;
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
}