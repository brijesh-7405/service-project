package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class EmployerStatsDTO {

    private long companies;
    private long employerMembers;
    private long activeJobs;
    private long pendingJobs;
    private long closedJobs;
    private long shortListedApplicants;
    private long applicantUnderInterview;
    private long applicantUnderHired;
    private long applicantUnderRejected;
}
