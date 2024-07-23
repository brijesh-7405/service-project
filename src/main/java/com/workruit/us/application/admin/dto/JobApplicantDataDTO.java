package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobApplicantDataDTO {
    private Long applicantId;
    private String firstName;
    private String lastName;
    private String location;
    private String consultancyName;
    private String primaryJobFunction;
    private String secondaryJobFunction;
    private String applicantStatus;
    private String statusUpdatedBy;
    private String dateOfLastUpdate;
    private int profileScore;
}
