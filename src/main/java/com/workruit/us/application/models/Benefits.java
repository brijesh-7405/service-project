/**
 * 
 */
package com.workruit.us.application.models;

import java.io.Serializable;

import javax.persistence.Basic;
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
@Table(name = "benefits")
public class Benefits implements Serializable {

	private static final long serialVersionUID = -2749778781703046662L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "benefit_id")
	private Integer benefitId;
	
	@Column(name = "benefit_name")
	private String benefitName;
	
	@Column(name = "status")
	private boolean status;
	
}
