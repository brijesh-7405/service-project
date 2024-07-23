package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompanyRegistrationMonthlyDTO {
    private String month;
    private long companySignups;
    private long emailVerified;
    private long step1RegCompleted;
    private long step2RegCompleted;
}
