/**
 * 
 */
package com.workruit.us.application.models;

import java.io.Serializable;
import java.util.Set;

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
 */
@Data
@Entity
@Table(name = "job_question_values")
public class JobQuestionValues implements Serializable {
	private static final long serialVersionUID = -6985432295271130270L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@JsonProperty(access = Access.READ_ONLY)
	@Column(name = "job_ques_id")
	private Long quesValueId;

	/*
	 * @Column(name = "question_id") private Long questionId;
	 */	
	@Column(name = "question_value")
	private String questionValue;
}
