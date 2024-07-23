package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ConsActivityDashboardJobResponse {
    private long totalCount = 0;
    private long totalPages = 0;
     Map<String, List<ConsActivityJobDTO>> consActivityJobDTOS=new HashMap<String, List<ConsActivityJobDTO>>();
}
