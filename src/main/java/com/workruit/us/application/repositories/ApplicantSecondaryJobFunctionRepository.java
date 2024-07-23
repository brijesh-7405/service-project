/**
 * 
 */
package com.workruit.us.application.repositories;
import com.workruit.us.application.models.ApplicantSecondaryJobFunction;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;

/**
 * @author Santosh Bhima
 *
 */
public interface ApplicantSecondaryJobFunctionRepository extends PagingAndSortingRepository<ApplicantSecondaryJobFunction, Long> {

	List<ApplicantSecondaryJobFunction> findByApplicantId(Long consultancyMatchedUser);

	List<ApplicantSecondaryJobFunction> findAllByApplicantIdIn(Set<Long> applicantIds);
	void deleteByApplicantId(Long applicantId);
}
