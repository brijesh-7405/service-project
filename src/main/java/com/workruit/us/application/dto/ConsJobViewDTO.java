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
public class ConsJobViewDTO {
    private String profileName;
    private String jobFunctionName;
    private String jobTitle;
    private String location;
    private String companyPic;
    private String companyName;
    private long jobPostId;
    private Date updatedDate;
    private String expiration;
    private int status;
    private long interestedCount;
    private long totalApplicantCount;
    private int matchScore;
    private boolean isRecommended;
}
