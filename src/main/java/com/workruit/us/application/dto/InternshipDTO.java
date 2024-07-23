/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Santosh Bhima
 */
@Data
public class InternshipDTO {
    private Long internshipId;
    private String jobTitle;
    private String companyName;
    private String description;
    private String location;
    private boolean currentlyWorkingHere;
    private Date startDate;
    private Date endDate;
}
