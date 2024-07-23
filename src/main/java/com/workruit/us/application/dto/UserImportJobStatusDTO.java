/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.UserImportAsyncStatus.UserImportJobStatus;
import lombok.Data;

/**
 * @author Santosh Bhima
 */
@Data
public class UserImportJobStatusDTO {
    private UserImportJobStatus status;
    private String description;
    private Long totalCount;
    private Long processedCount;
    private Long mobileDuplicates;
    private Long savedCounts;
    private Long emailDuplicates;
}
