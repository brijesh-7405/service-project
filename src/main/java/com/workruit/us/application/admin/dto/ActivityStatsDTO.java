package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ActivityStatsDTO {
    private long shortListedProfile;
    private long requestedInterview;
    private long scheduledInterview;
    private long rescheduledRequest;
    private long rescheduledInterview;
    private long holdApplicant;
    private long selectedApplicant;
    private long offerLetterSent;
    private long uniqueOfferLetterSent;
    private long acceptedOfferLetter;
    private long applicantAcceptedOfferLetter;
    private long joinedApplicant;
    private long rejectedOfferLetter;
    private long applicantRejectedOfferLetter;
    private long noShowApplicant;
    private long notFitApplicant;
    private long notJoinApplicant;
}
