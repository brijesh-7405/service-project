/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

/**
 * @author Santosh Bhima
 */
@Getter
@Setter
public class UserDTO {
    @Null
    private Long userId;
    private String firstName;
    private String lastName;
    private String roleName;
    @Size(min = 1, message = "Work email should be minimum of length 1.")
    private String email;
    private String profilePic;
    private String phoneNumber;
    private String countryCode;
    private Long companyId;
    private Long consultancyId;
    // @NotNull
    private String department;
    private String notificationToken;
}
