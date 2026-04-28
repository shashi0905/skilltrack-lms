package com.skilltrack.common.enums;

public enum QuestionType {
    SINGLE_CHOICE("Single Choice"),
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_FALSE("True/False");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean allowsMultipleCorrectAnswers() {
        return this == MULTIPLE_CHOICE;
    }

    public int getMaxOptions() {
        return switch (this) {
            case TRUE_FALSE -> 2;
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> 6;
        };
    }

    public int getMinOptions() {
        return switch (this) {
            case TRUE_FALSE -> 2;
            case SINGLE_CHOICE, MULTIPLE_CHOICE -> 2;
        };
    }
}