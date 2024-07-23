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
@Table(name = "department")
public class Department extends BaseModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "department_id")
	private Long departmentId;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

}
