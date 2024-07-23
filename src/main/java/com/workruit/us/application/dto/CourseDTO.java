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
public class CourseDTO {
	private Long courseId;
	private String courseTitle;
	private String description;
	private String institutionName;
	private String courseDuration;
	private boolean stillPursuing;

}
