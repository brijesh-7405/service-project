/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.*;
import com.workruit.us.application.models.JobMatchConsultancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Mahesh
 */
public interface JobMatchConsultancyRepository extends JpaRepository<JobMatchConsultancy, Long> {

//	@Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and limit=?3")
//	List<JobMatchConsultancy> findByJobPostIdAndConsultancyId(long jobPostId, long consultancyId, int limit);

    Page<JobMatchConsultancy> findByJobPostIdAndConsultancyIdAndConsultancyUserId(long jobPostId, long consultancyId, long consultancyUserId, Pageable pageable);

//    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.consultancyUserId in(?3) and j.applicantJobStatus = 0 order by j.applicantStatus DESC,j.matchScore DESC")
//    Page<JobMatchConsultancy> findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(long jobPostId, long consultancyId, List<Long> consultancyUserId, Pageable pageable);

    @Query(value = "select distinct j.applicantId as applicantId ,j.applicantStatus as applicantStatus,j.matchScore as matchScore from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.consultancyUserId in(?3) and j.applicantJobStatus = 0 and  j.applicantStatus in(0,1) ")
    Page<ConsApplicantStatusResultSet> findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(long jobPostId, long consultancyId, List<Long> consultancyUserId, Pageable pageable);

    @Query(value = "SELECT updated_by_cons_user_id as updatedByConsUserId,job_post_id as jobPostId,COUNT(CASE WHEN interview_status = 2 THEN 1 END) AS interview,COUNT(CASE WHEN interview_status = 7 THEN 1 END) AS hired "
            + ",COUNT(CASE WHEN interview_status = 8 THEN 1 END) AS rejected,COUNT(CASE WHEN applicant_job_status = 1 THEN 1 END) AS applied, Max(updated_date) as updatedDate "
            + "FROM job_match_consultancy where updated_by_cons_user_id in (?1) "
            + "GROUP BY job_post_id ORDER BY Max(updated_date) DESC", nativeQuery = true)
    List<ConsActivityJobResultSet> countConsJobStatus(List<Long> userIds, Pageable pageable);

    @Query(value = "SELECT updated_by_cons_user_id  FROM job_match_consultancy where job_post_id=?1 ORDER BY CASE WHEN updated_date IS NULL THEN 1 ELSE 0 END, updated_date DESC limit 1", nativeQuery = true)
    Long findRecentUpdatedUserId(long jobId);

    @Query(value = "SELECT count(*) from (SELECT updated_by_cons_user_id as updatedByConsUserId,job_post_id as jobPostId,COUNT(CASE WHEN interview_status = 2 THEN 1 END) AS interview,COUNT(CASE WHEN interview_status = 7 THEN 1 END) AS hired\n"
            + ",COUNT(CASE WHEN interview_status = 8 THEN 1 END) AS rejected,COUNT(CASE WHEN applicant_job_status = 1 THEN 1 END) AS applied "
            + "FROM job_match_consultancy where updated_by_cons_user_id in (?1)"
            + "GROUP BY updated_by_cons_user_id,job_post_id) tbl", nativeQuery = true)
    Long totalCountConsJobStatus(List<Long> userIds);

    @Query(value = "select * from job_match_consultancy where job_post_id=?1 and applicant_status=0 and applicant_job_status=0 and saved_applicant=0 and saved_recruiter=0", nativeQuery = true)
    List<JobMatchConsultancy> findByJobPostIdWithNoAction(Long jobPostId);

    @Query(value = "select * from job_match_consultancy where job_post_id=?1 and (applicant_status=1 or applicant_job_status=1) and interview_status in(0,1) and saved_applicant=0 and saved_recruiter=0", nativeQuery = true)
    List<JobMatchConsultancy> findByJobPostIdWithAction(Long jobPostId);


    @Query(value = "select * from job_match_consultancy where applicant_id=?1 and applicant_status=0 and applicant_job_status=0 and saved_applicant=0 and saved_recruiter=0", nativeQuery = true)
    List<JobMatchConsultancy> findByUserIddWithNoAction(Long applicantId);

