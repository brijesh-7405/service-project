package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class TalentFilterDTO {
    private Long jobId;
    private Integer jobFunction;
    private String status;
    private Float expMin;
    private Float expMax;
    private List<String> noticePeriod;
    private List<String> workMode;
    private String location;
    private List<Integer> eduQualification;

    private List<String> jobTypes;

    private List<String> citizenship;

    private List<Integer> jobSkills;

}
