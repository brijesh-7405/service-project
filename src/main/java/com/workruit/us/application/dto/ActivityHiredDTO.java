/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.OfferDetails;
import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 *
 */
@Data
public class ActivityHiredDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private Date updateDate;
    private String jobFunctionName;
    private String consultancyName;
    private String hiredBy;
    private boolean isConsultancy;
    private OfferDetails offerDetails;
    private InterviewDTO interviewDetails;

}
