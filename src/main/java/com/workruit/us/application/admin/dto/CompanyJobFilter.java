package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompanyJobFilter {
    private Long jobOwner;
    private String jobLocation;
    private String status;
}
