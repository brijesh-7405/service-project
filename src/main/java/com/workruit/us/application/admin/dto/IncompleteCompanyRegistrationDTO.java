package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class IncompleteCompanyRegistrationDTO {
    private long companyId;
    private String companyName;
    private String location;
    private String signupDate;
    private String isSignupCompleted;
    private String isEmailVerified;
    private String isCompanyRegistrationStep1Completed;
    private String isCompanyRegistrationStep2Completed;
}
