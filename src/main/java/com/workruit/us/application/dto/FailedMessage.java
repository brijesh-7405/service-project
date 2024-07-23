/**
 * 
 */
package com.workruit.us.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author Dell
 *
 */
@Data
@Builder
public class FailedMessage {
	private Message msg;
	private String status;
}
