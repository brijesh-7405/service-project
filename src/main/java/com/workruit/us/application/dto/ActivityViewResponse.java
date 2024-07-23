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
public class ActivityViewResponse {
	private long totalCount = 0;
	private long totalPages = 0;
	List<JobViewDTO> activityViewDTO = new ArrayList<>();
}
