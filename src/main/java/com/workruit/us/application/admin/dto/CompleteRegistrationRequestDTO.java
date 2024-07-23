package com.workruit.us.application.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompleteRegistrationRequestDTO {
    private CompleteRegistrationFilter completeRegistrationFilter;
    private List<SortByDTO> sortByDTO;
}
