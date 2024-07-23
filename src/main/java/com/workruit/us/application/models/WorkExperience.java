/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Entity
@Data
@Table(name = "work_experience")
public class WorkExperience extends BaseModel {
	@Id
	@Column(name = "work_experience_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long workExperienceId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "company")
	private String companyName;

	@Column(name = "location")
	private String location;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "currently_working_here")
	private boolean currentlyWorkingHere;

	@Column(name = "description")
	private String description;
}
