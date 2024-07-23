package com.workruit.us.application.dto;

import javax.persistence.Column;

public interface ConsultancyJobStatusResultSet {

    Long getJobMatchConId();
    Long getJobPostId();
    Long getConsultancyId();
    Long getConsultancyUserId();
    int getStatus();
    int getDaysLeft();
    String getJobTitle();
}
