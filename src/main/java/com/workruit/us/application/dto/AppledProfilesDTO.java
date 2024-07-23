/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 */
@Data
public class AppledProfilesDTO {

    private Long applicantId;
    private String applicantName;
    private String profilePic;
    private String jobFunc;
    private String secondaryJobFunc;
    private String location;
    private boolean recommended;
    private String experience;
    private int applicantStatus;
    private int matchScore;
    private String applicantTitle;

}
