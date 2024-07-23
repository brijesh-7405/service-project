package com.workruit.us.application.dto;

import lombok.Data;

@Data
public class RecommendedProfilesDTO {
    private AppledProfilesDTO applicantProfilesDTO;
    private int score;
}
