/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Santosh Bhima
 */
@Data
@Entity
@Table(name = "applicant_details")
public class ApplicantDetails {
    @Id
    @Column(name = "applicant_detail_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantDetailId;

    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "skills")
    private String skills;

    @Column(name = "career_level")
    private String careerLevel;

    @Column(name = "years_of_exp")
    private String yearsOfExperience;

    @Column(name = "current_salary")
    private String currentSalary;

    @Column(name = "salary_type")
    private String salaryType;

    @Column(name = "hide_salary")
    private boolean hideSalary;

    @Column(name = "expected_salary")
    private String expectedSalary;

    @Column(name = "expected_salary_type")
    private String expectedSalaryType;

    @Column(name = "hide_expected_salary")
    private boolean hideExpectedSalary;

    @Column(name = "job_function")
    private String jobFunction;

    @Column(name = "secondary_job_function")
    private String secondaryJobFunction;


    @Column(name = "preferred_work_mode")
    private String preferredWorkMode;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "notice_period")
    private String noticePeriod;

    @Column(name = "current_work_status")
    private String currentWorkStatus;

    @Column(name = "citizenship")
    private String citizenship;

    @Column(name = "willing_to_relocate")
    private boolean willingToRelocate;

    @Column(name = "relocation")
    private String relocation;

    @Column(name = "salaryRate")
    private String salaryRate;

    @Column(name = "expected_salary_rate")
    private String expectedSalaryRate;

    @Column(name = "degree_id")
    private Long degreeId;

    @Column(name = "job_function_id")
    private Long jobFunctionId;

    @Column(name = "secondary_job_function_id")
    private String secondaryJobFunctionIds;

    @Column(name = "skill_id")
    private String skillIds;

}
