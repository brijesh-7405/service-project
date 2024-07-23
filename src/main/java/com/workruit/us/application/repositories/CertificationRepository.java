package com.workruit.us.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.Certification;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
	List<Certification> findByApplicantId(Long applicantId);
}
