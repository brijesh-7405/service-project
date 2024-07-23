/**
 *
 */
package com.workruit.us.application.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santosh
 */
@Data
public class UserDetailsDTO {
    private long id;
    private String email;
    private String name;
    private String firstName;
    private String LastName;
    private String code;
    private String phone;
    private String roleName;
    private String companyName;
    private boolean enabledRole;
    private String companyImage;
    private String companyLocation;
    private Map<String, Object> attributes = new HashMap<>();
    private Long consultancyId;
    private Long companyId;
    private List<String> roles = new ArrayList<>();
    private Long departmentId;
    private boolean enableAsConsultant = false;
    private boolean emailVerified = false;
    private boolean profileDetailsNotPresent = false;
}