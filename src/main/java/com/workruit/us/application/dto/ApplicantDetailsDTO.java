/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Santosh Bhima
 */
@Data
public class ApplicantDetailsDTO {
    private Long applicantDetailId;
    private Long applicantId;
    private String careerLevel;
    private String citizenship;
    private String yearsOfExperience;
    private String currentSalary;
    private String salaryType;
    private boolean hideSalary;
    private String expectedSalary;
    private String expectedSalaryType;
    private boolean hideExpectedSalary;
    private String jobFunction;
    private String secondaryJobFunction;
    private String jobType;
    private String noticePeriod;
    private String currentWorkStatus;
    private boolean willingToRelocate;
    private String relocation;
    private String preferredWorkMode;
    private String skills;
    private String salaryRate;
    private String expectedSalaryRate;

}
