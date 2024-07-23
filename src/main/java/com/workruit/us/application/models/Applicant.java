/**
 * 
 */
package com.workruit.us.application.models;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.workruit.us.application.enums.ApplicantStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "applicant")
public class Applicant extends BaseModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "applicant_id")
	private Long applicantId;

	@Basic(optional = false)
	@NotNull
	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Basic(optional = false)
	@NotNull
	@Column(name = "email")
	private String email;

	@Basic(optional = false)
	@NotNull
	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "gender")
	private String gender;

	@Column(name = "enabled")
	private boolean enabled = false;

	@Column(name = "skills")
	private String skills;

	@Column(name = "country")
	private String country;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Basic(optional = false)
	@Column(name = "password")
	private String password;

	@Column(name = "resume_upload_id")
	private String resumeUploadId;

	@Column(name = "languages")
	private String languages;

	@Column(name = "resume_url")
	private String resumeURL;

	@Column(name = "resume_video_url")
	private String resumeVideoURL;

	@Column(name = "passport_upload_id")
	private String passportUploadId;
	@Column(name = "upload_additional_doc_id")
	private String uploadAdditionalDocId;

	@Column(name = "upload_id")
	private String uploadId;

	@Column(name = "consultancy_id")
	private Long consultancyId;

	@Column(name = "profile_summary")
	private String profileSummary;

	@Column(name = "dob")
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date dob;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "number")
	private String number;

	@Column(name = "location")
	private String location;

	@Column(name = "ethnicity")
	private String ethnicity;

	@Column(name = "version")
	private Long version;

	@Column(name = "Status")
	private int status;

	@Column(name = "consultancy_user_id")
	private Long consultancyUserId;

	@Column(name = "correction_required")
	private boolean correctionRequired = false;
}
