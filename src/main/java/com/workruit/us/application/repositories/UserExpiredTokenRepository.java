/**
 * 
 */
package com.workruit.us.application.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workruit.us.application.models.UserExpiredToken;

/**
 * @author Santosh
 *
 */
public interface UserExpiredTokenRepository extends JpaRepository<UserExpiredToken, Long> {
	Long countByUserIdAndAccessToken(Long userId, String accessToken);
}
