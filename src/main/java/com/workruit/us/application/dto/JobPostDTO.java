/**
 *
 */
package com.workruit.us.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.workruit.us.application.enums.Currency;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.enums.SalaryType;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * @author Mahesh
 *
 */
@Data
public class JobPostDTO {

    private Long userId;

    @JsonProperty(access = Access.READ_ONLY)


    private Long jobPostId;

    @NotBlank(message = "Title cannot be empty/null")
    @Size(min = 1, max = 100, message = "Title should be minimum of length 1 and maximum of length 100.")
    private String title;

    private String jobPostedBy;


    @NotEmpty(message = "Job function cannot be empty/null")
    @Valid
    private List<Integer> jobFunction = new ArrayList<>();

    private List<Integer> optionalJobfunctions = new ArrayList<>();

    @NotEmpty(message = "Job Skills cannot be empty/null")

    @Valid
    private List<Integer> jobSkills = new ArrayList<>();

    private Set<JobQuestionDTO> jobQuestion = new HashSet<>();

    private List<Integer> jobBenefits = new ArrayList<>();

    private List<Integer> jobDegrees = new ArrayList<>();

    private List<Integer> jobSupplementalPay = new ArrayList<>();

    @NotNull(message = "Job Type cannot be empty/null")
    @NumberFormat
    @Valid
    private Integer jobType;

    private Date jobTypeStartDate;

    private Date jobTypeEndDate;
    private Date createdDate;
    private Date updatedDate;


    private Integer jobTypeDuration;

    private boolean jobTypeUnpaid;

    @NotBlank(message = "Work location value cannot be empty/null")
    @Valid
    private String worklocValue;

    @NotBlank(message = "Work location type cannot be empty/null")
    @Valid
    private String worklocType;
    @NotNull(message = "Experience minimum cannot be empty/null")
    @NumberFormat
    @Valid
    private Long experienceMin;

    @NotNull(message = "Experience max cannot be empty/null")
    @NumberFormat
    @Valid
    private Long experienceMax;

    @NotNull(message = "Salary minimum  cannot be empty/null")
    @NumberFormat
    @Valid
    private Long salaryMin;

    @NotNull(message = "Salary max  cannot be empty/null")
    @NumberFormat
    @Valid
    private Long salaryMax;

    @NotNull(message = "Job location cannot be empty/null")
    @NumberFormat
    @Valid
    private String location;

    @NotBlank(message = "Job description cannot be empty/null")
    @Valid
    private String description;

    @JsonProperty(access = Access.AUTO)
    private JobStatus status;

    @NotNull(message = "Hide salary cannot be empty/null")
    @Valid
    private boolean hideSalary;

    private boolean unPaid;

    private Integer degree;

    private Integer noticePeriod;

    private Integer contractNoticePeriod;

    @NotNull(message = "Salaray type needs to be HOURLY, DAILY, MONTHLY,ANNUALY,")
    private SalaryType salaryType;

    @NotNull(message = "Currency cannot be empty/null")
    @Valid
    private Currency currency;

    @NotNull(message = "Job vaccancies cannot be empty/null")
    @NumberFormat
    @Valid
    private Long vacancies;

    private Date jobApplyBy;

    private Integer ethnicity;

    private Integer citizenship;

    private String certOrLicenseReq;

    private String benefitOther;

    private String supplementPayOther;

    private String collaboratorId;
    private List<UserDTO> collaboratorsData;

    private Integer enableViewFor;

}
