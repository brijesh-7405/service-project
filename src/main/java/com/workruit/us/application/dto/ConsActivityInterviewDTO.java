/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.Interview;
import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 */
@Data
public class ConsActivityInterviewDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private String companyName;
    private Date updatedDate;
    private String conComments;
    private String conOptions;
    private String interviewScheduledBy;
    private String interviewRequestedBy;
    private int interviewStatus;
    private int interviewAcceptStatus;
    private int interviewFeedbackStatus;
    private Interview interviewDetails;


}
