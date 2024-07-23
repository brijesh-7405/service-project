package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "applicant_secondary_job_function")
@Entity
public class ApplicantSecondaryJobFunction extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applicant_secondary_job_function_id")
    private Long applicantSecondaryJobFunctionId;

    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "job_function_id")
    private Integer jobFunctionId;
}
