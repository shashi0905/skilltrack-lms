package com.skilltrack.common.enums;

public enum ContentType {
    TEXT("Text Content"),
    VIDEO("Video Content"),
    PDF("PDF Document"),
    IMAGE("Image Content"),
    QUIZ("Quiz Assessment");

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresFileUpload() {
        return this == VIDEO || this == PDF || this == IMAGE;
    }

    public boolean requiresProcessing() {
        return this == VIDEO;
    }

    public boolean isProtectedContent() {
        return this == VIDEO;
    }

    public boolean isDownloadable() {
        return this == PDF;
    }
}