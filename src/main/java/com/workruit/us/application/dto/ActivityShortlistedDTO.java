/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 */
@Data
public class ActivityShortlistedDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private Date updatedDate;
    private String location;
    private String experience;
    private String jobFunctionName;
    private String secondaryJobFunctionName;
    private String consultancyName;
    private String shortListedBy;
    private boolean isConsultancy;

}
