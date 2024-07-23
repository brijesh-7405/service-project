package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompanyRegistrationStatsDTO {
    private long companySignups;
    private long emailVerified;
    private long step1RegCompleted;
    private long step2RegCompleted;
}
