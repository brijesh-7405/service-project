/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "user")
public class User extends BaseModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "user_id")
	private Long userId;

	@Basic(optional = false)
	@NotNull
	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Basic(optional = false)
	@NotNull
	@Column(name = "work_email")
	private String workEmail;

	@Basic(optional = false)
	@NotNull
	@Column(name = "phone_number")
	private String phoneNumber;

	@Basic(optional = false)
	@NotNull
	@Column(name = "password")
	private String password;

	@Column(name = "gender")
	private String gender;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "role_name")
	private String roleName;

	@Column(name = "enabled")
	private boolean enabled = false;

	@Column(name = "company_id")
	private Long companyId;

	@Column(name = "consultancy_id")
	private Long consultancyId;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "department_id")
	private Long departmentId;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "notification_token")
	private String notificationToken;

	@Column(name = "enabled_date")
	private Date enabledDate;


}
