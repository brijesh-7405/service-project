package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicantProfileRequestDTO {
    private ApplicantProfileFilterDTO applicantProfileFilterDTO;
    private List<SortByDTO> sortByDTO;
}
