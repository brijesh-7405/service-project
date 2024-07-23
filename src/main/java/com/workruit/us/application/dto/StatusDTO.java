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
public class StatusDTO {
	private long shortlisted=0;
	private long interviewed=0;
	private long hired=0;
	private long rejected=0;
	private long matchCount=0;

}
