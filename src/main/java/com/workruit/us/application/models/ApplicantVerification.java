package com.workruit.us.application.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name = "applicant_verification")
@Entity
@Getter
@Setter
public class ApplicantVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "applicant_verification_id")
	private Long applicantVerificationId;
	@Column(name = "applicant_id")
	private Long applicantId;
	@Column(name = "email_otp_code")
	private String otp;
}
