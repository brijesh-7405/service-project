package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class OverallActivityDTO {
    private String jobTitle;
    private String jobPostedDate;
    private String companyName;
    private String applicantFirstName;
    private String applicantLastName;
    private String applicantLocation;
    private String consultancyName;
    private String applicantStatus;
    private String statusUpdatedBy;
    private String dateOfLastStatusUpdate;
    private int profileMatchScore;
}
