package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobApplicantProfilesDTO {
    private Long applicantId;
    private String firstName;
    private String lastName;
    private String uploadedDate;
    private String primaryJobFunction;
    private String secondaryJobFunction;
    private String location;
    private String applicantStatus;
    private String dateOfLastUpdate;
    private int profileScore;
}
