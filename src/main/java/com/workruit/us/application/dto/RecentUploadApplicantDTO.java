package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecentUploadApplicantDTO {

    private List<RecentUploadApplicantDataDTO> data;
    private Long totalProfile;
    private Long working;
    private Long notWorking;
    private Long onBench;
    private Long underNoticePeriod;
    private Long jobId;
    private long totalCount = 0;
    private long totalPages = 0;
}
