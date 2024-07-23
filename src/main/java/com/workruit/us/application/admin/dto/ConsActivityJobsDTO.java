package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ConsActivityJobsDTO {
    private Long jobId;
    private String title;
    private String companyName;
    private String appliedBy;
    private Long underApplied;
    private Long underInterview;
    private Long underHired;
    private Long underRejected;
}
