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
public class ConsActivityRejectedDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private String rejectedBy;
    private String offerRejectedBy;
    private Date updatedDate;
    private int interviewSatus;
    private int offerStatus;
    private Integer interviewFeedbackStatus;

}
