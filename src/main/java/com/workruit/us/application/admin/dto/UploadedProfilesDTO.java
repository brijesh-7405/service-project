package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class UploadedProfilesDTO {
    private Long applicantId;
    private String firstName;
    private String lastName;
    private String uploadedDate;
    private String location;
    private String jobFunction;
    private String secondaryJobFunction;
    private String gender;
    private String ethnicity;
    private String citizenship;
    private float yearsOfExperience;
    private String careerLevel;
    private String workMode;
    private String jobType;
    private String noticePeriod;
    private String currentWorkStatus;
    private Long jobsAppliedFor;
    private Long relevantJobs;
    private float profileCompletion;
    private String uploadedBy;
}
