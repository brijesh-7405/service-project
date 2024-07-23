/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Santosh Bhima
 */
public interface AlertRepository extends PagingAndSortingRepository<Alert, Long> {
    @Query(value = "select a from Alert a where a.consultancyId=?1 group by a.message")
    Page<Alert> findByConsultancyId(Long consultancyId, Pageable pageable);

    List<Alert> findByUserId(Long userId, Pageable pageable);

    Page<Alert> findByConsultancyIdAndUserId(Long consultancyId, Long userId, Pageable pageable);
}
