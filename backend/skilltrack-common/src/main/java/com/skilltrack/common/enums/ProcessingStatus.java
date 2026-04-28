package com.skilltrack.common.enums;

public enum ProcessingStatus {
    PENDING("Pending Upload"),
    UPLOADING("Uploading"),
    PROCESSING("Processing"),
    READY("Ready"),
    FAILED("Processing Failed");

    private final String displayName;

    ProcessingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isComplete() {
        return this == READY || this == FAILED;
    }

    public boolean canBeAccessed() {
        return this == READY;
    }

    public boolean isInProgress() {
        return this == UPLOADING || this == PROCESSING;
    }
}