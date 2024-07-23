/**
 * 
 */
package com.workruit.us.application.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.workruit.us.application.models.Consultancy;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Santosh Bhima
 *
 */
public interface ConsultancyRepository extends PagingAndSortingRepository<Consultancy, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE consultancy SET profile_image_url=?2 WHERE consultancy_id=?1", nativeQuery=true)
    void updateProfileImageKey(Long id, String key);
}
