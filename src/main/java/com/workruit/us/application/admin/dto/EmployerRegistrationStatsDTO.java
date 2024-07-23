package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class EmployerRegistrationStatsDTO {
    private long invitesSent;
    private long accountActivations;
    private long profileUpdates;

}
