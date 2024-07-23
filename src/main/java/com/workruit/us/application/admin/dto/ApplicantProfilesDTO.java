package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ApplicantProfilesDTO {
    private Long applicantId;
    private String firstName;
    private String lastName;
    private String consultancyName;
    private String uploadedDate;
    private String jobFunction;
    private String secondaryJobFunction;
    private String location;
    private int profileMatch;
    private String interestedDate;
}