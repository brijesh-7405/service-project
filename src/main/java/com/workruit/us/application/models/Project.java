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
@Table(name = "projects")
@Data
public class Project extends BaseModel {
	@Id
	@Column(name = "project_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long projectId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "institution_name")
	private String institutionName;

	@Column(name = "role")
	private String role;

	@Column(name = "title")
	private String title;

	@Column(name = "location")
	private String location;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "project_ongoing")
	private boolean projectOngoing;

	@Column(name = "description")
	private String description;
}
