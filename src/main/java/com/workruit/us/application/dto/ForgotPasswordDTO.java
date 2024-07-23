/**
 * 
 */
package com.workruit.us.application.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class ForgotPasswordDTO {
	@NotNull
	private String password;
	@NotNull
	private String confirmPassword;

}
