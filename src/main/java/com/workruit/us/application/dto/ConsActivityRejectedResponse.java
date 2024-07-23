/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class ConsActivityRejectedResponse {
	private long totalCount = 0;
	private long totalPages = 0;
	List<ConsActivityRejectedDTO> activityRejectedDTO = new ArrayList<>();
	private ConsDashboardStatsDTO consDashboardStatsDTO;
}