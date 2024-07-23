package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobApplicantRequestDTO {
    private JobApplicantFilter jobApplicantFilter;
    private List<SortByDTO> sortByDTOS;
}
