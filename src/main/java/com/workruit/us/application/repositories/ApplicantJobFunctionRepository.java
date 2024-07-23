/**
 * 
 */
package com.workruit.us.application.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.workruit.us.application.models.ApplicantJobFunction;

/**
 * @author Santosh Bhima
 *
 */
public interface ApplicantJobFunctionRepository extends PagingAndSortingRepository<ApplicantJobFunction, Long> {

	List<ApplicantJobFunction> findByApplicantId(Long consultancyMatchedUser);

	List<ApplicantJobFunction> findAllByApplicantIdIn(Set<Long> applicantIds);
	void deleteByApplicantId(Long applicantId);
}
