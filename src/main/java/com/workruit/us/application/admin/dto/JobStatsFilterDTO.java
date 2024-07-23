package com.workruit.us.application.admin.dto;

import com.workruit.us.application.enums.JobType;
import lombok.Data;

@Data
public class JobStatsFilterDTO {
    private String location;
    private Integer jobFunction;
    private JobType jobType;
    private String workMode;
}
