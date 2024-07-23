package com.workruit.us.application.admin.enums;

public enum ApplicantStatus {
    SHORTLISTED(1),
    UNDER_INTERVIEW(2),
    UNDER_HIRED(3),
    UNDER_REJECTED(4);

    private final int value;

    ApplicantStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
