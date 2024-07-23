/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "user_role")
public class UserRole extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_role_id")
    private Long userRoleId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "enabled")
    private boolean enabled = false;

    @Column(name = "role_id")
    private Long roleId;

}
