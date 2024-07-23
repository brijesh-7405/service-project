package com.workruit.us.application.enums;

public enum WorkMode {
    ONSITE_HYBRID(1, "Onsite-Hybrid"), ONSITE_IN_OFFICE(2, "Onsite-In Office"), REMOTE(3, "Remote");
    private final int value;
    private final String status;

    WorkMode(int value, String status) {
        this.value = value;
        this.status = status;
    }

    public static int getValueOf(String value) {
        if (value.equalsIgnoreCase(ONSITE_HYBRID.toString()))
            return WorkMode.ONSITE_HYBRID.getValue();
        else if (value.equalsIgnoreCase(ONSITE_IN_OFFICE.toString()))
            return WorkMode.ONSITE_IN_OFFICE.getValue();
        else if (value.equalsIgnoreCase(REMOTE.toString()))
            return WorkMode.REMOTE.getValue();
        else
            return -1;
    }

    public static WorkMode getByValue(int value) {
        for (WorkMode status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid WorkMode value: " + value);
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return status;
    }
}
