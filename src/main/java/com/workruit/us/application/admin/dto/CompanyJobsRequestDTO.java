package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompanyJobsRequestDTO {
    private CompanyJobFilter companyJobFilter;
    private List<SortByDTO> sortBy;
}
