package com.workruit.us.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.InterviewFeedback;

/**
 * Mahesh
 */
@Repository
public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {
	InterviewFeedback findByInterviewId(Long interviewId);
	
}
