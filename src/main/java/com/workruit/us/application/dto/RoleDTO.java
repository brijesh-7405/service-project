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
public class RoleDTO {
	@NotNull
	private Long roleId;
	private String name;
	private String description;
}
