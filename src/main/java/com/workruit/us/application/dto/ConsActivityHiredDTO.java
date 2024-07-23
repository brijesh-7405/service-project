/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.Interview;
import com.workruit.us.application.models.OfferDetails;
import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 */
@Data
public class ConsActivityHiredDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private Date updatedDate;
    private String consultancyName;
    private String hiredBy;
    private boolean isConsultancy;
    private OfferDetails offerDetails;
    private Interview interviewDetails;
    private String offerAccepted;

}
