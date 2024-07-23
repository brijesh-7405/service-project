package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlertsResponse {

    private long totalCount = 0;
    private long totalPages = 0;
    List<AlertDTO> alertDTOs = new ArrayList<>();
}
