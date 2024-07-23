package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadedApplicantFilterDTO {
    private String location;
    private List<Integer> jobFunction;
    private String gender;
    private List<String> ethnicity;
    private List<String> citizenship;
    private Float expMin;
    private Float expMax;
    private List<String> careerLevel;
    private List<String> workMode;
    private List<String> jobTypes;
    private List<String> noticePeriod;
    private List<String> currentStatus;
    private List<Long> uploadedBy;
}
