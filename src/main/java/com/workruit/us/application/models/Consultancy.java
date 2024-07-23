/**
 * 
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "consultancy")
public class Consultancy extends BaseModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "consultancy_id")
	private Long consultancyId;

	@Column(name = "name")
	private String name;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "website")
	private String website;

	@Column(name = "about")
	private String about;

	@Column(name = "location")
	private String location;

	@Column(name = "founded_date")
	private Date foundedDate;

	@Column(name = "industry_Types")
	private String industryTypes;

	@Column(name = "number_of_employees")
	private String numberOfEmployees;

	private String domains;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "consultancy_id", nullable = false)
	private Set<Client> clients;

	@Column(name = "number_of_applicants_hired")
	private Long numberOfApplicantsHired;

	@Column(name = "facebook_link")
	private String facebookLink;

	@Column(name = "twitter_link")
	private String twitterLink;

	@Column(name = "linkedin_link")
	private String linkedinLink;

	@Column(name = "overall_talent_pool")
	private String overallTalentPool;
}
