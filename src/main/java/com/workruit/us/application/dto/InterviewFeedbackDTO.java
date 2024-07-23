/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class InterviewFeedbackDTO {
	private int communicationLevel;
	private int knowledgeLevel;
	private String recruiterComments;
	private String consComments;
	private String consOption;
	private int status;
	private long applicantId;
	private long recruiterId;
	private long jobId;
	private long consultantId;
	
	private String applicantName;
	private String recruiterName;
	private String consultantName;
	private String jobTitle;
	
}
