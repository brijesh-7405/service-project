package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "consultancy_job_status")
public class ConsultancyJobStatus extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "consultancy_job_status_id")
    private Long consultancyJobStatusId;
    @Column(name = "job_post_id")
    private Long jobPostId;
    @Column(name = "consultancy_id")
    private Long consultancyId;
    @Column(name = "consultancy_user_id")
    private Long consultancyUserId;
    @Column(name = "status")
    private int status;

}
