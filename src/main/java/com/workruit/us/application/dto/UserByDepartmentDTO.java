package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserByDepartmentDTO {
    private long totalCount = 0;
    private long totalPages = 0;
    List<DepartmentUserDto> users=new ArrayList<>();
}
