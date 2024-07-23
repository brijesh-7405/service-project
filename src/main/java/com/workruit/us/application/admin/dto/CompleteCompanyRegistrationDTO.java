package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompleteCompanyRegistrationDTO {
    private long companyId;
    private String companyName;
    private String location;
    private String registrationDate;
    private String industryType;

    private long registeredEmployer;
    private long pendingRegistrationEmployer;

    private long pendingJob;
    private long activeJob;
    private long closedJob;
    private long vacancies;

    private long shortlisted;
    private long underInterview;
    private long underHire;
    private long underReject;

    private String lastActive;
    private String accountStatus;
}
