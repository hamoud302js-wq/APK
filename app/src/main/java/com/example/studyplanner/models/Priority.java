package com.example.studyplanner.models;

/**
 * Priority levels for a task, stored in the database as {@code priority - 1}.
 */
public enum Priority {
    LOW("منخفضة"),
    MEDIUM("متوسطة"),
    HIGH("مرتفعة");

    private final String arabicLabel;

    Priority(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String getArabicLabel() {
        return arabicLabel;
    }

    public int toDbValue() {
        return ordinal() + 1;
    }

    public static Priority fromDbValue(int value) {
        int index = value - 1;
        if (index < 0 || index >= values().length) {
            return MEDIUM;
        }
        return values()[index];
    }
}