    @Query(value = "select * from job_match_consultancy where applicant_id=?1 and (applicant_status=1 or applicant_job_status=1) and interview_status in(0,1) and saved_applicant=0 and saved_recruiter=0", nativeQuery = true)
    List<JobMatchConsultancy> findByUserIddWithAction(Long applicantId);

    JobMatchConsultancy findByJobPostIdAndApplicantId(long jobPostId, long applicantId);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.applicantId in (?2) and j.applicantJobStatus = 0")
    List<JobMatchConsultancy> getProfilesByIds(long jobId, List<Long> applicantIds);


//    @Query(value = "select j from JobMatchConsultancy j where j.updatedByRecId in (?1) and j.jobPostId in(?2)")
//    List<JobMatchConsultancy> findDashboardStats(List<Long> userIds, List<Long> jobIds);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId in (?1)")
    List<JobMatchConsultancy> findDashboardStats(List<Long> jobIds);

    @Query(value = "select j from JobMatchConsultancy j where j.updatedByConsUserId in (?1) and  j.interviewStatus!=0")
    List<JobMatchConsultancy> findDashboardConsStats(List<Long> userIds);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId=?1 and j.updatedByConsUserId in (?2) and  j.interviewStatus!=0")
    List<JobMatchConsultancy> findDashboardConsStats(Long jobId, List<Long> userIds);

//    @Query(value = "select COUNT(DISTINCT j.jobMatchConId) from JobMatchConsultancy j where j.updatedByRecId in (?1) and j.jobPostId in(?2) and j.applicantStatus=1 and j.applicantJobStatus = 1 and j.interviewStatus in(0,1)")
//    Long findDashboardMatchStats(List<Long> userIds, List<Long> jobIds);

    @Query(value = "select COUNT(DISTINCT j.jobMatchConId) from JobMatchConsultancy j where  j.jobPostId in(?1) and j.applicantStatus=1 and j.applicantJobStatus = 1 and j.interviewStatus in(0,1)")
    Long findDashboardMatchStats(List<Long> jobIds);

    @Query(value = "select COUNT(DISTINCT j.jobMatchConId) from JobMatchConsultancy j where  j.updatedByConsUserId in(?1) and j.applicantStatus=1 and j.applicantJobStatus = 1 and j.interviewStatus in(0,1)")
    Long findDashboardMatchConsStats(List<Long> userIds);

    @Query(value = "select COUNT(DISTINCT j.jobMatchConId) from JobMatchConsultancy j where j.jobPostId=?1 and j.updatedByConsUserId in(?2) and j.applicantStatus=1 and j.applicantJobStatus = 1 and j.interviewStatus in(0,1)")
    Long findDashboardMatchConsStats(Long jobId, List<Long> userIds);


    List<JobMatchConsultancy> findByConsultancyId(Long consultancyId);

