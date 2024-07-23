package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobAppliedDTO {
    private Long jobId;
    private String title;
    private String jobFunction;
    private String location;
    private String workMode;
    private String jobType;
    private String companyName;
    private String applicantStatus;
    private String lastUpdated;
    private int matchScore;
}
