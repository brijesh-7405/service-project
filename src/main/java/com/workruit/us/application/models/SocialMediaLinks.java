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
@Table(name = "social_media_links")
public class SocialMediaLinks extends BaseModel {
	@Id
	@Column(name = "social_media_links_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long socialMediaLinksId;

	@Column(name = "applicant_id")
	private Long applicantId;

	@Column(name = "github_link")
	private String githubLink;

	@Column(name = "linkedin_link")
	private String linkedinLink;

	@Column(name = "website_link")
	private String websiteLink;

	@Column(name = "twitter_link")
	private String twitterLink;

	@Column(name = "blog_link")
	private String blogLink;

	@Column(name = "behance_link")
	private String behanceLink;

	@Column(name = "facebook_link")
	private String facebookLink;


}
