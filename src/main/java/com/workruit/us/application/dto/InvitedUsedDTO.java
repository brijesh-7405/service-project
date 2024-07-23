package com.workruit.us.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class InvitedUsedDTO {

    List<UserDTO> users;
    private long totalCount;
    private long totalPages;

}
