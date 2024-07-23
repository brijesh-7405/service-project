/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.Date;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class WorkExperienceDTO {
	private Long workExperienceId;
	private String jobTitle;
	private String companyName;
	private String location;
	private Date startDate;
	private Date endDate;
	private boolean currentlyWorkingHere;
	private String description;
}
