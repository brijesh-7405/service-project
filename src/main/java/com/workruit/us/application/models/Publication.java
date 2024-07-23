/**
 * 
 */
package com.workruit.us.application.models;

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
@Entity
@Table(name = "publication")
@Data
public class Publication extends BaseModel {

	@Id
	@Column(name = "publication_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long publicationId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "title")
	private String title;

	@Column(name = "description")
	private String description;

}
