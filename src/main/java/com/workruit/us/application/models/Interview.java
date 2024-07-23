/**
 * 
 */
package com.workruit.us.application.models;

import java.sql.Date;
import java.sql.Time;

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
@Table(name = "interview")
public class Interview extends BaseModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "interview_id")
	private Long interviewId;

	@Column(name = "job_post_id")
	private Long jobPostId;

	@Column(name = "applicant_id")
	private Long applicantId;
	
	@Column(name = "recruiter_id")
	private Long recruiterId;

	@Column(name = "interview_title")
	private String interviewTitle;

	@Column(name = "interview_location")
	private String interviewLocation;

	@Column(name = "interview_date")
	private Date interviewDate;
	
	@Column(name = "interview_start_time")
	private Time interviewStartTime;
	
	@Column(name = "interview_end_time")
	private Time interviewEndTime;
	
	@Column(name = "interview_mode")
	private int interviewMode;
	
	@Column(name = "interview_description")
	private String interviewDescription;
	
	@Column(name="interview_video_link")
	private String interviewVideoLink;

}
