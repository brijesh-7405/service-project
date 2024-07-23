package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompleteConsultancyRegistrationDTO {
    private long consultancyId;
    private String consultancyName;
    private String location;
    private String registrationDate;
    private String industryType;
    private String domains;

    private long registeredConsultancy;
    private long pendingRegistrationConsultancy;
    private long uploadedProfiles;

    private long shortlisted;
    private long underInterview;
    private long underHire;
    private long underReject;

    private String lastActive;
    private String accountStatus;
}
