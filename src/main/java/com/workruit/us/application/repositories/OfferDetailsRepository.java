package com.workruit.us.application.repositories;

import com.workruit.us.application.models.OfferDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Mahesh
 */
@Repository
public interface OfferDetailsRepository extends JpaRepository<OfferDetails, Long> {
    OfferDetails findByJobPostIdAndApplicantId(long jobPostId, long applicantId);

    @Query(value = "select j from OfferDetails j where j.jobPostId =?1 and j.applicantId = ?2 and j.offerStatus=?3")
    OfferDetails findByJobPostIdAndApplicantId(long jobPostId, long applicantId, int status);

    @Query(value = "select j from OfferDetails j where j.jobPostId =?1 and j.applicantId = ?2 and j.offerStatus in(?3) ")
    OfferDetails findByJobPostIdAndApplicantId(long jobPostId, long applicantId, List<Integer> status);

    @Query(value = "select j from OfferDetails j where j.jobPostId =?1 and j.applicantId = ?2 ")
    OfferDetails findByJobPostIdAndApplicantIds(long jobPostId, long applicantId);

    @Query(value = "select j from OfferDetails j where j.applicantId =?1 and j.jobPostId=?2 and j.recruiterId=?3")
    OfferDetails findByJobPostIdAndApplicantId(long applicantId, long jobPostId, long recruiterId);

    @Query(value = "select j from OfferDetails j where j.applicantId =?1 and j.jobPostId=?2 and j.recruiterId=?3 and j.offerStatus in(?4)")
    OfferDetails findByJobPostIdAndApplicantId(long applicantId, long jobPostId, long recruiterId, long status);


    @Query(value = "select j from OfferDetails j where j.applicantId =?1 and j.jobPostId=?2 and j.recruiterId in(?3)")
    OfferDetails findByJobPostIdAndReceuiterIds(long applicantId, long jobPostId, List<Long> recruiterId);

}
