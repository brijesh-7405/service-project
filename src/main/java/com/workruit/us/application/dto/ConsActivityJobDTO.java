package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ConsActivityJobDTO {
    private Long jobId;
    private String jobFunction;
    private String jobTitle;
    private String companyName;
    private Long totalCount;
    private Long interviewedCount;
    private Long hiredCount;
    private Long rejectedCount;
    private Long appliedCount;
    private Long uploadedCount;
    private String profilePic;
    private String appliedBy;
    private Long userId;
    private Date updatedDate;
    private String name;
}
