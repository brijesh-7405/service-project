/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Santosh Bhima
 *
 */
@Entity
@Data
@Table(name = "internship")
public class Internship extends BaseModel {

    @Id
    @Column(name = "internship_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internshipId;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "company")
    private String companyName;

    @Column(name = "location")
    private String location;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "description")
    private String description;

    @Column(name = "currently_working_here")
    private boolean currentlyWorkingHere;
}
