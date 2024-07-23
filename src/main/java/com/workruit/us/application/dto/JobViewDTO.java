/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Mahesh
 */
@Data
public class JobViewDTO extends ActivityBaseDTO {
    private long shortlisted;
    private String postedBy;
    private String userName;
    private String jobStatus;
    private String jobFunctionName;
    private String optionalJobFunctionName = "";
    private List<UserDTO> usersList;

}
