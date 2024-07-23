/**
 * 
 */
package com.workruit.us.application.repositories;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.workruit.us.application.models.UserRole;

/**
 * @author Santosh Bhima
 *
 */
public interface UserRoleRepository extends PagingAndSortingRepository<UserRole, Long> {
	List<UserRole> findByUserId(Long userId);
}
