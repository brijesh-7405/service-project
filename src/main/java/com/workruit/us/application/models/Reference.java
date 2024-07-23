/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Santosh Bhima
 *
 */
@Entity
@Table(name = "reference")
@Data
public class Reference extends BaseModel {
    @Id
    @Column(name = "reference_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referenceId;

    @Column(name = "applicant_id")
    private Long applicantId;

    //@Basic(optional = false)
    @Column(name = "name")
    private String name;

    //@Basic(optional = false)
    @Column(name = "email")
    private String email;

    //@Basic(optional = false)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "reference_Type")
    private String referenceType;

    @Column(name = "employer")
    private String employer;

    @Column(name = "title")
    private String title;
}
