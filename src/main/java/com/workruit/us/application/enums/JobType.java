/**
 *
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 */
public enum JobType {
    FULLTIME(1, "Full Time"),
    PARTTIME(2, "Part Time"),
    CONTRACT(3, "Contract"),
    INTERNSHIP(4, "Internship"),
    FREELANCE(5, "Freelance");
    private final int value;
    private final String status;

    JobType(int value, String status) {
        this.value = value;
        this.status = status;
    }

    public static int getValueOf(String value) {
        // for (JobType status : values()) {
        if (value.equalsIgnoreCase(FULLTIME.toString()))
            return JobType.FULLTIME.getValue();
        else if (value.equalsIgnoreCase(PARTTIME.toString()))
            return JobType.PARTTIME.getValue();
        else if (value.equalsIgnoreCase(CONTRACT.toString()))
            return JobType.CONTRACT.getValue();
        else if (value.equalsIgnoreCase(INTERNSHIP.toString()))
            return JobType.INTERNSHIP.getValue();
        else if (value.equalsIgnoreCase(FREELANCE.toString()))
            return JobType.FREELANCE.getValue();
        else
            return -1;
        // }
    }

    public static JobType getByValue(int value) {
        for (JobType status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid JobType value: " + value);
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return status;
    }


}