    @Query(value = " select consultancy_id as consultancyId, Max(applicant_job_status) as applicantJobStatus,job_post_id as jobPostId, COUNT(CASE WHEN applicant_id  THEN 1 END) as applicantId " +
            " FROM job_match_consultancy where job_post_id =?1  order by consultancy_id ", nativeQuery = true)
    Page<MatchedProfilesResultSet> findMatchedProfilesResultSet(long jobPostId, Pageable pageable);

//    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId=?1 and j.applicantStatus=0 and j.applicantJobStatus in (0,1)  and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.status =2 and j.consultancyUserId=a.consultancyUserId) and j.interviewStatus in(0,1)  group by j.consultancyId order by Max(j.applicantJobStatus) DESC, count(j.applicantId) DESC ")
//    Page<JobMatchConsultancy> findJobMatchesByJobPostId(long jobPostId, Pageable pageable) throws Exception;

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId=?1 and j.applicantStatus=0 and j.applicantJobStatus in (0,1) and j.isSavedByRecruiter=0  and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.status =2 and j.consultancyUserId=a.consultancyUserId) and j.interviewStatus in(0,1)  group by j.consultancyId order by Max(j.applicantJobStatus) DESC,Max(j.matchScore) DESC,count(j.applicantId) DESC")
    Page<JobMatchConsultancy> findJobMatchesByJobPostId(long jobPostId, Pageable pageable) throws Exception;

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId=?1 and j.applicantStatus=0 and j.interviewStatus in(0,1) and j.applicantJobStatus=0 and j.consultancyId not in(?2) group by j.consultancyId order by count(j.applicantId) DESC")
    Page<JobMatchConsultancy> findJobMatchesByJobPostId(long jobPostId, List<Long> consultantIds, Pageable pageable) throws Exception;

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId=?1 and j.applicantStatus=0 and j.interviewStatus in(0,1) and j.applicantJobStatus=1 group by j.consultancyId order by count(j.applicantId) DESC")
    Page<JobMatchConsultancy> findIntrestedJobMatchesByJobPostId(long jobPostId, Pageable pageable) throws Exception;

    @Query(value = "select count(j.applicantId) from JobMatchConsultancy j where j.jobPostId=?1 and j.consultancyId=?2 and j.applicantStatus=0 and j.applicantJobStatus in (0,1) and j.isSavedByRecruiter=0")
    Long findCountofConsultants(long jobPostId, long consultantId);

