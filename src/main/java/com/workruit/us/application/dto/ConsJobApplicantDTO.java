/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 */
@Data
public class ConsJobApplicantDTO {
    private long applicantId;
    private String applicantName;
    private String jobFunction;
    private String experience;
    private String qualification;
    private String jobType;
    private String location;
    private String profilePic;
    private int percentage;
    private int status;
    private boolean recommended;
    private int matchScore;

}
