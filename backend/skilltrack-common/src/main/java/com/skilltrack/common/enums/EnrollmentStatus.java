package com.skilltrack.common.enums;

public enum EnrollmentStatus {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    DROPPED("Dropped");

    private final String displayName;

    EnrollmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
