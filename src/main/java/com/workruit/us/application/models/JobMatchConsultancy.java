/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Mahesh
 */
@Data
@Entity
@Table(name = "job_match_consultancy")
public class JobMatchConsultancy extends BaseModel implements Serializable {

    private static final long serialVersionUID = 6220010588160422748L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "job_match_con_id")
    private long jobMatchConId;
    @Column(name = "job_post_id")
    private long jobPostId;
    @Column(name = "applicant_id")
    private long applicantId;
    @Column(name = "recruiter_id")
    private long recruiterId;
    @Column(name = "consultancy_id")
    private long consultancyId;
    //	@Column(name = "viewed_recruiter")
//	private boolean isViewedByRecruiter;
//	@Column(name = "viewed_applicant")
//	private boolean isViewedByConOrUser;
    @Column(name = "saved_applicant")
    private boolean isSavedByApplicant;
    @Column(name = "saved_recruiter")
    private boolean isSavedByRecruiter;
    @Column(name = "applicant_updated_date")
    private Date applicantUpdatedDate;
    @Column(name = "recruiter_updated_date", nullable = true)
    private Date recruiterUpdatedDate;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "updated_date")
    private Date updatedDate;

    // Column holds applicant status for job - Shortlisted/interview/hire/rejected
    @Column(name = "applicant_status")
    private int applicantStatus;
    // Column holds job status for applicant - Applied/interview/hire/rejected
    @Column(name = "applicant_job_status")
    private int applicantJobStatus;

    @Column(name = "updated_by_rec_id")
    private Long updatedByRecId;

    @Column(name = "updated_by_cons_id")
    private Long updatedByConsId;

    @Column(name = "updated_by_cons_user_id")
    private Long updatedByConsUserId;

    @Column(name = "interview_status")
    private int interviewStatus;

    @Column(name = "hired_status")
    private int hiredStatus;

    @Column(name = "reject_status")
    private int rejectStatus;

    @Column(name = "match_score")
    private int matchScore = 0;

    @Column(name = "consultancy_user_id")
    private Long consultancyUserId;
    //Interview Date
    @Column(name = "interview_requested_date")
    private Date interviewRequestedDate;
    @Column(name = "interview_scheduled_date")
    private Date interviewScheduledDate;
    @Column(name = "rescheduled_request_date")
    private Date rescheduledRequestDate;
    @Column(name = "rescheduled_interview_date")
    private Date rescheduledInterviewDate;
    @Column(name = "on_hold_date")
    private Date onHoldDate;
    @Column(name = "accepeted_date")
    private Date accepetedDate;
    //Hired Date
    @Column(name = "selected_date")
    private Date selectedDate;
    @Column(name = "offer_sent_date")
    private Date offerSentDate;
    @Column(name = "offer_accepted_date")
    private Date offerAcceptedDate;
    //Rejected Date
    @Column(name = "rejected_date")
    private Date rejectedDate;

    @Column(name = "rejected_user_id")
    private Long rejectedUserId;

    @Column(name = "selected_user_id")
    private Long selectedUserId;
    @Column(name = "onhold_updated_user_id")
    private Long onholdUpdatedUserId;
    @Column(name = "offersent_user_id")
    private Long offersentUserId;
    @Column(name = "offeraccepted_user_id")
    private Long offeracceptedUserId;

    @Column(name = "offerrejected_user_id")
    private Long offerrejectedUserId;
    @Column(name = "accepted_user_id")
    private Long acceptedUserId;

    @Column(name = "interview_scheduled_user_id")
    private Long interviewScheduledUserId;
    @Column(name = "interview_rescheduled_user_id")
    private Long interviewRescheduledUserId;
    @Column(name = "interview_rescheduled_requested_user_id")
    private Long interviewRescheduledRequestedUserId;
    //new
    @Column(name = "interview_requested_user_id")
    private Long interviewRequestedUserId;

    @Column(name = "interview_rejected_user_id")
    private Long interviewRejectedUserId;
    @Column(name = "interview_rejected_date")
    private Date interviewRejectedDate;

    @Column(name = "saved_recruiter_id")
    private Long savedRecruiterId;
    @Column(name = "saved_recruiter_date")
    private Date savedRecruiterDate;

    @Column(name = "recommended")
    private Integer recommended;
    @Column(name = "recommended_score")
    private Integer recommendedScore;
    @Column(name = "applicant_joined_status_updated_user_id")
    private Long applicantAoinedStatusUpdatedUserId;
    @Column(name = "applicant_joined_status_updated_date")
    private Date applicantAoinedStatusUpdatedDate;

    @Column(name = "lastaction_performed_recruiter_id")
    private Long lastActionPerformedRecruiterId;

    @Column(name = "lastaction_performed_consultant_user_id")
    private Long lastactionPerformedConsultantUserId;
}
