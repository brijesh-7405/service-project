/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
public class JobStatisticsDTO {
    private long jobPostId;
    private Long shortlisted = 0L;
    private Long interviewed = 0L;
    private Long hired = 0L;
    private Long rejected = 0L;

}
