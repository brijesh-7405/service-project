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
public class ConsActivityViewResponse {
	private long totalCount = 0;
	private long totalPages = 0;
	List<ConsultancyActivityDTO> activityViewDTO = new ArrayList<>();
}
