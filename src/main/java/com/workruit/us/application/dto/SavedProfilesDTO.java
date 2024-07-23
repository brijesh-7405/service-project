/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 */
@Data
public class SavedProfilesDTO {
    private long applicantId;
    private long jobMatchId;
    private long jobId;
    private String jobTitle;
    private long consultancyId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private String consultancyName;
    private long applicantStatus;
    private boolean isConsultancy;
    private String expiration;
    private long diffDays;
}
