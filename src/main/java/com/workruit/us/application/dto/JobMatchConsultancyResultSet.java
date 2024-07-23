package com.workruit.us.application.dto;

import com.workruit.us.application.models.JobMatchConsultancy;

import java.util.Date;

public interface JobMatchConsultancyResultSet {

    JobMatchConsultancy getJobMatchConsultancy();
    Date getUpdatedDate();
}
