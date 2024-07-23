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
@Table(name = "education_history")
public class EducationHistory extends BaseModel {

	@Id
	@Column(name = "education_history_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long educationHistoryId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "college_name")
	private String collegeName;

	@Column(name = "degree")
	private String degree;

	@Column(name = "field")
	private String field;

	@Column(name = "location")
	private String location;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "currently_studying")
	private boolean currentlyStudying;

	@Column(name = "description")
	private String description;

}
