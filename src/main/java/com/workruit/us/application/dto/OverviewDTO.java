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
public class OverviewDTO {
	private int jobsAppliedFor;
	private int profilesUploaded;
	private int profilesShared;

	private int activeJobs;
	private int pendingJobs;
	private int closedJobs;

}
