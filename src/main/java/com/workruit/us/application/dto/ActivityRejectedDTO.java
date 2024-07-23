/**
 * 
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.OfferDetails;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class ActivityRejectedDTO {
	private long applicantId;
	private long jobMatchId;
	private String profilePicUrl;
	private long interviewStatus;
	private String applicantName;
	private String location;
	private String jobFunctionName;
	private String consultancyName;
	private String rejectedBy;
	private boolean isConsultancy;
	private OfferDetails offerDetails;

	

}
