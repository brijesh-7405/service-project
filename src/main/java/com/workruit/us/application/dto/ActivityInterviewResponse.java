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
public class ActivityInterviewResponse {
	private long totalCount = 0;
	private long totalPages = 0;
	private StatusDTO countsInfo=new StatusDTO();
	List<ActivityInterviewDTO> activityInterviewDTO = new ArrayList<>();
}
