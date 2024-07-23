/**
 * 
 */
package com.workruit.us.application.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.workruit.us.application.models.ApplicantJobSkill;

/**
 * @author Santosh Bhima
 *
 */
public interface ApplicantJobSkillRepository extends PagingAndSortingRepository<ApplicantJobSkill, Long> {

	List<ApplicantJobSkill> findByApplicantId(Long consultancyMatchedUser);

	List<ApplicantJobSkill> findAllByApplicantIdIn(Set<Long> applicantIds);

	void deleteByApplicantId(Long applicantId);

}
