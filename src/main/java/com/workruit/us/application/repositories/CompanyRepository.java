/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Company;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Santosh Bhima
 */
public interface CompanyRepository extends PagingAndSortingRepository<Company, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE company SET profile_image_url=?2 WHERE company_id=?1", nativeQuery = true)
    void updateProfileImageKey(Long id, String key);

    @Transactional
    @Modifying
    @Query(value = "DELETE from company_client  WHERE client_id=?1", nativeQuery = true)
    void deleteClientById(Long id);

    @Query(value = "SELECT name FROM company where name LIKE ?1%", nativeQuery = true)
    List<String> getCompanyNameOnSearch(String name);

}
