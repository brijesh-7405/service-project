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
public class ProjectDTO {
	private Long projectId;
	private String institutionName;
	private String role;
	private String title;
	private String location;
	private Date startDate;
	private Date endDate;
	private boolean projectOngoing;
	private String description;
}
