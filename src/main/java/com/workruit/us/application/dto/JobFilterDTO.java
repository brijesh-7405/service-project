package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JobFilterDTO {

    private Long userId;
    private Long jobPostId;
    private String title;
    private List<Integer> jobFunction = new ArrayList<>();
    private List<Integer> optionalJobfunctions = new ArrayList<>();
    private List<Integer> jobSkills = new ArrayList<>();
    private List<Integer> jobDegrees = new ArrayList<>();
    private List<Integer> jobSupplementalPay = new ArrayList<>();
    private String worklocValue;
    private String worklocType;
    private Long experienceMin;
    private Long experienceMax;
    private String location;
    private Integer citizenship;
    private Integer jobType;
    private Integer noticePeriod;
    private Integer contractNoticePeriod;

}
