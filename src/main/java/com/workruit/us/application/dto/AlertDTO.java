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
public class AlertDTO {
	private Long alertId;
	private String message;
	private Long userId;
	private String username;
	private String date;
	private long totalCount = 0;
	private long totalPages = 0;
}
