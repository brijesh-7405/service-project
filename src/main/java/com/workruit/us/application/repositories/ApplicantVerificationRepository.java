/**
 * 
 */
package com.workruit.us.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.us.application.models.ApplicantVerification;

/**
 * @author Santosh
 *
 */
public interface ApplicantVerificationRepository extends JpaRepository<ApplicantVerification, Long> {
	ApplicantVerification findByApplicantIdAndOtp(Long userId, String otp);

	ApplicantVerification findByApplicantId(Long userId);
}
