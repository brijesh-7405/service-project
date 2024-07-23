/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class DashboardDTO {
	private OverviewDTO overview;
	private StatusDTO status;
	private Set<TeamProfileDTO> teamProfiles;
	private List<ApplicantInterviewDTO> applicantInterviews;
	private JobViewResponse jobViewResponse;
	private AlertsResponse alerts;
}
