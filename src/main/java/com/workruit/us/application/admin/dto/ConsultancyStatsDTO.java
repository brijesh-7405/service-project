package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ConsultancyStatsDTO {

    private long consultancies;
    private long consultancyMembers;
    private long uploadedApplicantProfile;
    private long applicantsUnderApplied;
    private long applicantUnderInterview;
    private long applicantUnderHired;
    private long applicantUnderRejected;
}
