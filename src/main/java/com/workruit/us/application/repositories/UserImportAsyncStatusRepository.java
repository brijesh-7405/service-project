/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.UserImportAsyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Santosh Bhima
 */
public interface UserImportAsyncStatusRepository extends JpaRepository<UserImportAsyncStatus, Long> {
    UserImportAsyncStatus findByUserImportAsyncStatusIdAndUserId(Long id, Long userId);

    @Query(value = "SELECT max(version) FROM user_import_status where user_id=?1", nativeQuery = true)
    Long findVersionByUserImportAsyncUserId(Long userId);

    @Query(value = "SELECT max(user_import_status_id) FROM user_import_status where  user_id=?1 ", nativeQuery = true)
    Long findIdByUserImportAsyncUserId(Long userId);

}
