/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.ConsDashBoardStatsResultSet;
import com.workruit.us.application.dto.JobMatchConsultancyResultSet;
import com.workruit.us.application.dto.JobMatchStateResultSet;
import com.workruit.us.application.models.JobMatch;
import com.workruit.us.application.models.JobMatchConsultancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * @author Mahesh
 */
public interface JobMatchingRepository extends JpaRepository<JobMatch, Long> {

    @Query(value = "select j from JobMatch j where j.jobPostId=?1 and j.applicantStatus=0")
    Page<JobMatch> findJobMatchesByJobPostId(long jobPostId, Pageable pageable) throws Exception;

    @Query(value = "select j from JobMatch j where j.jobPostId=?1 and j.applicantId=?2")
    JobMatch findByJobPostIdAndApplicantId(long jobPostId, long applicantId);

    // Returns matched jobs for consultancy( applicantId is consultany id in
    // JobMatch table when the profile is from consultancy)
    @Query(value = "select j from JobMatch j where j.isConsultancy=true and j.applicantId=?1 and (j.applicantStatus=0)")
    Page<JobMatch> findJobMatchesByConsultancyId(long consultancyId, Pageable pageable);

    // Returns matched jobs for user
    @Query(value = "select j from JobMatch j where j.applicantId=?1 and (j.applicantJobStatus=0)")
    Page<JobMatch> findJobMatchesByNormalUserId(long applciantId, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, updated_date as updatedDate FROM job_match where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, updated_date as updatedDate  FROM job_match_consultancy where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + " ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + "SELECT job_match_id as jobMatchId FROM job_match where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + "union all "
            + "SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + ") tbl",

