/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

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
@Table(name = "degrees")
public class Degrees {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "degree_id")
	private Integer degreeId;
	@Column(name = "title")
	private String title;
	@Column(name = "short_title")
	private String shortTitle;
	@Column(name = "status")
	private String status;

}
