package com.workruit.us.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.Publication;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
	List<Publication> findByApplicantId(Long applicantId);

	void deleteByApplicantId(Long applicantId);
}
