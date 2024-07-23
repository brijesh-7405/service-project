package com.workruit.us.application.admin.dto;

import com.workruit.us.application.admin.enums.OrderBy;
import lombok.Data;

@Data
public class SortByDTO {
    private String columnName;
    private OrderBy orderBy;
}
