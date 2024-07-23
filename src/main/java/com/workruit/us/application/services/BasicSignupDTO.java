/**
 *
 */
package com.workruit.us.application.services;

import lombok.Data;

/**
 * @author Santosh Bhima
 */
@Data
public class BasicSignupDTO {
    private String email;
    private String department;
    private String mobileNumber;
    private String expires;
    private Long userVerificationId;
    private Integer invitedCount;
}
