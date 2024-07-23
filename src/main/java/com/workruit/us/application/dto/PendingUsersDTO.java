package com.workruit.us.application.dto;

import java.util.List;

import com.workruit.us.application.services.BasicSignupDTO;

import lombok.Data;

@Data
public class PendingUsersDTO {

    List<BasicSignupDTO> basicSignupDTO;
    private long totalCount;
    private long totalPages;
}
