package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ConsDashboardStatsDTO {
    private Long jobApplied;
    private Long profilesUploaded;
    private Long appliedCount;
    private Long interviewCount;
    private Long hiredCount;
    private Long rejectedCount;
    private Long sharedCount;

    private Date updatedDate;

    private AlertsResponse alerts;
}