            nativeQuery = true)
    Page<JobMatchStateResultSet> getHiredProfilesByInterviewStatusforUsers(Long jobPostId, int interviewStatus,
                                                                           List<Long> userids, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, updated_date as updatedDate FROM job_match where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, updated_date as updatedDate  FROM job_match_consultancy where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + " ) tbl", countQuery = "select count(jobMatchId)  from ("
            + "SELECT job_match_id as jobMatchId FROM job_match where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + "union all "
            + "SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where job_post_id = ?1 and interview_status=?2 and updated_by_rec_id in(?3)"
            + ") tbl",

            nativeQuery = true)
    Page<JobMatchStateResultSet> getRejectedProfilesByInterviewStatusforUsers(Long jobPostId, int interviewStatus,
                                                                              List<Long> userids, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,interviewStatus, hiredStatus,updatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, interview_status as interviewStatus,hired_status as hiredStatus,updated_date as updatedDate FROM job_match where job_post_id = ?1 and applicant_status=1 and interview_status in(?2) and updated_by_rec_id in(?3) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId , interview_status as interviewStatus, hired_status as hiredStatus,updated_date as updatedDate FROM job_match_consultancy where job_post_id = ?1  and applicant_status=1 and interview_status in(?2) and updated_by_rec_id in(?3) "
            + " ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + " SELECT job_match_id as jobMatchId FROM job_match where job_post_id = ?1 and applicant_status=1 and interview_status in(?2) and updated_by_rec_id in(?3) "
            + "	union all "
            + "	SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where job_post_id = ?1  and applicant_status=1 and interview_status in(?2) and updated_by_rec_id in(?3) "
            + "	) tbl ", nativeQuery = true)
    Page<JobMatchStateResultSet> getInterviewsListforJobByTeam(Long jobPostId, List<Long> applicantStatus,
                                                               List<Long> userIds, Pageable pageable);

    //j.interviewStatus in (2,3)
    //j.recruiterId in(?1) or j.interviewScheduledUserId in (?1) or
    @Query(value = "select j.jobMatchConId as jobMatchId, j.jobPostId as jobPostId, j.applicantId as applicantId,j.consultancyId as consultancyId,j.recruiterId  as recruiterId from JobMatchConsultancy j, Interview i where" +
            " (j.interviewScheduledUserId in (?1) or j.interviewRescheduledUserId in (?1)) and j.applicantId = i.applicantId and " +
            " j.jobPostId=i.jobPostId and TIMESTAMP(i.interviewDate, i.interviewEndTime) >= NOW() and j.jobPostId in(?2) order by i.interviewDate ASC, i.interviewEndTime ASC")
    Page<JobMatchStateResultSet> getAllInterviewsListforJobByTeam(List<Long> userIds, List<Long> jobIds, Pageable pageable);

    @Query(value = "select j.jobMatchConId as jobMatchId, j.jobPostId as jobPostId, j.applicantId as applicantId,j.consultancyId as consultancyId,j.recruiterId  as recruiterId from JobMatchConsultancy j, Interview i where (j.interviewScheduledUserId in (?1) or j.interviewRescheduledUserId in (?1)) and j.applicantId = i.applicantId and " +
            " j.jobPostId=i.jobPostId and TIMESTAMP(i.interviewDate, i.interviewEndTime) >= NOW()  order by i.interviewDate ASC, i.interviewEndTime ASC")
    Page<JobMatchStateResultSet> getAllInterviewsListforJobByTeam(List<Long> userIds, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate, hiredStatus from ("
            + " SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, recruiter_updated_date as updatedDate, applicant_job_status as hiredStatus  FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and " +
            " applicant_status in(?3) and applicant_job_status in(?4) and interview_status in(0,1) "
            + " group by recruiter_updated_date order by Max(recruiter_updated_date) DESC, hiredStatus DESC ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + "	SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status=?3 and applicant_job_status=?4 and interview_status in(0,1) "
            + "  ) tbl ", nativeQuery = true)
    Page<JobMatchStateResultSet> getProfilesByApplicantStatus(List<Long> recruiterid, Long jobPostId,
                                                              int applicantStatus, int applicantJobStatus, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate, hiredStatus from ("
            + " SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId, recruiter_updated_date as updatedDate, applicant_job_status as hiredStatus  FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and " +
            " applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + " group by recruiter_updated_date order by Max(recruiter_updated_date) DESC, hiredStatus DESC ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + "	SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + "  ) tbl ", nativeQuery = true)
    Page<JobMatchStateResultSet> getProfilesByApplicantStatus(List<Long> recruiterid, Long jobPostId, Pageable pageable);


    @Query(value = "select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate,hiredStatus from ("
            + " SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId,recruiter_updated_date as updatedDate, applicant_job_status as hiredStatus "
            + " FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + " group by recruiter_updated_date order by Max(recruiter_updated_date) DESC, hiredStatus DESC ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + "	SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + "	group by recruiter_updated_date order by Max(recruiter_updated_date) DESC ) tbl", nativeQuery = true)
    Page<JobMatchStateResultSet> getProfilesByApplicantStatusAll(List<Long> recruiterid, Long jobPostId, Pageable pageable);

    @Query(value = "select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate,hiredStatus from ("
            + " SELECT job_match_con_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId,updated_date as updatedDate, applicant_job_status as hiredStatus "
            + " FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + " group by job_match_con_id order by updated_date DESC, hiredStatus DESC ) tbl ", countQuery = "select count(jobMatchId)  from ("
            + "	SELECT job_match_con_id as jobMatchId FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status in(1) and applicant_job_status in(0,1) and interview_status in(0,1) "
            + "	) tbl", nativeQuery = true)
    Page<JobMatchStateResultSet> getProfilesByApplicantStatusAllWithSort(List<Long> recruiterid, Long jobPostId, Pageable pageable);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId FROM job_match where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status=?3 and applicant_job_status in(0,1) and interview_status in(0,1) "
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId  FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status=?3 and applicant_job_status in (0,1) and interview_status in(0,1) "
            + ") tbl ", countQuery = "select count(jobMatchId) from ("
            + "SELECT job_match_id as jobMatchId  FROM job_match where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status=?3 and applicant_job_status in(0,1) and interview_status in(0,1) "
            + " union all "
            + "SELECT job_match_con_id as jobMatchId   FROM job_match_consultancy where updated_by_rec_id in(?1) and job_post_id = ?2 and applicant_status=?3 and applicant_job_status in (0,1) and interview_status in(0,1) "
            + ") tbl", nativeQuery = true)
    Page<JobMatchStateResultSet> getProfilesByApplicantStatus(List<Long> recruiterid, Long jobPostId,
                                                              int applicantStatus, Pageable pageable);

    // Returns no of jobs hired/rejected/interviewed count
    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,updatedDate from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId,updated_date as updatedDate FROM job_match where job_post_id = ?1 and interview_status=?2 "
            + " union all"
            + " SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId,updated_date as updatedDate  FROM job_match_consultancy where job_post_id = ?1 and interview_status=?2"
            + ") tbl ", nativeQuery = true)
    Page<JobMatchStateResultSet> getUpcomingInterviews(Long recruiterId, int applicantStatus, Pageable pageable);

    JobMatch findByJobPostIdAndApplicantIdAndIsConsultancy(long jobPostId, long applicantId, boolean b);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,interviewStatus,hiredStatus from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, interview_status as interviewStatus, applicant_job_status as hiredStatus FROM job_match where job_post_id = ?1  and applicant_status=1  "
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId , interview_status as interviewStatus,applicant_job_status as hiredStatus FROM job_match_consultancy where job_post_id = ?1 and  applicant_status=1 "
            + ") tbl ", nativeQuery = true)
    List<JobMatchStateResultSet> getStats(Long jobPostId);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,interviewStatus,hiredStatus from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, interview_status as interviewStatus, applicant_job_status as hiredStatus FROM job_match where job_post_id = ?1  and applicant_status=1  and  updated_by_rec_id in (?2)"
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId , interview_status as interviewStatus,applicant_job_status as hiredStatus FROM job_match_consultancy where job_post_id = ?1 and  applicant_status=1  and updated_by_rec_id in (?2)"
            + ") tbl ", nativeQuery = true)
    List<JobMatchStateResultSet> getStats(Long jobPostId, List<Long> userIds);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,interviewStatus from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, updated_by_rec_id as recruiterId, interview_status as interviewStatus FROM job_match where job_post_id in (?1)  and applicant_status=1  "
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, updated_by_rec_id as recruiterId , interview_status as interviewStatus FROM job_match_consultancy where job_post_id = ?1 and  applicant_status=1   "
            + ") tbl ", nativeQuery = true)
    List<JobMatchStateResultSet> getStats(List<Long> jobIds);

    @Query(value = "SELECT COUNT(CASE WHEN interview_status = 2 or interview_status = 9 or interview_status = 3 or interview_status = 4 or interview_status = 5 or interview_status = 6 THEN 1 END) AS interview,COUNT(CASE WHEN interview_status = 7 THEN 1 END) AS hired " +
            ",COUNT(CASE WHEN interview_status = 8 THEN 1 END) AS rejected,COUNT(CASE WHEN applicant_job_status = 1 and applicant_status=0 THEN 1 END) AS applied " +
            "FROM job_match_consultancy where job_post_id =?1 and updated_by_cons_user_id in (?2)  ", nativeQuery = true)
    ConsDashBoardStatsResultSet getDashboardStats(Long jobPostId, List<Long> userIds);

    @Query(value = "SELECT COUNT(CASE WHEN interview_status = 2 or interview_status = 9 or interview_status = 3 or interview_status = 4 or interview_status = 5 or interview_status = 6 THEN 1 END) AS interview,COUNT(CASE WHEN interview_status = 7 THEN 1 END) AS hired " +
            ",COUNT(CASE WHEN interview_status = 8 THEN 1 END) AS rejected,COUNT(CASE WHEN applicant_job_status = 1 and applicant_status=0 THEN 1 END) AS applied " +
            "FROM job_match_consultancy where  updated_by_cons_user_id in (?1)  ", nativeQuery = true)
    ConsDashBoardStatsResultSet getDashboardStats(List<Long> userIds);

    @Query(value = "SELECT COUNT(DISTINCT job_post_id)FROM job_match_consultancy where updated_by_cons_user_id in (?1) and applicant_job_status = 1 ", nativeQuery = true)
    Long getDashboardStatsJobApplied(List<Long> userIds);

    @Query(value = "SELECT COUNT(DISTINCT job_match_con_id) FROM job_match_consultancy where job_post_id =?1 and updated_by_rec_id in (?2) and applicant_status=1 and applicant_job_status = 1 and interview_status in(0,1)", nativeQuery = true)
    Long getDashboardStatsJobApplied(Long jobPostId, List<Long> userIds);

    @Query(value = "SELECT COUNT(DISTINCT job_match_con_id) FROM job_match_consultancy where job_post_id =?1 and  applicant_status=1 and applicant_job_status = 1 and interview_status in(0,1)", nativeQuery = true)
    Long geJobMatchedStats(Long jobPostId);

    @Query(value = "SELECT COUNT(DISTINCT job_match_con_id)FROM job_match_consultancy where job_post_id =?1 and applicant_status=1 and applicant_job_status = 1 and interview_status in(0,1)", nativeQuery = true)
    Long getDashboardStatsforJob(Long jobPostId);

    @Query(value = "select distinct(j.jobPostId) from JobMatchConsultancy j where  j.consultancyId=?1  and j.consultancyUserId in(?2) " +
            " and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.consultancyUserId in(?2) and a.status in (1,2))" +
            " and j.jobPostId in(select jp.jobPostId from JobPost jp where jp.status='ACTIVE' and j.jobPostId=jp.jobPostId) and  DATEDIFF(CURDATE(), j.updatedDate) <= 7 order by j.updatedDate DESC")
    Page<Long> findJobIdsByConsultancyIdAndConsultancyUserId(long consultancyId, List<Long> consultancyUserId, Pageable pageable);

