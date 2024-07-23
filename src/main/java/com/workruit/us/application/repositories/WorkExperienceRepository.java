package com.workruit.us.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.WorkExperience;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {
	List<WorkExperience> findByApplicantId(Long applicantId);

	WorkExperience findByWorkExperienceIdAndApplicantId(Long workExperienceId, Long applicantId);

	@Query(value = "select * from work_experience  where  applicant_id=?1 ORDER BY  work_experience_id DESC LIMIT 1", nativeQuery = true)
	WorkExperience findByWorkExperienceIdAndApplicantId(Long applicantId);

	void deleteByApplicantId(Long applicantId);

}
