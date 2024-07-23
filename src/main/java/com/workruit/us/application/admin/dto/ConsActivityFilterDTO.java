package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsActivityFilterDTO {
    private List<String> companyName;
    private List<Long> appliedBy;
}
