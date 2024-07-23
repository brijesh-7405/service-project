package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicantFilterDTO
{
    private List<Integer> jobFunction;
    private String location;
    private List<String> jobTypes;
    private List<String> currentWorkStatus;
    private List<String> preferredWorkModes;
    private List<String> careerLevel;
    private Float yearsOfExp;
    private String citizenship;
    private List<String> eduQualification;
    private List<Long> userIds;

}
