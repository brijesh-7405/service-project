package com.workruit.us.application.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Table(name = "user_verification")
@Entity
@Getter
@Setter
public class UserVerification extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_verification_id")
    private Long userVerificationId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "email_otp_code")
    private String otp;
    @Column(name = "created_date")
    private Date createdDate;
    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "resend_invite")
    private Integer resendInvite;
    @Column(name = "consultancy_id")
    private Long consultancyId;
    @Column(name = "role_name")
    private String roleName;
}
