package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchedJobsFilter {
    private Integer jobFunction;
    private List<String> worklocValue;

    private List<Integer> noticePeriod;

    private List<String> locations;

    private List<Integer> jobTypes;
    private List<Integer> jobSkills;

    private List<Integer> citizenship;

    private List<Integer> eduQualification;

    private Integer jobId;

}
