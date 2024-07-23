package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Set;

@Data
public class JobQuestionDTO {
    private Long questionId;
    private String questionType;
    private String questionTitle;
    private Set<JobQuestionValuesDTO> questionValues;
    private boolean mandatory;
    private Long sortId;
}
