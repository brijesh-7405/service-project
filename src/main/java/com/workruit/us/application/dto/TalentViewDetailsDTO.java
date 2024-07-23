	/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.List;

import com.workruit.us.application.models.JobQuestion;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class TalentViewDetailsDTO {
	private long applicantId;
	private String applicantName;
	private String jobFunc;
	private String experience;
	private String qualification;
	private String salary;
	private String location;
	private String about;

	private List<String> applicantSkills;
	private List<String> applicantExperience;
	private List<String> applicantEducation;
	private List<JobQuestion> jobQuestion;
	
}
