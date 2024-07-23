package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class JobApplicantFilter {
    private Long consultancy;
    private String location;
    private int status;
}
