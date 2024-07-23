/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class ConsBulkApplyDTO {

	private long applicantId;
	private long jobPostId;
	private long consultancyId;
	private long jobMatchId;
	private List<QuestionAnswerDTO> questionAnswerDTO;
}
