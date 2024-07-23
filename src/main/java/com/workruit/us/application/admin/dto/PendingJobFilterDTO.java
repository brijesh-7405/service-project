package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class PendingJobFilterDTO {
    private String location;
    private Integer jobFunction;
    private String postedBy;
}
