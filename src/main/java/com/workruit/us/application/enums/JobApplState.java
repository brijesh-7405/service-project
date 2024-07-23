/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 * SHORTLISTED-Liked-applicant_status(Recruiter shown Intrest to applicant)
 * SHORTLISTED-Liked-applicant_job_status(User & Consultancy shown Intrest to Job)
 * 1 Like 
 * 2 INTERVIEWED
 * 3 HIRED
 * 4 REJECTED-- In Interview process Recruiter rejecting user
 *
 */
public enum JobApplState {
	SHORTLISTED,REJECTED;
	
	public int getValue() {
	    return ordinal() + 1;
	}
}
