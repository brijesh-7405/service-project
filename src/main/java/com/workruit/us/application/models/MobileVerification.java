/**
 * 
 */
package com.workruit.us.application.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Table(name = "mobile_verification")
@Entity
@Getter
@Setter
public class MobileVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "auto")
	private Long id;
	@Column(name = "mobile_number")
	private String mobileNumber;
	@Column(name = "otp")
	private String otp;
}
