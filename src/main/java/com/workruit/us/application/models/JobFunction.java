/**
 * 
 */
package com.workruit.us.application.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * @author Mahesh
 *
 */
@Data
@Entity
@Table(name = "job_function")
public class JobFunction implements Serializable {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "job_function_id")
	private Integer jobFunctionId;

	@Column(name = "job_function_name")
	private String jobFunctionName;

	@Column(name = "status")
	private int status;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "updated_date", nullable = true)
	private Date updatedDate;

	/*
	 * @Column(name = "category_id") private int categoryId;
	 */
}
