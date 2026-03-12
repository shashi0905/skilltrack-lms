package com.skilltrack.common.enums;

public enum CourseStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String displayName;

    CourseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }
}