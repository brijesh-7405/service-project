/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class ApplicantInterviewDTO {
	private Long inteviewId;
	private Long applicantId;
	private String applicantName;
	private String jobTitle;
	private String interviewDate;
	private String companyName;
	private Long companyId;
	private Long recruiterId;
	private String recruiterName;
}
