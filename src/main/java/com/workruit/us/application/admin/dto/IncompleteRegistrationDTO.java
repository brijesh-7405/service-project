package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class IncompleteRegistrationDTO {
    private long id;
    private String name;
    private String location;
    private String signupDate;
    private String isSignupCompleted;
    private String isEmailVerified;
    private String isRegistrationStep1Completed;
    private String isRegistrationStep2Completed;
}
