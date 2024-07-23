/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class OfferDetailsDTO {
	private Long offerDetailsId;
	private Long consultancyId;
	private Date joiningDate;
	private String offerUrl;
	private String offerSignedUrl;
	//ENUM of offerStatus
	@Schema(description = "1=order recieved,2=accepted")
	private int offerStatus;	
	
	private String applicantName;
	private String recruiterName;
	private String consultantName;
	private String jobTitle;
}
