package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompleteCompanyRegistrationFilter {
    private String location;
    private String industryType;
    private Integer accountStatus;
}
