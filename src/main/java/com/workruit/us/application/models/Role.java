/**
 *
 */
package com.workruit.us.application.models;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "role")
public class Role extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "role_id")
    private Long roleId;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;

    public enum Roles {
        CONSULTANCY_ADMIN, COMPANY_ADMIN, CONSULTANCY_MANAGER, HR_MANAGER, AGENT
    }
}
