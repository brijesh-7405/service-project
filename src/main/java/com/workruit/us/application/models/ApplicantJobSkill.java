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
@Data
@Table(name = "applicant_job_skill")
@Entity
public class ApplicantJobSkill extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "applicant_job_skill_id")
	private Long applicantJobFunctionId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "job_skill_id")
	private Integer jobSkillId;

}
