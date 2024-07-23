/**
 * 
 */
package com.workruit.us.application.models;

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
@Table(name = "courses")
@Data
public class Course extends BaseModel {

	@Id
	@Column(name = "course_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long courseId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "course_title")
	private String courseTitle;

	@Column(name = "institution_name")
	private String institutionName;

	@Column(name = "course_duration")
	private String courseDuration;

	@Column(name = "description")
	private String description;

	@Column(name = "still_pursuing")
	private boolean stillPursuing;
}
