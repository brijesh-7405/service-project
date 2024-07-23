package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobAppliedFilterDTO {
    private List<Integer> jobFunction;
    private List<String> location;
    private String workMode;
    private List<Integer> jobTypes;
    private List<String> companyName;
    private List<Integer> status;
}
