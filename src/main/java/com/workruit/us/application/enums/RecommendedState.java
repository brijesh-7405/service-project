package com.workruit.us.application.enums;

public enum RecommendedState {
    RELEVANT,SUGGESTED;
    public int getValue() {
        return ordinal() + 1;
    }
}
