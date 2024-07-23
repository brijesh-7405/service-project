/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Entity
@Table(name = "certification")
@Data
public class Certification extends BaseModel {

	@Id
	@Column(name = "certification_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long certificationId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "title")
	private String title;

	@Column(name = "valid_from")
	private Date validFrom;

	@Column(name = "valid_to")
	private Date validTo;

	@Column(name = "description")
	private String description;

	@Column(name = "upload_certificate")
	private String uploadCertificate;

	@Column(name = "does_not_expire")
	private boolean doesNotExpire;
}
