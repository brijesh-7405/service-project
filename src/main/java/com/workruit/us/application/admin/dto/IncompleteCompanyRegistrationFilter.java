package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class IncompleteCompanyRegistrationFilter {
    private String location;
    private String isSignupCompleted;
    private String isEmailVerified;
    private String isCompanyRegistrationStep1Completed;
    private String isCompanyRegistrationStep2Completed;

}