//    @Query(value = "select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,applicantStatus, savedRecruiter  from " +
//            "(SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, recruiter_id " +
//            " as recruiterId, applicant_job_status as applicantStatus, saved_recruiter as savedRecruiter  FROM job_match_consultancy  " +
//            " where job_post_id=?1 and applicant_status=0 and interview_status in(0,1)) tbl ", nativeQuery = true)
//    Page<JobMatchStateResultSet> findJobMatchesByJobPostId(long jobPostId, Pageable pageable) throws Exception;

    List<JobMatchConsultancy> findByRecruiterIdAndConsultancyId(Long recruiterId, Long consultancyId);

    List<JobMatchConsultancy> findByRecruiterIdAndConsultancyIdAndJobPostId(Long recruiterId, Long consultancyId,
                                                                            Long jobPostId);

    Page<JobMatchConsultancy> findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(long jobId, long recruiterId,
                                                                                 boolean b, Pageable pageable);

    Page<JobMatchConsultancy> findByJobPostIdAndConsultancyId(long jobPostId, long consultancyId, Pageable pageable);

    @Query(value = "select count(j) from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantJobStatus = ?3 and j.applicantStatus=0 and j.isSavedByRecruiter=0 and j.interviewStatus in(0,1) and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.status in (2) and j.jobPostId=a.jobPostId )")
    Long getProfilesByInterviewStatus(long jobId, long consultancyId, int status);

    @Query(value = "select count(j) from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantStatus !=2 and j.isSavedByRecruiter=0 and  j.applicantJobStatus !=2 and j.interviewStatus in(0,1) and j.matchScore>95")
    Long getProfilesByRecommendedStatus(long jobId, long consultancyId);

    //and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.status in (1,2) and j.jobPostId=a.jobPostId )
    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantStatus=0 and j.applicantJobStatus in(0,1) and isSavedByRecruiter=0 " +
            "and j.interviewStatus in(0,1)  order by applicantJobStatus DESC, j.matchScore DESC")
    Page<JobMatchConsultancy> getProfilesByJobStatus(long jobId, long consultancyId, Pageable pageable);


    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantJobStatus=1 and j.applicantStatus=0 " +
            "and j.interviewStatus in(0,1) order by applicantStatus DESC, j.matchScore DESC")
    Page<JobMatchConsultancy> getProfilesByJobStatusWithApplicantStatus(long jobId, long consultancyId, Pageable pageable);

    @Query(value = "select j.updatedDate from JobMatchConsultancy j where j.jobPostId =?1 and (j.applicantStatus !=0 or j.applicantJobStatus !=0) group by j.updatedDate  order by Max(j.updatedDate) DESC")
    List<Date> geJobMatchedUpdatedDate(Long jobPostId);

    @Query(value = "select j from JobMatchConsultancy j where j.recruiterId =?1 and j.isSavedByRecruiter = ?2 ")
    Page<JobMatchConsultancy> findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(long recruiterId,
                                                                                 boolean savedJobStatus, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where j.recruiterId =?1 and j.isSavedByRecruiter = ?2 and applicantJobStatus=?3")
    Page<JobMatchConsultancy> findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(long recruiterId,
                                                                                 boolean savedJobStatus, int applicantJobStatus, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantStatus = ?3 and j.applicantJobStatus=?4 and j.interviewStatus in(0,1)")
    Page<JobMatchConsultancy> findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(long jobId, long consultancyId,
                                                                                 int applicantStatus, int applicantJobStatus, Pageable pageable);

    //and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.status in (1,2) and j.jobPostId=a.jobPostId )
    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantStatus =0 and j.applicantJobStatus =?3 and j.interviewStatus in(0,1) and isSavedByRecruiter=0  order by j.matchScore DESC")
    Page<JobMatchConsultancy> findByJobPostIdAndRecruiterIdAndIsIntrestedByUser(long jobId, long consultancyId, int applicantJobStatus, Pageable pageable);


    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,applicantStatus, savedRecruiter, updatedDate ,recruiterUpdatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate, recruiter_updated_date as recruiterUpdatedDate FROM job_match where  saved_recruiter =1 and updated_by_rec_id in(?1) and interview_status in(0,1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  " + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, updated_date as updatedDate, recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and updated_by_rec_id in(?1) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1)  " +
            "  and job_post_id not in (select a.job_post_id from consultancy_job_status a where a.status =2 and consultancy_id=a.consultancy_id) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select recruiterUpdatedDate  from ("
            + "SELECT job_match_id as jobMatchId,Null as recruiterUpdatedDate FROM job_match where  applicant_status=1 and saved_recruiter =1 and updated_by_rec_id in(?1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and updated_by_rec_id in(?1) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1)  "
            + " and job_post_id not in (select a.job_post_id from consultancy_job_status a where a.status =2 and consultancy_id=a.consultancy_id) " +
            ") tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJob(List<Long> userIds, int applicantStatus, int applicantJobStatus,
                                                        Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId ,applicantStatus, savedRecruiter, updatedDate  from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId , applicant_job_status as applicantStatus,Null as savedRecruiter, updated_date as updatedDate FROM job_match where  saved_recruiter =1 and updated_by_rec_id in(?1) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,saved_recruiter as savedRecruiter, updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and updated_by_rec_id in(?1) and interview_status in(0,1) " +
            " ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select updatedDate from ("
            + "SELECT job_match_id as jobMatchId,Null as updatedDate FROM job_match where saved_recruiter =1 and updated_by_rec_id in(?1) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and updated_by_rec_id in(?1)  and interview_status in(0,1) "
            + " ) tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC"

            , nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJob(List<Long> userIds, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId ,applicantStatus, savedRecruiter, updatedDate  from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId , applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate FROM job_match where  saved_recruiter =1 and job_post_id in(?1) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,saved_recruiter as savedRecruiter, recruiter_updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 ", countQuery = " select count(*) from  (select updatedDate from ("
            + "SELECT job_match_id as jobMatchId,Null as updatedDate FROM job_match where saved_recruiter =1 and job_post_id in(?1) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,recruiter_updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1)  and interview_status in(0,1) "
            + " ) tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 "

            , nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIds(List<Long> jobIds, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId ,applicantStatus, savedRecruiter, updatedDate  from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId , applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate FROM job_match where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?2)) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,saved_recruiter as savedRecruiter, updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?2)) and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select updatedDate from ("
            + "SELECT job_match_id as jobMatchId,Null as updatedDate FROM job_match where saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?2)) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,recruiter_updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?2)) and interview_status in(0,1) "
            + " ) tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC"

            , nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIds(List<Long> jobIds, List<Long> userIds, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId ,applicantStatus, savedRecruiter, updatedDate  from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId , applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate FROM job_match where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?2) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,saved_recruiter as savedRecruiter, updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?2) and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select updatedDate from ("
            + "SELECT job_match_id as jobMatchId,Null as updatedDate FROM job_match where saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?2) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,updated_date as updatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?2)) and interview_status in(0,1) "
            + " ) tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC"

            , nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIdsandNotByUser(List<Long> jobIds, List<Long> userIds, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,applicantStatus, savedRecruiter, updatedDate ,recruiterUpdatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate, recruiter_updated_date as recruiterUpdatedDate FROM job_match where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?4)) and interview_status in(0,1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  " + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, updated_date as updatedDate, recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?4)) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select recruiterUpdatedDate  from ("
            + "SELECT job_match_id as jobMatchId,Null as recruiterUpdatedDate FROM job_match where  applicant_status=1 and saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?4)) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and (job_post_id in(?1) or updated_by_rec_id in(?4)) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1)  "
            + ") tbl ) tbl WHERE DATEDIFF(CURDATE(), updatedDate) <= 7 order by updatedDate DESC", nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIds(List<Long> jobIds, int applicantStatus, int applicantJobStatus, List<Long> userIds,
                                                                     Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,applicantStatus, savedRecruiter, updatedDate ,recruiterUpdatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate, recruiter_updated_date as recruiterUpdatedDate FROM job_match where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?4) and interview_status in(0,1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  " + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, updated_date as updatedDate, recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?4) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), updated_date) <= 7 order by updatedDate DESC", countQuery = " select count(*) from  (select recruiterUpdatedDate  from ("
            + "SELECT job_match_id as jobMatchId,Null as recruiterUpdatedDate FROM job_match where  applicant_status=1 and saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?4) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) and updated_by_rec_id not in(?4) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1)  "
            + ") tbl ) tbl WHERE DATEDIFF(CURDATE(), updated_date) <= 7 order by updatedDate DESC", nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIdsandNotByUser(List<Long> jobIds, int applicantStatus, int applicantJobStatus, List<Long> userIds,
                                                                                 Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,applicantStatus, savedRecruiter, updatedDate ,recruiterUpdatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, Null as updatedDate, recruiter_updated_date as recruiterUpdatedDate FROM job_match where  saved_recruiter =1 and job_post_id in(?1) and interview_status in(0,1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  " + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, null as isConsultancy, applicant_id as consultancyId, updated_by_rec_id as recruiterId, applicant_job_status as applicantStatus,Null as savedRecruiter, recruiter_updated_date as updatedDate, recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1) "
            + ") tbl WHERE DATEDIFF(CURDATE(), recruiterUpdatedDate) <= 7 ", countQuery = " select count(*) from  (select recruiterUpdatedDate  from ("
            + "SELECT job_match_id as jobMatchId,Null as recruiterUpdatedDate FROM job_match where  applicant_status=1 and saved_recruiter =1 and job_post_id in(?1) "
            + " and  applicant_status = ?2 and applicant_job_status =?3  and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId,recruiter_updated_date as recruiterUpdatedDate  FROM job_match_consultancy where  saved_recruiter =1 and job_post_id in(?1) "
            + " and applicant_status = ?2 and applicant_job_status =?3 and interview_status in(0,1)  "
            + ") tbl ) tbl WHERE DATEDIFF(CURDATE(), recruiterUpdatedDate) <= 7 ", nativeQuery = true)
    Page<JobMatchStateResultSet> getSavedProfilesforJobBasedonjobIds(List<Long> jobIds, int applicantStatus, int applicantJobStatus,
                                                                     Pageable pageable);

    Page<JobMatchConsultancy> getProfilesByJobPostIdAndConsultancyIdAndApplicantJobStatus(long jobId,
                                                                                          long consultancyId, int value, Pageable pageable);

    Long countByConsultancyIdAndApplicantStatus(Long consultancyId, int status);

    Long countByConsultancyIdAndApplicantJobStatus(Long consultancyId, int jobstatus);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.updatedByConsId =?2  and j.updatedByConsUserId in(?3) and j.interviewStatus in(0,1)")
    Page<JobMatchConsultancy> getAppliedJobsforConsultantUser(long jobId, long updatedConsId,
                                                              List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.updatedByConsId =?2 and j.applicantJobStatus = ?3 and j.applicantStatus = ?4 and j.updatedByConsUserId in(?5) and j.interviewStatus in(0,1)")
    Page<JobMatchConsultancy> getAppliedJobsforConsultantUser(long jobId, long updatedConsId, int applJobStatus, int applStatus,
                                                              List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId =?1 and j.applicantJobStatus = 1 and j.applicantStatus=0")
    List<JobMatchConsultancy> getProfilesByStatus(long jobId);

    @Query(value = "select count(j) from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyUserId in(?2) and j.applicantJobStatus =0 and j.applicantStatus=1 and j.interviewStatus in(0,1)")
    Long getIntrestedProfilesCountForRecruiter(long jobId, List<Long> updatedByConsultantId);

    @Query(value = "select count(j) from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyUserId in(?2) and j.applicantJobStatus =0 and j.matchScore>=95 and j.interviewStatus in(0,1)")
    Long getRecommendedProfilesCountForRecruiter(long jobId, List<Long> updatedByConsultantId);

//	@Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantJobStatus = ?3 and j.hiredStatus=?4")
//	Page<JobMatchConsultancy> getProfilesByHiredStatus(long jobId, long consultancyId, int applJobStatus,
//			int hireStatus, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.applicantJobStatus = ?3 and j.rejectStatus=?4")
    Page<JobMatchConsultancy> getProfilesByRejectStatus(long jobId, long consultancyId, int applJobStatus,
                                                        int rejectStatus, Pageable pageable);
    // select count(applicant_status) as applicant_status from job_match_consultancy
    // where recruiter_id =53 and applicant_status=1

//	@Query(value = "select count(jobMatchConId) from JobMatchConsultancy j where j.recruiterId =?1 and j.applicantStatus = ?2 ")
//	Long countByRecruiterIdAndApplicantStatus(Long consultancyId, int status);
//	
//	@Query(value = "select count(jobMatchConId) from JobMatchConsultancy j where j.recruiterId =?1 and j.interviewStatus = ?2 ")
//	Long countByRecruiterIdAndApplicantInterviewStatus(Long consultancyId, int status);
//	
//	@Query(value = "select count(jobMatchConId) from JobMatchConsultancy j where j.recruiterId =?1 and j.hired_status = ?2 ")
//	Long countByRecruiterIdAndApplicantHiredStatus(Long consultancyId, int status);

    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.updatedByConsId =?2  and j.updatedByConsUserId in (?4) and j.interviewStatus =?3 order by j.updatedDate DESC")
    Page<JobMatchConsultancy> getHiredJobsforConsultantUser(long jobId, long updatedConsId, List<Integer> hiredStatus,
                                                            List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select distinct j from JobMatchConsultancy j where j.jobPostId = ?1 and j.consultancyId =?2 and j.interviewStatus in (?3) and j.updatedByConsUserId in (?4)  ")
    Page<JobMatchConsultancy> getRejectedJobsforConsultantUserByInterviewStatus(long jobId, long updatedConsId, List<Integer> interviewStatus,
                                                                                List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select distinct j from JobMatchConsultancy j inner join OfferDetails o on (o.applicantId=j.applicantId and o.jobPostId=j.jobPostId) where j.jobPostId = ?1 and j.consultancyId =?2 and o.offerStatus in (?3) and j.updatedByConsUserId in (?4)  ")
    Page<JobMatchConsultancy> getRejectedJobsforConsultantUserByOfferStatus(long jobId, long updatedConsId, List<Integer> offerStatus,
                                                                            List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select distinct j from JobMatchConsultancy j left join OfferDetails o on (o.applicantId=j.applicantId and o.jobPostId=j.jobPostId) where j.jobPostId = ?1 and j.consultancyId =?2 and (o.offerStatus in (?3) or  j.interviewStatus in (?4)) and j.updatedByConsUserId in (?5) ")
    Page<JobMatchConsultancy> getRejectedJobsforConsultantUserByByInterviewStatusAndOfferStatus(long jobId, long updatedConsId, List<Integer> offerStatus, List<Integer> interviewStatus,
                                                                                                List<Long> updatedConsUserIds, Pageable pageable);


    @Query(value = "select j from JobMatchConsultancy j where j.jobPostId = ?1 and j.updatedByConsId =?2 and j.interviewStatus in (?3) and j.updatedByConsUserId in(?4)")
    Page<JobMatchConsultancy> getProfilesByInterviewStatus(long jobId, long updatedByConsId, List<Integer> interviewStatus,
                                                           List<Long> updatedConsUserIds, Pageable pageable);


    @Query(value = "select j from JobMatchConsultancy j, Interview i where  j.interviewStatus in (?1) and j.updatedByConsUserId in(?2)  and j.applicantId = i.applicantId and " +
            "j.jobPostId=i.jobPostId and TIMESTAMP(i.interviewDate, i.interviewEndTime) >= NOW() order by i.interviewDate ASC, i.interviewEndTime ASC")
    Page<JobMatchConsultancy> getProfilesByInterviewStatus(List<Integer> interviewStatus, List<Long> updatedConsUserIds, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j, Interview i where  j.interviewStatus in (?1) and j.recruiterId in(?2)  and j.applicantId = i.applicantId and " +
            "j.jobPostId=i.jobPostId and TIMESTAMP(i.interviewDate, i.interviewEndTime) >= NOW()")
    Page<JobMatchConsultancy> getProfilesByInterviewForEmployer(List<Integer> interviewStatus, List<Long> updatedConsUserIds, Pageable pageable);

    Optional<JobMatchConsultancy> findByApplicantIdAndConsultancyIdAndConsultancyUserIdAndJobPostId(Long applicantId, Long consultancyId, Long consultancyUserId, Long jobPostId);

    @Query(value = "select j FROM JobMatchConsultancy j where j.applicantId=?1 and j.jobPostId =?2")
    Optional<JobMatchConsultancy> findByApplicantIdAndConsultancyIdAndConsultancyUserIdAndJobPostId(Long applicantId, Long jobPostId);

    @Query(value = " SELECT\n" +
            "\t  COUNT(Distinct jp.applicant_id) AS relevant,\n" +
            "\t  COUNT(CASE WHEN jp.applicant_job_status = 1 and jp.interview_status=0 THEN 1 END) AS interested,\n" +
            "      COUNT(CASE WHEN jp.applicant_status = 1 and jp.interview_status=0 THEN 1 END) AS shortlisted,\n" +
            "      COUNT(CASE WHEN jp.interview_status in (2,3) THEN 1 END) AS underInterview,\n" +
            "      COUNT(CASE WHEN jp.interview_status = 7 THEN 1 END) AS underHire,\n" +
            "      COUNT(CASE WHEN jp.interview_status in (5,8,10) THEN 1 END) AS underReject\n" +
            "    FROM\n" +
            "      job_match_consultancy jp\n" +
            "    WHERE\n" +
            "      jp.job_post_id= ?1 limit 1"

            , nativeQuery = true)
    List<List<Long>> getJobPostCounts(Long jobPostId);


    @Query(value = "select count(j.applicantId) from JobMatchConsultancy j where j.consultancyId=?1")
    Long findCountofConsultantcies(long consultantId);

    @Query(value = "select count(j) from JobMatchConsultancy j where j.consultancyId =?1 and j.applicantJobStatus = ?2 and j.applicantStatus=0 and j.interviewStatus in(0,1) and j.applicantId IN (?3)")
    Long getProfilesByInterviewStatusForCons(long consultancyId, int status, List<Long> applicantIds);

    @Query(" select j FROM JobMatchConsultancy j where j.jobPostId =?1 and j.recommended=?2 and j.recommendedScore is not null and j.recommendedScore>0")
    Page<JobMatchConsultancy> findRecommendedProfiles(long jobPostId, int recommended, Pageable pageable);

    @Query(value = "select applicant_id as id,match_score as matchScore,applicant_status as applicantStatus from job_match_consultancy where job_post_id = ?1 and applicant_id in (?2)  order by applicantStatus desc,matchScore desc", nativeQuery = true)
    List<ApplicantRecommendedResultSet> getApplicantRecommended(long jobPostId, List<Long> applicantIds);

    @Query(value = "select applicant_id as id,job_post_id as jobPostId,match_score as matchScore,applicant_status as applicantStatus from job_match_consultancy where applicant_id in (?1) order by  applicantStatus desc,matchScore desc", nativeQuery = true)
    List<ApplicantRecommendedResultSet> getApplicantsMatchScore(List<Long> applicantIds);

    @Query(value = "select applicant_id as id,job_post_id as jobPostId,match_score as matchScore,applicant_status as applicantStatus from job_match_consultancy where applicant_id=?1 order by  applicantStatus desc,matchScore desc", nativeQuery = true)
    List<ApplicantRecommendedResultSet> getApplicantMatchScore(Long applicantId);

    @Query(value = "select count(*) from job_match_consultancy j where j.job_post_id=?1 and j.applicant_id in (?2) and j.applicant_status=0 and j.interview_status in (0,1) and j.applicant_job_status=1", nativeQuery = true)
    Long interestedCountForJob(long jobPostId, List<Long> applicantIds);

    @Query(value = "select count(*) from job_match_consultancy j where j.job_post_id=?1 and j.applicant_id in (?2) and j.applicant_status=1 and j.interview_status in (0,1) and j.applicant_job_status=0", nativeQuery = true)
    Long interestedCountForJobStatus(long jobPostId, List<Long> applicantIds);

    @Query(value = "select count(*) from job_match_consultancy j where j.consultancy_id=?1 and j.job_post_id=?3 and j.applicant_id in (?2) and j.applicant_status=0 and j.interview_status in (0,1) and j.applicant_job_status=1", nativeQuery = true)
    Long interestedCountForConsultancy(long consultancyId, List<Long> applicantIds, long jobId);

    @Query(value = "select count(*) from job_match_consultancy j where j.consultancy_id=?1 and j.job_post_id=?3 and j.applicant_id in (?2) and j.applicant_status=1 and j.interview_status in (0,1) and j.applicant_job_status=0", nativeQuery = true)
    Long interestedCountForConsultancyStatus(long consultancyId, List<Long> applicantIds, long jobId);


    @Query(value = "SELECT lastaction_performed_recruiter_id FROM job_match_consultancy where job_match_con_id =?1", nativeQuery = true)
    Long getLastActionPerfomedRecruiterId(Long jobMatchConId);

    @Query(value = "SELECT lastaction_performed_consultant_user_id FROM job_match_consultancy where job_match_con_id =?1", nativeQuery = true)
    Long getLastActionPerfomedConsultantUserId(Long jobMatchConId);

    @Query(value = "select count(*) from job_match_consultancy j where j.job_post_id=?1 and j.applicant_id in (?2) and j.applicant_job_status=0 and j.applicant_status in (0,1)", nativeQuery = true)
    Long getApplicantCountForJob(long jobPostId, List<Long> applicantIds);

    @Query(value = " select * from job_match_consultancy jm where jm.saved_recruiter=1 and jm.applicant_status=0 and jm.updated_date <= CURRENT_DATE - 7 ", nativeQuery = true)
    List<JobMatchConsultancy> getAllExpiredSavedJobs();

    @Modifying
    @Transactional
    @Query(value = "update job_match_consultancy jm set jm.saved_recruiter=0  where jm.job_match_con_id in (SELECT distinct jms.job_match_con_id FROM job_match_consultancy jms where   jms.job_match_con_id = jm.job_match_con_id and jms.saved_recruiter=1 and jms.updated_date <= CURRENT_DATE - 7)", nativeQuery = true)
    void updateSavedJobRecrutier();
}


