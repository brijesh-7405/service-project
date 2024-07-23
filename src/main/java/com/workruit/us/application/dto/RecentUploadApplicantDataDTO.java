package com.workruit.us.application.dto;

import com.workruit.us.application.enums.ApplicantStatus;
import lombok.Data;

@Data
public class RecentUploadApplicantDataDTO {

    private Long applicantId;
    private String firstName;
    private String lastName;
    private String location;
    private String jobFunction;
    private String experience;
    private String jobType;
    private String qualification;
    private String uploadedUserBy;
    private boolean correctionNeeded;
    private long uploadedUserId;
    private ApplicantStatus status;
    private float profileCompletionStatus;

}
