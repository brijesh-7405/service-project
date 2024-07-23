/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Embeddable
@MappedSuperclass
public class BaseModel {
	@Column(name = "created_date", insertable = true, updatable = false)
	private Date createdDate;
	@Column(name = "updated_date", insertable = true, updatable = true)
	private Date updatedDate;
}
