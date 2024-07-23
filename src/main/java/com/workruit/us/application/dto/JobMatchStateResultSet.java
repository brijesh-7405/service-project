/**
 *
 */
package com.workruit.us.application.dto;

import java.util.Date;

/**
 * @author Mahesh
 */
public interface JobMatchStateResultSet {
    Long getJobMatchId();

    Long getJobPostId();

    Long getApplicantId();

    Long getRecruiterId();

    Long getConsultancyId();

    Boolean isConsultancy();

    Long getInterviewStatus();

    Long getApplicantStatus();

    Long getSavedRecruiter();

    Long getHiredStatus();

    Date getUpdatedDate();

}
