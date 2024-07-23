/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class QuestionAnswerDTO {
	private long questionId;
	private long questionValueId;
	private String questionAnswer;
}
