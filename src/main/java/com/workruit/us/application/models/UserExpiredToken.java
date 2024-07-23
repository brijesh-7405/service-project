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

import java.util.Date;

/**
 * @author Santosh
 *
 */
@Table(name = "user_expired_tokens")
@Entity
@Data
public class UserExpiredToken {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	private Long id;
	@Column(name = "user_id")
	private Long userId;
	@Column(name = "access_token")
	private String accessToken;
	@Column(name = "last_active_date")
	private Date lastActiveDate;
}
