package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadedApplicantRequestDTO {
    private UploadedApplicantFilterDTO uploadedApplicantFilterDTO;
    private List<SortByDTO> sortByDTO;
}
