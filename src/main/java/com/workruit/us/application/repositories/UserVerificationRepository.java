/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.UserVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Santosh
 */
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    UserVerification findByUserIdAndOtp(Long userId, String otp);

    UserVerification findByUserId(Long userId);

    @Query(value = "SELECT * FROM user_verification where consultancy_id=?1 and user_id !=?2 and role_name=?3 order by updated_date desc", nativeQuery = true)
    Page<UserVerification> findUsersByConsultancyId(Long consultancyId, Long userId, String role, Pageable page);

    Page<UserVerification> findByConsultancyId(Long consultancyId, Pageable page);

    Page<UserVerification> findByUserId(Long userId, Pageable page);

}
