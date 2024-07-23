/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.Date;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class CertificationDTO {
	private Long certificationId;
	private String title;
	private Date validFrom;
	private Date validTo;
	private String description;
	private String uploadCertificate;
	private boolean doesNotExpire;

}
