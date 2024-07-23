/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Santosh
 *
 */
@Data
@Builder
public class ApiResponse {
	// private String message;
	private Object data;
	// private Message msg;
	private String status;
	private String description;
	private String title;

}
