package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class IncompleteRegistrationFilter {
    private String location;
    private String isSignupCompleted;
    private String isEmailVerified;
    private String isRegistrationStep1Completed;
    private String isRegistrationStep2Completed;

}
