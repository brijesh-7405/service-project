package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class ApplicantStatusHistoryDTO {

    private String status;
    private String updatedBy;
    private String dateOfLastUpdate;

}
