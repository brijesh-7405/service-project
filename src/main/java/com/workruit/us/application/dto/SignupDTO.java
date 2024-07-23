/**
 * 
 */
package com.workruit.us.application.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class SignupDTO {
	@NotNull(message = "First name cannot be empty/null")
	@Size(min = 1, max = 19, message = "First name size should be minimum of length 1 and maximum of length 19.")
	private String firstName;
	@NotNull(message = "Last name cannot be empty/null")
	@Size(min = 1, max = 19, message = "Last name size should be minimum of length 1 and maximum of length 19.")
	private String lastName;
	@NotNull(message = "Work email cannot be empty/null")
	@Size(min = 1, message = "Work email should be minimum of length 1.")
	private String email;
	@NotNull(message = "Country code cannot be empty/null")
	@Size(min = 1, message = "Country code should be minimum of length 1.")
	private String countryCode;
	@NotNull(message = "Phone number cannot be empty/null")
	@Size(min = 1, message = "Phone number should be minimum of length 1.")
	private String phoneNumber;
	@NotNull(message = "Password cannot be empty/null")
	@Size(min = 8, message = "Password should be minimum of length 8.")
	private String password;
	@NotNull(message = "Password cannot be empty/null")
	@Size(min = 8, message = "Password should be minimum of length 8.")
	private String confirmPassword;
	@NotNull(message = "Company name cannot be empty/null")
	@Size(min = 1, message = "Company name should be minimum of length 1.")
	private String companyName;
	@NotNull
	private String role;
	private String roleName;
	private String notificationToken;
}
