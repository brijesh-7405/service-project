package com.workruit.us.application.admin.dto;

import com.workruit.us.application.enums.JobStatus;
import lombok.Data;

import java.util.Map;

@Data
public class CompanyJobDTO {

    private Long jobPostId;
    private String postedDate;
    private String applyBy;
    private Map<Long,String>  jobOwner;
    private String jobLocation;
    private JobStatus status;
    private Long vacancies;
    private Long relevantProfiles;
    private Long interestedProfiles;
    private Long shortlistedProfiles;
    private Long underInterviewProfiles;
    private Long underHiredProfiles;
    private Long underRejectedProfiles;
    private String jobTitle;
    private Map<Long,String> collaborator;
}
