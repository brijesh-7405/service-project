/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
@Entity
@Table(name = "interview_feedback")
public class InterviewFeedback extends BaseModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "feedback_id")
	private Long feedbackId;

	@Column(name = "interview_id")
	private Long interviewId;

	@Column(name = "communication_level")
	private int communicationLevel;
	
	@Column(name = "knowledge_level")
	private int knowledgeLevel;

	@Column(name = "recruiter_comments")
	private String recruiterComments;
	
	@Column(name = "cons_comments")
	private String consComments;

	@Column(name="cons_option")
	private String consOption;

	@Column(name = "status")
	private int status;
	
	@Column(name = "recruiter_id")
	private Long recruiterId;
	
	@Column(name = "job_id")
	private Long jobId;
	
	@Column(name = "applicant_id")
	private Long applicantId;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "updated_date")
	private Date updatedDate;

}
