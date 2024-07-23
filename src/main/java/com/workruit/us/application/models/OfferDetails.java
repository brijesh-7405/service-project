/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Mahesh
 */
@Data
@Table(name = "offer_details")
@Entity
public class OfferDetails extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_details_id")
    private Long offerDetailsId;

    @Column(name = "applicant_id")
    private Long applicantId;

    @Column(name = "job_post_id")
    private Long jobPostId;

    @Column(name = "recruiter_id")
    private Long recruiterId;
    @Column(name = "consultancy_id")
    private Long consultancyId;

    @Column(name = "joining_date")
    private Date joiningDate;
    @Column(name = "offer_signed_date")
    private Date offerSignedDate;
    @Column(name = "offer_status")
    private int offerStatus;

    @Column(name = "offer_url")
    private String offerUrl;
    @Column(name = "offer_signed_url")
    private String offerSignedUrl;
}
