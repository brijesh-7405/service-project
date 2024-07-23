/**
 * 
 */
package com.workruit.us.application.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.workruit.us.application.models.Role;

/**
 * @author Santosh Bhima
 *
 */
public interface RoleRepository extends PagingAndSortingRepository<Role, Long> {
	Role findByName(String name);
}
