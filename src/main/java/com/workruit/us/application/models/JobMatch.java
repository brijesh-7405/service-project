/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Mahesh
 */
@Data
@Entity
@Table(name = "job_match")
@SQLInsert(sql = "insert ignore into job_match (job_post_id, applicant_id, is_consultancy, recruiter_id,viewed_recruiter,viewed_applicant,saved_applicant,saved_recruiter,applicant_updated_date,recruiter_updated_date) VALUES (?, ?,?,?,?,?, ?,?,?,?)")
public class JobMatch implements Serializable {

    private static final long serialVersionUID = 6220010588160422748L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "job_match_id")
    private long jobMatchId;
    @Column(name = "job_post_id")
    private long jobPostId;
    @Column(name = "applicant_id")
    private long applicantId;
    @Column(name = "is_consultancy")
    private boolean isConsultancy;
    @Column(name = "recruiter_id")
    private long recruiterId;
    //	@Column(name = "viewed_recruiter")
//	private boolean isViewedByRecruiter;
//	@Column(name = "viewed_applicant")
//	private boolean isViewedByConOrUser;
    @Column(name = "saved_applicant")
    private boolean isSavedByApplicant;
    /*
     * @Column(name = "liked_applicant") private boolean isLikedByApplicant;
     *
     * @Column(name = "liked_recruiter") private boolean isLikedByRecruiter;
     */
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

    @Column(name = "applicant_status")
    private int applicantStatus;

    @Column(name = "applicant_job_status")
    private int applicantJobStatus;

    @Column(name = "updated_by_rec_id")
    private Long updatedByRecId;

    @Column(name = "updated_by_cons_id")
    private Long updatedByConsId;

    @Column(name = "interview_status")
    private long interviewStatus;

    @Column(name = "hired_status")
    private int hiredStatus;

    @Column(name = "reject_status")
    private int rejectStatus;

    @Column(name = "match_score")
    private int matchScore = 0;

    @Column(name = "consultancy_user_id")
    private Long consultancyUserId;

    @Column(name = "applicants_count")
    private Long applicantsCount;

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
}
