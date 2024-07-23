/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mahesh
 *
 */
@Data
public class JobViewResponse {
    List<JobViewDTO> jobViewDTO = new ArrayList<>();
    private long totalCount;
    private long totalPages;
    private long activeCount;
    private long pendingCount;
    private long closedCount;
}
