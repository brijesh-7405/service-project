/**
 * 
 */
package com.workruit.us.application.dto;

import java.sql.Date;
import java.sql.Time;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class InterviewDTO {

	@JsonProperty(access = Access.READ_ONLY)
	private Long interviewId;
	private String interviewTitle;
	private String interviewLocation;
	private Date interviewDate;
	private String interviewStartTime;
	private String interviewEndTime;
	private int interviewMode;
	private String applicantName;
	private String recruiterName;
	private String consultantName;
	private String jobTitle;

//	private int interviewStatus;
//	private long recruiterId;
//	private long jobId;
//	private long applicantId;

	private String interviewDescription;

	private boolean isConsultancy = true;

	private String interviewVideoLink;

}
