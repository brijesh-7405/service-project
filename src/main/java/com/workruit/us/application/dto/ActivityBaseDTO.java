/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Mahesh
 *
 */
@Data
public class ActivityBaseDTO {
    private long jobPostId;
    private String title;
    private String description;
    private int jobType;
    private Date postedOn;
    private Date lastActionDate;
    private long interviewed;
    private long hired;
    private long rejected;
    private long matchedCount;

}
