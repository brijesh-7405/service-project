package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class OverallAlertDTO {
    private long id;
    private String msg;
    private String companyName;
    private String alertDate;
}
