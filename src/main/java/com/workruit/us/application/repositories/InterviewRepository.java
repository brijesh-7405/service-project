package com.workruit.us.application.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.Interview;

/**
 * Mahesh
 */
@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
	Interview findByJobPostIdAndApplicantId(long jobPostId, long applicantId);

	@Query(value = "select * from interview  where job_post_id=?1 and applicant_id in(?2)", nativeQuery=true)
	List<Interview> findByJobPostIdAndApplicantId(long jobPostId, List<Long> applicantId);

	List<Interview> findByRecruiterIdIn(Set<Long> recruiterIds);

}
