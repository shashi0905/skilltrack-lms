package com.skilltrack.common.enums;

public enum DifficultyLevel {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert");

    private final String displayName;

    DifficultyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return ordinal() + 1;
    }

    public static DifficultyLevel fromDisplayName(String displayName) {
        for (DifficultyLevel level : values()) {
            if (level.displayName.equalsIgnoreCase(displayName)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty level: " + displayName);
    }
}