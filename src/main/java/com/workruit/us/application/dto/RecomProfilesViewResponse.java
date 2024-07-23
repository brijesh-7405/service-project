package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecomProfilesViewResponse {
    private List<RecommendedProfilesDTO> recommendedProfilesDTO;
    private long totalCount;
    private long totalPages;
}
