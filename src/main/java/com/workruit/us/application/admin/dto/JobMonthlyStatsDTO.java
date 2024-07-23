package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobMonthlyStatsDTO {
    private String month;
    private long createdJobs;
    private long pendingJobs;
    private long activeJobs;
    private long activatedJobs;
    private long closedJobs;
    private long totalVacancies;
}
