/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Applicant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Santosh Bhima
 */
public interface ApplicantRepository extends PagingAndSortingRepository<Applicant, Long> {

    /**
     * @param username
     * @return
     */
    Applicant findByEmail(String username);

    /**
     * @param consultancyId
     * @param applicantId
     * @return applicantResponse
     */

    Optional<Applicant> findByApplicantIdAndConsultancyId(Long applicantId, Long consultancyId);

    Page<Applicant> findByConsultancyUserIdAndVersion(Long consultancyUserId, Long version, Pageable pageable);

    Page<Applicant> findByConsultancyUserId(Long consultancyUserId, Pageable pageable);

    @Query("select u  from Applicant u where u.consultancyUserId in(?1)")
    Page<Applicant> findByConsultancyUserId(List<Long> consultancyUserId, Pageable pageable);

    Page<Applicant> findByConsultancyUserIdAndVersionAndFirstNameContaining(Long consultancyUserId, Long version, String firstName, Pageable pageable);

    Page<Applicant> findByConsultancyUserIdAndFirstNameContaining(Long consultancyUserId, String firstName, Pageable pageable);

    @Query("select u from Applicant u where u.consultancyUserId in(?1) and u.firstName=?2 ")
    Page<Applicant> findByConsultancyUserIdAndFirstNameContaining(List<Long> consultancyUserId, String firstName, Pageable pageable);

    Long countByConsultancyId(Long consultancyId);

    @Query(value = "SELECT count(*) FROM applicant where consultancy_user_id in(?1)", nativeQuery = true)
    Long countByConsultancyId(List<Long> consultancyId);

    @Query(value = "SELECT count(*) FROM applicant app inner join applicant_details ad on ad.applicant_id=app.applicant_id where app.consultancy_id=?1 and ad.current_work_status=?2 and app.consultancy_user_id=?3", nativeQuery = true)
    Long countByCurrentWorkStatus(Long consultancyId, String currentWorkStatus, Long consultantUserId);

    @Query(value = "SELECT count(*) FROM applicant app inner join applicant_details ad on ad.applicant_id=app.applicant_id where app.consultancy_id=?1 and ad.current_work_status=?2 ", nativeQuery = true)
    Long countByCurrentWorkStatus(Long consultancyId, String currentWorkStatus);


    @Transactional
    @Modifying
    @Query(value = "UPDATE applicant SET profile_image_url=?2 WHERE applicant_id=?1", nativeQuery = true)
    void updateProfileImageKey(Long id, String key);

    @Transactional
    @Modifying
    @Query(value = "UPDATE applicant SET resume_upload_id=?2 WHERE applicant_id=?1", nativeQuery = true)
    void updateResumeKey(Long id, String key);

    @Transactional
    @Modifying
    @Query(value = "UPDATE applicant SET passport_upload_id=?2 WHERE applicant_id=?1", nativeQuery = true)
    void updatePassportKey(Long id, String key);

    @Transactional
    @Modifying
    @Query(value = "UPDATE applicant SET upload_additional_doc_id=?2 WHERE applicant_id=?1", nativeQuery = true)
    void updateAdditionalDocKey(Long id, String key);

    Applicant findByPhoneNumber(String phoneNumber);

}
