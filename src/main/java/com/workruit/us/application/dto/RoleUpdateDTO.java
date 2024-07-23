/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author Santosh Bhima
 *
 */
@Getter
@Setter
public class RoleUpdateDTO {

    @NotNull
    private String role;
    @NotNull
    private long userId;
}
