package com.workruit.us.application.dto;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class UpdateApplicantDTO {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String location;
    private String skills;
    private String country;
    private Date dob;
    private String phoneNumber;
    private String ethnicity;
    private String profileSummary;
    @NotNull
    private String email;
    @NotNull
    private String gender;
    private boolean enabled = false;
    private String profileImageUrl;
    private String password;
    private String resumeUploadId;
    private String languages;
    private String resumeURL;
    private String resumeVideoURL;
    private String passportUploadId;
    private String uploadAdditionalDocId;
    private String uploadId;
    private String countryCode;
    private String number;
}
