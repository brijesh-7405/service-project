package com.workruit.us.application.dto;

import java.util.Date;

public interface ConsActivityJobResultSet {
    Long getInterview();

    Long getHired();

    Long getRejected();

    Long getApplied();

    Long getUpdatedByConsUserId();

    Long getJobPostId();

    Date getUpdatedDate();


}
