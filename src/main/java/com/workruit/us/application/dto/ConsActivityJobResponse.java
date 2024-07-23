package com.workruit.us.application.dto;

import lombok.Data;

import java.util.*;

@Data
public class ConsActivityJobResponse {
    private long totalCount = 0;
    private long totalPages = 0;
   // Map<String, List<ConsActivityJobDTO>> consActivityJobDTOS=new HashMap<String, List<ConsActivityJobDTO>>();
    List<ConsActivityJobDTO> consActivityJobDTOS =new ArrayList<ConsActivityJobDTO>();
}