//    @Query(value = "select distinct(j.jobPostId) from JobMatchConsultancy j where  j.consultancyId=?1  and j.consultancyUserId in(?2) and j.applicantStatus!=2 " +
//            " and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.consultancyUserId in(?2) and a.status in (1,2))" +
//            " and j.jobPostId in(select jp.jobPostId from JobPost jp where jp.status='ACTIVE' and j.jobPostId=jp.jobPostId) ")
//    Page<Long> findJobIdsByConsultancyIdAndConsultancyUserIdforJobs(long consultancyId, List<Long> consultancyUserId, Pageable pageable);

//    @Query(value = "select j from JobMatchConsultancy j where  j.consultancyId=?1  and j.consultancyUserId in(?2) and j.applicantStatus!=2 " +
//            " and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.consultancyUserId in(?2) and a.status in (1,2))" +
//            " and j.jobPostId in(select jp.jobPostId from JobPost jp where jp.status='ACTIVE' and j.jobPostId=jp.jobPostId) group by j.jobPostId order by j.applicantStatus DESC, Max(j.updatedDate) DESC")
//    Page<JobMatchConsultancy> findJobIdsByConsultancyIdAndConsultancyUserIdforJobs(long consultancyId, List<Long> consultancyUserId, Pageable pageable);

    @Query(value = "select j from JobMatchConsultancy j where  j.consultancyId=?1  and j.consultancyUserId in(?2) and j.applicantStatus!=2 and j.applicantJobStatus =0 " +
            " and j.jobPostId not in (select a.jobPostId from ConsultancyJobStatus a where a.consultancyUserId in(?3) and a.status in (1,2))" +
            " and j.jobPostId in(select jp.jobPostId from JobPost jp where jp.status='ACTIVE' and j.jobPostId=jp.jobPostId) group by j.jobPostId order by MAX(j.applicantStatus) DESC,j.jobMatchConId DESC")
    Page<JobMatchConsultancy> findJobIdsByConsultancyIdAndConsultancyUserIdforJobs(long consultancyId, List<Long> consultancyUserId, Long userId, Pageable pageable);

    //order by Max(j.applicantJobStatus) DESC, count(j.applicantId) DESC
    @Query(value = "select j from JobMatchConsultancy j where  j.jobPostId in(?1) group by j.jobPostId order by j.applicantStatus DESC, Max(j.updatedDate) DESC")
    Page<JobMatchConsultancy> findJobMatchesByConsultancyIdAndConsultancyUserId(List<Long> jobIds, Pageable pageable);

