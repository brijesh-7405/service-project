package com.workruit.us.application.dto;

public interface ApplicantRecommendedResultSet {
    Long getId();
    Long getJobPostId();
    Integer getMatchScore();

    Integer getApplicantStatus();
}
