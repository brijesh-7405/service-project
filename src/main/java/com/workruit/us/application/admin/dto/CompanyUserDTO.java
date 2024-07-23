package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class CompanyUserDTO {
    private long userId;
    private String firstName;
    private String lastName;
    private String workEmail;
    private String createdDate;
    private String isActivated;
    private String enabledAccountDate;
    private String updatedProfile;
    private String contactNumber;
    private String role;
}
