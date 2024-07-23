/**
 *
 */
package com.workruit.us.application.models;

import com.workruit.us.application.enums.Currency;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.enums.SalaryType;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Mahesh
 */
@Data
@Entity
@Table(name = "job_post")
public class JobPost implements Serializable {
    private static final long serialVersionUID = -6809400986246478907L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "job_post_id")
    private Long jobPostId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

//	@Column(name = "user_name")
//	private String userName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "jobpost_jobfunctions", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "job_function_id"))
    private Set<JobFunction> jobFunction;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "optional_jobpost_jobfunctions", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "job_function_id"))
    private Set<JobFunction> optionalJobfunctions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_post_skills", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<JobSkills> jobSkills;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private List<JobQuestion> jobQuestion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_post_benefits", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "benefit_id"))
    private Set<Benefits> jobBenefits;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_supplemental_pay", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "supplemental_pay_id"))
    private Set<SupplementalPay> jobSupplementalPay;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_degrees", joinColumns = @JoinColumn(name = "job_post_id"), inverseJoinColumns = @JoinColumn(name = "degree_id"))
    private Set<Degrees> jobDegrees;

    @Column(name = "job_type")
    private Integer jobType;

    @Column(name = "job_type_start_date")
    private Date jobTypeStartDate;

    @Column(name = "job_type_end_date")
    private Date jobTypeEndDate;

    @Column(name = "job_type_duration")
    private Integer jobTypeDuration;

    @Column(name = "job_type_unpaid")
    private boolean jobTypeUnpaid;

    @Column(name = "workloc_value")
    private String worklocValue;

    @Column(name = "workloc_type")
    private String worklocType;

    @Column(name = "experience_min")
    private Long experienceMin;

    @Column(name = "experience_max")
    private Long experienceMax;

    @Column(name = "salary_min")
    private Long salaryMin;

    @Column(name = "salary_max")
    private Long salaryMax;

    @Column(name = "location")
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(name = "hide_salary")
    private boolean hideSalary;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "un_paid")
    private boolean unPaid;

    @Column(name = "degree_id")
    private Integer degree;

    @Column(name = "notice_period")
//	@Enumerated(EnumType.STRING)
    private Integer noticePeriod;

    @Column(name = "contract_notice_period")
    private Integer contractNoticePeriod;

    @Column(name = "activated_date")
    private Date activatedDate;

    @Column(name = "salary_type")
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "vacancies")
    private Long vacancies;

    @Column(name = "job_apply_by")
    private Date jobApplyBy;

    @Column(name = "ethnicity")
    private Integer ethnicity;

    @Column(name = "citizenship_id")
    private Integer citizenship;

    @Column(name = "cert_license_req")
    private String certOrLicenseReq;

    @Column(name = "benefit_other")
    private String benefitOther;

    @Column(name = "supplement_pay_other")
    private String supplementPayOther;

    @Column(name = "collaborator_id")
    private String collaboratorId;

    @Column(name = "enable_view_for")
    private Integer enableViewFor;

    @Column(name = "closed_date")
    private Date closedDate;

}