//    @Query(value = "select j from JobMatchConsultancy j where  j.consultancyId=?1 and j.applicantStatus in(?3)  and j.consultancyUserId in (?2) " +
//            " and j.jobPostId in (select a.jobPostId from ConsultancyJobStatus a where a.consultancyUserId in(?2) and a.status=1 ) group by j.jobPostId,j.jobMatchConId order by j.updatedDate desc")
//    Page<JobMatchConsultancy> findSavedJobMatchesByConsultancyIdAndConsultancyUserId(long consultancyId, List<Long> consultancyUserId, List<Integer> status, Pageable pageable);

    @Query(value = "select j as jobMatchConsultancy,cjs.updatedDate as updatedDate from JobMatchConsultancy j inner join ConsultancyJobStatus cjs on cjs.jobPostId =  j.jobPostId " +
            "where  j.consultancyId=?1 and j.applicantStatus in(?3)  and j.consultancyUserId in (?2) and j.jobPostId  not in(select jc.jobPostId from JobMatchConsultancy jc where jc.consultancyUserId in(?2) and jc.applicantStatus =1  and cjs.jobPostId=jc.jobPostId )" +
            " and cjs.consultancyUserId in(?2) and cjs.status=1 group by j.jobPostId,j.consultancyUserId order by cjs.updatedDate desc")
    Page<JobMatchConsultancyResultSet> findSavedJobMatchesByConsultancyIdAndConsultancyUserId(long consultancyId, List<Long> consultancyUserId, List<Integer> status, Pageable pageable);

    @Query(value = "select j as jobMatchConsultancy,cjs.updatedDate as updatedDate from JobMatchConsultancy j inner join ConsultancyJobStatus cjs on cjs.jobPostId =  j.jobPostId " +
            "where  j.consultancyId=?1 and j.applicantStatus in(?3)  and j.consultancyUserId in (?2) " +
            " and cjs.consultancyUserId in(?2) and cjs.status=1 group by j.jobPostId,j.consultancyUserId order by cjs.updatedDate desc")
    Page<JobMatchConsultancyResultSet> findSavedJobMatchesByConsultancyIdAndConsultancyUserIdV1(long consultancyId, List<Long> consultancyUserId, List<Integer> status, Pageable pageable);

//    @Query(value = "select distinct(j) as jobMatchConsultancy,j.updatedDate as updatedDate from JobMatchConsultancy j,ConsultancyJobStatus cjs where j.consultancyId=?1 and j.applicantStatus in(?3)  and j.consultancyUserId in (?2) and cjs.status=1" +
//            " and cjs.jobPostId  not in(select jc.jobPostId from JobMatchConsultancy jc where jc.consultancyUserId in(?2) and jc.applicantStatus =1  and cjs.jobPostId=jc.jobPostId )" +
//            " and cjs.jobPostId in(select jp.jobPostId from JobPost jp where jp.status='ACTIVE' and cjs.jobPostId=jp.jobPostId) and j.jobPostId=cjs.jobPostId group by cjs.jobPostId,j.updatedDate order by j.updatedDate desc")
//    Page<JobMatchConsultancyResultSet> findSavedJobMatchesByConsultancyIdAndConsultancyUserId(long consultancyId, List<Long> consultancyUserId, List<Integer> status, Pageable pageable);
//
}
