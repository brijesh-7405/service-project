package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private EmployerStatsDTO employer;
    private ConsultancyStatsDTO consultancy;
}
