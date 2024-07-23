/**
 * 
 */
package com.workruit.us.application.enums;

/**
 * @author Mahesh
 *
 */
public enum InterviewStatus {
	SELECTED(1, "Selected"),
	INTERVIEW_SCHEDULED(2, "Interview Scheduled"),
	RESCHEDULED_INTERVIEW(3, "Interview Re-Scheduled"),
	RESCHEDULE_REQUESTED(4, "Interview Re-Schedule Requested"),
	NO_SHOW(5, "No Show"),
	ON_HOLD(6, "On Hold"),
	HIRED(7, "Hired"),
	REJECTED(8, "Rejected"),
	REQUESTED_INTERVIEW(9, "Interview Requested"),
	NOT_FIT(10, "Not Fit"),
	NO_SHOW_REJECTED(11, "No Show Rejected"),
	INTERVIEW_ACCEPTED(12, "Interview Accepted"),
	INTERVIEW_REJECTED(13, "Interview Rejected"),
	APPLICANT_JOINED(14, "Applicant Joined"),
	APPLICANT_NOT_JOINED(15, "Applicant Not Joined"),
	OFFER_SENT(16, "Offer Letter Sent"),
	OFFER_ACCEPTED(17, "Offer Letter Accepted"),
	OFFER_REJECTED(18, "Offer Letter Rejected");

	private final int value;
	private final String status;

	InterviewStatus(int value, String status) {
		this.value = value;
		this.status = status;
	}

	public int getValue() {
		return value;
	}

	@Override
	public String toString() {
		return status;
	}

	public static InterviewStatus getByValue(int value) {
		for (InterviewStatus status : values()) {
			if (status.value == value) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid InterviewStatus value: " + value);
	}

}


