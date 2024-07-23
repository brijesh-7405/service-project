/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.Interview;
import lombok.Data;

import java.util.List;

/**
 * @author Mahesh
 *
 */
@Data
public class ActivityInterviewDTO {
    List<UserDTO> usersList;
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private String jobTitle;
    private String secondaryJobFunctionName;
    private String consultancyName;
    private String interviewScheduledBy;
    private boolean isConsultancy;
    private long interviewStatus;
    private long interviewFeedbackStatus;
    private int communicationLevel;
    private int knowledgeLevel;
    private String recruiterComments;
    private long interviewAcceptStatus;
    private Interview interviewDetails;


}
