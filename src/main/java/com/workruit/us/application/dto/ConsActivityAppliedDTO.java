/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 *
 */
@Data
public class ConsActivityAppliedDTO {
    private long applicantId;
    private long jobMatchId;
    private String profilePicUrl;
    private String applicantName;
    private String location;
    private String jobFunctionName;
    private String appliedBy;
    private Date updatedDate;
}
