package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompleteRegistrationFilter {
    private String location;
    private List<String> industryType;
    private Integer accountStatus;
    private List<String> domainSpecialization;
}
