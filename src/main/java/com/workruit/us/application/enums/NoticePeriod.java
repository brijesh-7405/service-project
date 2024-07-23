/**
 *
 */
package com.workruit.us.application.enums;

import java.util.List;

/**
 * @author Mahesh
 */
public enum NoticePeriod {
    IMMEDIATE("Immediate"), DAYS_15_LESS("Less Than 15 days");
    private final String npValue;

    NoticePeriod(String npValue) {
        this.npValue = npValue;
    }

    public static int getValueOf(String value) {
        if (value.equalsIgnoreCase(IMMEDIATE.toString()))
            return NoticePeriod.IMMEDIATE.getValue();
        else if (value.equalsIgnoreCase(DAYS_15_LESS.toString()))
            return NoticePeriod.DAYS_15_LESS.getValue();
        else
            return -1;
    }

    public static NoticePeriod getValueOf(int value) {
        switch (value) {
            case 1:
                return IMMEDIATE;
            case 2:
                return DAYS_15_LESS;
            default:
                return null;
        }
    }

    public static NoticePeriod getValueOf(List<Integer> value) {

        switch (value.get(0)) {
            case 1:
                return IMMEDIATE;
            case 2:
                return DAYS_15_LESS;
            default:
                return null;
        }
    }

    //  public String getValue() {
//      return this.npValue;
//  }
    public int getValue() {
        return ordinal() + 1;
    }

    public String toString() {
        switch (this) {
            case IMMEDIATE:
                return "Immediate";
            case DAYS_15_LESS:
                return "Less Than 15 days";
        }
        return null;
    }

}
