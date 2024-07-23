/**
 * 
 */
package com.workruit.us.application.models;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Data;

/**
 * @author Mahesh
 *
 * 
 */
@Data
@Entity
@Table(name = "job_question_answers")
public class JobQuestionAnswers extends BaseModel implements Serializable{

	private static final long serialVersionUID = -6004914968050287961L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@JsonProperty(access = Access.READ_ONLY)
	@Column(name = "job_ques_ans_id")
	private Long quesAnsValueId;

	@Column(name = "job_post_id")
	private Long jobPostId;
	
	@Column(name = "application_id")
	private Long applicantId;

	@Column(name = "question_id")
	private Long questionId;

	@Column(name = "question_ans_value")
	private String questionAnsValue;
	
	@Column(name = "consultancy_id")
	private Long consultancyId;

}