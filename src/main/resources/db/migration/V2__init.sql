CREATE TABLE `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(255) NOT NULL,
  `last_name` VARCHAR(255) NOT NULL,
  `work_email` VARCHAR(255) NOT NULL,
  `dob` DATE NULL,
  `phone_number` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `gender` VARCHAR(32) NULL,
  `profile_image_url` TEXT NULL,
  `enabled` TINYINT(4) NULL,
  `company_id` BIGINT,
  `consultancy_id` BIGINT,
  `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`));
  
  CREATE TABLE `role` (
  	`role_id` BIGINT NOT NULL AUTO_INCREMENT,
  	`name` VARCHAR(255) NOT NULL,
  	`description` TEXT,
  	`created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	`updated_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  	PRIMARY KEY(`role_id`)
  );
  
 CREATE TABLE `user_role` (
  	`user_role_id` BIGINT NOT NULL AUTO_INCREMENT,
  	`role_id` BIGINT NOT NULL,
  	`user_id` BIGINT NOT NULL,
  	`created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	`updated_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  	PRIMARY KEY(`user_role_id`)
  );
  
 CREATE TABLE `user_verification` (
  	`user_verification_id`	BIGINT NOT NULL AUTO_INCREMENT,
  	`user_id` BIGINT NOT NULL,
  	`email_otp_code`	VARCHAR(32) NOT NULL,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	`updated_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  	PRIMARY KEY(`user_verification_id`)
  );
  
 CREATE TABLE `consultancy` (
 	`consultancy_id` BIGINT NOT NULL AUTO_INCREMENT,
 	`name` VARCHAR(1024) NULL,
 	`profile_image_url`	VARCHAR(2048) NULL,
 	`website` VARCHAR(2048) NULL,
 	`location` VARCHAR(2048) NULL,
 	`about` TEXT NULL,
 	`founded_date` DATE,
 	`industry_types` VARCHAR(2048) NULL,
 	`number_of_employees` BIGINT NULL,
 	`domains` VARCHAR(2048) NULL,
 	`clients` VARCHAR(2048) NULL,
 	`number_of_applicants_hired` BIGINT NULL,
 	 `facebook_link` VARCHAR(512) NULL AFTER `updated_date`,
 	`twitter_link` VARCHAR(512) NULL AFTER `facebook_link`,
	 `linkedin_link` VARCHAR(512) NULL AFTER `twitter_link`,
    `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	`updated_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 	 PRIMARY KEY(`consultancy_id`)
  );