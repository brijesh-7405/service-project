/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class ReferenceDTO {
	private Long referenceId;
	private String name;
	private String email;
	private String phoneNumber;
	private String referenceType;
	private String employer;
	private String title;
}
