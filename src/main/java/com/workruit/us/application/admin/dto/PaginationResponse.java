package com.workruit.us.application.admin.dto;

import lombok.Data;

@Data
public class PaginationResponse {
    private Object data;
    private long totalCount = 0;
    private long totalPages = 0;
}
