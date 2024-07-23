package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class JobRequestDTO {
    private JobAppliedFilterDTO jobFilterDTO;
    private List<SortByDTO> sortByDTO;
}
