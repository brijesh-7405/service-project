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
public class EducationHistoryDTO {
	private Long educationHistoryId;
	private String collegeName;
	private String degree;
	private String field;
	private String location;
	private Date startDate;
	private Date endDate;
	private boolean currentlyStudying;
	private String description;
}
