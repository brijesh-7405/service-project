/**
 * 
 */
package com.workruit.us.application.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class UpdateJobStateDTO {
	@NotNull
	private String action;
	@NotNull
	private Long recruiterId;
	@NotNull
	private String recruiterName;
	@NotNull
	private String jobTitle;
	@NotNull
	private String applicantName;
	@NotNull
	private String consultancyName;
	
	

}
