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
public class JobAppliedFor {
	private String jobName;
	private int shortlisted;
	private int interviewed;
	private int hired;
	private int rejected;
}
