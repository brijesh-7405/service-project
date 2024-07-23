/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 */
@Data
public class TalentViewDTO {
    private boolean isConsultant;
    private String profileName;
    private String jobFunctionName;
    private String location;
    private String profilePic;
    private long count;
    private long intrestedCount;
    private long matchScore;
    private int applicantStatus;
    // Contains applicant id or Consultancy id based on isConsultant flag
    private long profileId;
    private boolean isintrested;
    private boolean recommended;


}
