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
public class ChangePasswordDTO {
	@NotNull
	private String currentPassword;
	@NotNull
	private String password;
	@NotNull
	private String confirmPassword;
}
