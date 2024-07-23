package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.Date;

@Data
public class PendingJobsDTO {
    private String jobTitle;
    private String jobLocation;
    private String jobFunction;
    private String postedBy;
    private Date postedDate;
    private Date applyBy;
    private long noOfVacancies;
    private long jobPostId;
}
