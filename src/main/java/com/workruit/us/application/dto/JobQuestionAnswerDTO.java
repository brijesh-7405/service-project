package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Set;

@Data
public class JobQuestionAnswerDTO {

    private Long questionId;
    private String questionTitle;
    private String questionType;
    private Set<String> questionAns;

}
