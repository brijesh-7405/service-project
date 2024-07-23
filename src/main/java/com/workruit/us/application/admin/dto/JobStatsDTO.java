package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobStatsDTO {
    private long createdJobs;
    private long pendingJobs;
    private long activeJobs;
    private long activatedJobs;
    private long closedJobs;
    private long totalVacancies;
}
