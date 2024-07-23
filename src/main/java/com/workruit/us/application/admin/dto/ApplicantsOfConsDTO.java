package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ApplicantsOfConsDTO {
    private long userId;
    private String firstName;
    private String lastName;
    private long profilesUploaded;
}
