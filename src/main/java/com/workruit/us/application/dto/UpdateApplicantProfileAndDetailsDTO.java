package com.workruit.us.application.dto;

import lombok.Data;

@Data
public class UpdateApplicantProfileAndDetailsDTO {
    private UpdateApplicantDTO applicantProfile;
    private ApplicantDetailsDTO applicantDetails;
}
