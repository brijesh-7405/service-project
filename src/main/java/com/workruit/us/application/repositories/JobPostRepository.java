/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.JobMatchStateResultSet;
import com.workruit.us.application.dto.JobStateResultSet;
import com.workruit.us.application.dto.JobStatusResultSet;
import com.workruit.us.application.dto.UserInfoResultSet;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.models.JobPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Mahesh
 */
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    // Returns noof jobs hired/rejected/interviewed count
    @Query(value = "select j from JobPost j where j.status = ?3 and (j.userId=?1 or find_in_set(?2, collaboratorId)> 0)")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(long userId, String collaboratorId, JobStatus status,
                                                           Pageable pageable) throws Exception;

    @Query(value = "select j from JobPost j where j.status in('ACTIVE')  and (j.userId=?1 or find_in_set(?2, collaboratorId)> 0)")
    List<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(long userId, String collaboratorId);

    @Query(value = "select j from JobPost j where j.status = ?2 and j.userId=?1 ")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(long userId, JobStatus status,
                                                           Pageable pageable) throws Exception;


    @Query(value = "select j from JobPost j where j.status = ?3 and (j.userId in (?1) or find_in_set(?2, j.collaboratorId) > 0 ) order by j.createdDate DESC")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(List<Long> userId, String collaboratorId,
                                                           JobStatus status, Pageable pageable) throws Exception;

    @Query(value = "select j from JobPost j where j.status = ?2 and  find_in_set(?1, collaboratorId) > 0 ")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(String collaboratorId,
                                                           JobStatus status, Pageable pageable) throws Exception;

    @Query(value = "SELECT status as jobState, count(*) as jobStateCount  FROM job_post where user_id in (?1) or find_in_set(?2, collaborator_id) > 0 group by status", nativeQuery = true)
    List<JobStateResultSet> getJobStatesForUser(List<Long> recruiterId, String collaboratorId);


    @Query(value = "select collaboratorId from JobPost j where j.status = ?2 and j.userId=?1")
    String findJobCollaborators(long userId, JobStatus status);

    @Query(value = "select j from JobPost j INNER JOIN JobMatchConsultancy jm on j.jobPostId = jm.jobPostId where j.status in('ACTIVE','CLOSED') and (j.userId=?1 or find_in_set(?2, j.collaboratorId) > 0) group by j.jobPostId order by Max(jm.updatedDate) DESC")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(long userId, String collaboratorId, Pageable pageable)
            throws Exception;


    @Query(value = "select j from JobPost j INNER JOIN" +
            " JobMatchConsultancy jm on j.jobPostId = jm.jobPostId where j.status in('ACTIVE','CLOSED') and j.userId in(?1) group by j.jobPostId order by Max(jm.updatedDate) DESC")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(List<Long> userIds, Pageable pageable)
            throws Exception;

    @Query(value = "select j from JobPost j INNER JOIN JobMatchConsultancy jm on j.jobPostId = jm.jobPostId where j.status in('ACTIVE','CLOSED') and find_in_set(?1, j.collaboratorId) > 0 group by j.jobPostId order by MAX(jm.updatedDate) DESC")
    Page<JobPost> findJobByCollaboratorIdAndStatus(String collaboratorId, Pageable pageable)
            throws Exception;

    @Query(value = "select j from JobPost j where j.status in('ACTIVE','CLOSED') and j.userId =?1")
    Page<JobPost> findJobByUserIdAndStatus(Long userId, Pageable pageable)
            throws Exception;


    @Query(value = "select j from JobPost j where j.status =?2 and find_in_set(?1, j.collaboratorId) > 0")
    Page<JobPost> findJobByCollaboratorIdAndStatus(String collaboratorId, JobStatus status, Pageable pageable)
            throws Exception;

    @Query(value = "select j from JobPost j where j.status in('ACTIVE') and find_in_set(?1, j.collaboratorId) > 0")
    Page<JobPost> findJobByCollaboratorIdAndStatusforSavedJobs(String collaboratorId, Pageable pageable)
            throws Exception;

    @Query(value = "select j from JobPost j where j.status = ?2 and j.userId in (?1) order by j.createdDate DESC")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(List<Long> userId, JobStatus status, Pageable pageable)
            throws Exception;

    @Query(value = "select j from JobPost j where j.status = ?2 and (j.userId in (?1) or find_in_set(?3, j.collaboratorId) > 0 ) order by j.createdDate DESC")
    Page<JobPost> findJobByUserIdOrCollaboratorIdAndStatus(List<Long> userId, JobStatus status, String collaboratorId, Pageable pageable)
            throws Exception;

//	@Query(value = "select j from JobPost j where j.status = ?3 and j.userId=?1 ")
//	Page<JobPost> findJobsByUser(long userId, JobStatus status, Pageable pageable) throws Exception;

    @Query(value = " SELECT job_post_id as jobPostId,interview_status as interviewStatus,sum(stat) as totalCount  from ("
            + "SELECT job_post_id,interview_status,count(*) as stat FROM job_match where job_post_id in (?1) and applicant_status =1 and applicant_job_status !=2 group by job_post_id,interview_status "
            + "union all "
            + "SELECT job_post_id,interview_status,count(*) as stat FROM job_match_consultancy where  job_post_id in (?1) and applicant_status =1 and applicant_job_status !=2 group by job_post_id,applicant_status, interview_status "
            + " ) tbl group by job_post_id,interview_status ", nativeQuery = true)
    List<JobStatusResultSet> getJobStatistics(List<Long> jobPostIdList);

    @Query(value = " SELECT job_post_id as jobPostId,interview_status as interviewStatus,sum(stat) as totalCount  from ("
            + "SELECT job_post_id,interview_status,count(*) as stat FROM job_match where job_post_id=?1 and applicant_status =1 and applicant_job_status !=2 group by job_post_id,interview_status "
            + "union all "
            + "SELECT job_post_id,interview_status,count(*) as stat FROM job_match_consultancy where  job_post_id=?1 and applicant_status =1 and applicant_job_status !=2 group by job_post_id,applicant_status, interview_status "
            + " ) tbl group by job_post_id,interview_status ", nativeQuery = true)
    List<JobStatusResultSet> getJobStatistics(Long jobPostIdList);

    // Return No of jobs posted and in which state by user - Active/Pending/Closed
    @Query(value = "SELECT status as jobState, count(*) as jobStateCount FROM job_post where user_id = ?1 group by status", nativeQuery = true)
    List<JobStateResultSet> getJobStatesForUser(Long recruiterId);

    @Query(value = "SELECT status as jobState, count(*) as jobStateCount FROM job_post where job_post_id in (?1) group by status", nativeQuery = true)
    List<JobStateResultSet> getJobStatesForUserByJobid(List<Long> jobId);

    @Query(value = " select jobMatchId, jobPostId, applicantId,isConsultancy,consultancyId,recruiterId,interviewStatus,hiredStatus from ("
            + "SELECT job_match_id as jobMatchId, job_post_id as jobPostId, applicant_id as applicantId, is_consultancy as isConsultancy, Null as consultancyId, recruiter_id as recruiterId, interview_status as interviewStatus, applicant_job_status as hiredStatus FROM job_match where job_post_id = ?1  and applicant_status=1  "
            + "union all "
            + "SELECT job_match_con_id as jobMatchConId, job_post_id as jobPostId, applicant_id as applicantId, Null as isConsultancy, consultancy_id as consultancyId, recruiter_id as recruiterId , interview_status as interviewStatus,applicant_job_status as hiredStatus FROM job_match_consultancy where job_post_id = ?1 and  applicant_status=1   "
            + ") tbl ", nativeQuery = true)
    List<JobMatchStateResultSet> getStats(Long jobPostId);

//    @Query(value = "select a.applicant_id as applicantId, a.consultancy_id as consultancyId,a.consultancy_user_id as consultancyUserid from applicant a  where a.applicant_id in (select askill.applicant_id from applicant_job_skill askill where job_skill_id in (?1)) and a.applicant_id in  (select aj.applicant_id from applicant_job_function aj where job_function_id in (?2)) and a.applicant_id in (select ad.applicant_id from applicant_details ad where years_of_exp >=?3 and years_of_exp <=?4)", nativeQuery = true)
//    List<UserInfoResultSet> findUserProfilesByCriteria(List<Integer> jobSkills, List<Integer> jobFuncList, long expMin,
//                                                       long expMax, String location, int jobType, Pageable pageable);

//    @Query(value = "select a.applicant_id as applicantId, a.consultancy_id as consultancyId,a.consultancy_user_id as consultancyUserid from applicant a \n" +
//            "where a.applicant_id in (select askill.applicant_id from applicant_job_skill askill where job_skill_id in (?1))\n" +
//            "and a.applicant_id in  (select aj.applicant_id from applicant_job_function aj where job_function_id in (?2)) \n" +
//            "and a.applicant_id in (select ad.applicant_id from applicant_details ad where years_of_exp >=?3 and years_of_exp <=?4 and job_type=?6\n" +
//            "and preferred_work_mode=?7 and citizenship=?8 and notice_period=?9)\n" +
//            "and a.applicant_id in (select ed.applicant_id from education_history ed inner join degrees d on (d.SHORT_TITLE=ed.degree or d.title=ed.degree) where d.degree_id IN (?10))\n" +
//            "and a.location=?5", nativeQuery = true)
//    List<UserInfoResultSet> findUserProfilesByCriteria(List<Integer> jobSkills, List<Integer> jobFuncList, long expMin,
//                                                       long expMax, String location, String jobType, String workMode, String citizenship, String noticePeriod, List<Integer> degreeIds, Pageable pageable);


    @Query(value = "select a.applicant_id as applicantId, a.consultancy_id as consultancyId,a.consultancy_user_id as consultancyUserid from applicant a \n" +
            "where a.applicant_id in (select askill.applicant_id from applicant_job_skill askill where job_skill_id in (?1))\n" +
            "and (a.applicant_id in  (select aj.applicant_id from applicant_job_function aj where job_function_id in (?2) or job_function_id in (?3)) \n" +
            "or a.applicant_id in  (select asj.applicant_id from applicant_secondary_job_function asj where job_function_id in (?2) or job_function_id in (?3)))\n" +
            "and a.applicant_id in (select ad.applicant_id from applicant_details ad where years_of_exp >=?4 and years_of_exp <=?5) and a.correction_required=0 and a.consultancy_id!=?6", nativeQuery = true)
    List<UserInfoResultSet> findUserProfilesByCriteria(List<Integer> jobSkills, List<Integer> jobFuncList, List<Integer> optJobFuncList, long expMin,
                                                       long expMax, long consultantId, Pageable pageable);


    @Query(value = "select j.job_post_id as jobPostId, j.user_id as recruiterId from job_post j where job_post_id in (select job_post_id from job_post_skills where skill_id in (?1)) or job_post_id in (select job_post_id from jobpost_jobfunctions where job_function_id in (?2)) or j.experience_min>=?3 or j.experience_max<=?4 or j.location=?5", nativeQuery = true)
    List<UserInfoResultSet> findJobsForAppilcantByCriteria(List<Integer> userSkills, List<Integer> jobFuncList,
                                                           long expMin, long expMax, String location, int jobType, Pageable pageable);

    @Query(value = "select a.applicant_id as applicantId, a.consultancy_id as consultancyId from applicant a  where a.applicant_id in (select askill.applicant_id from applicant_job_skill askill where job_skill_id in (?1)) and a.applicant_id in  (select aj.applicant_id from applicant_job_function aj where job_function_id in (?2)) and a.applicant_id in (select ad.applicant_id from applicant_details ad where years_of_exp >=?3 and years_of_exp <=?4)", nativeQuery = true)
    Page<UserInfoResultSet> findUserProfilesBySearchFilter(List<Integer> jobSkills, List<Integer> jobFuncList,
                                                           long expMin, long expMax, String location, int jobType, Pageable pageable);

    Page<JobPost> findJobPostByStatus(JobStatus active, Pageable pageable);

    @Query(value = "SELECT job_post_id as jobPostId,  applicant_job_status as applicantStatus, count(applicant_job_status) as totalCount FROM job_match_consultancy where consultancy_id in (?1) group by applicant_job_status,job_post_id order by job_post_id desc ", countQuery = "select count(job_post_id) FROM job_match_consultancy where consultancy_id in (?1) and applicant_job_status !=0", nativeQuery = true)
    Page<JobStatusResultSet> getUserStatistics(Long consultancyId, Pageable pageable);

    JobPost findJobPostByJobPostIdAndStatus(Long jobPostId, JobStatus active);

    @Query(value = "SELECT  job_post_id FROM job_post where user_id = ?1 or find_in_set(?2, collaborator_id)> 0", nativeQuery = true)
    List<Long> findJobPostByJobPostIdAndStatus(Long userId, String collabratorId);

    @Query(value = "select count(j) from JobPost j where j.status = ?3 and (j.userId in (?1) or find_in_set(?2, j.collaboratorId) > 0) ")
    Long findJobPostByJobPostIdAndStatus(List<Long> userId, String collabratorId, JobStatus status);

    @Query(value = "select count(j) from JobPost j where j.status = ?2 and j.userId in (?1) ")
    Long findJobPostByJobPostIdAndStatus(List<Long> userId, JobStatus status);

    @Query(value = "select count(j) from JobPost j where j.status = ?2 and find_in_set(?1, j.collaboratorId) > 0 ")
    Long findJobPostByJobPostIdAndStatus(String collabratorId, JobStatus status);

    @Query(value = "SELECT  job_post_id FROM job_post where user_id in (?1) ", nativeQuery = true)
    List<Long> findJobPostByJobPostIdAndStatus(List<Long> userId);

    //    @Query(value = "select j.job_post_id as jobPostId \n" +
//            "from job_post j  \n" +
//            "where j.job_post_id in (select jskill.job_post_id from `job_post_skills` jskill where skill_id in (?1)) \n" +
//            "and j.job_post_id in  (select jj.job_post_id from `jobpost_jobfunctions` jj where job_function_id in (?2)) \n" +
//            "and j.job_post_id in  (select jd.job_post_id from `job_degrees` jd where degree_id in (?9))\n" +
//            "and ?3 between  j.experience_min AND j.experience_max and j.location=?4 and j.job_type=?5 and j.workloc_type=?6 and j.citizenship_id=?7 and j.notice_period=?8 ", nativeQuery = true)
//    List<Long> findJobsByCriteria(List<Integer> userSkills, List<Integer> jobFuncList, float exp, String location, int jobType, String workMode, int citizenship, int noticePeriod, List<Integer> degreeIds);
//optional_jobpost_jobfunctions

    //Admin side changes for exp min and exp max
//    @Query(value = "select j.job_post_id as jobPostId " +
//            "from job_post j " +
//            "where j.job_post_id in (select jskill.job_post_id from `job_post_skills` jskill where skill_id in (?1)) " +
//            "and (j.job_post_id in  (select jj.job_post_id from jobpost_jobfunctions jj where job_function_id in (?2) or job_function_id in (?6)) " +
//            "or j.job_post_id in  (select jsj.job_post_id from optional_jobpost_jobfunctions jsj where job_function_id in (?2) or job_function_id in (?6)))" +
//            " and j.experience_min >=?3 AND j.experience_max <=?4 AND j.user_id !=?5 ", nativeQuery = true)
//    List<Long> findJobsByCriteria(List<Integer> userSkills, List<Integer> jobFuncList, long expMin, long expMax, long userId, List<Integer> secJobFuncList);

    @Query(value = "select j.job_post_id as jobPostId " +
            "from job_post j " +
            "where j.job_post_id in (select jskill.job_post_id from `job_post_skills` jskill where skill_id in (?1)) " +
            "and (j.job_post_id in  (select jj.job_post_id from jobpost_jobfunctions jj where job_function_id in (?2) or job_function_id in (?5)) " +
            "or j.job_post_id in  (select jsj.job_post_id from optional_jobpost_jobfunctions jsj where job_function_id in (?2) or job_function_id in (?5)))" +
            " and  ((?3 between (j.experience_min - 1) AND (j.experience_max + 1)) OR (?3 between j.experience_min AND j.experience_max )) AND j.user_id !=?4 ", nativeQuery = true)
    List<Long> findJobsByCriteria(List<Integer> userSkills, List<Integer> jobFuncList, float exp, long userId, List<Integer> secJobFuncList);

    @Query(value = "select a.applicant_id from applicant a \n" +
            "where a.applicant_id in (select askill.applicant_id from applicant_job_skill askill where job_skill_id in (?1))\n" +
            "and (a.applicant_id in  (select aj.applicant_id from applicant_job_function aj where job_function_id in (?2) or job_function_id in (?6)) \n" +
            "or a.applicant_id in  (select asj.applicant_id from applicant_secondary_job_function asj where job_function_id in (?2) or job_function_id in (?6)))\n" +
            "and a.applicant_id in (select ad.applicant_id from applicant_details ad where years_of_exp >=?3 and years_of_exp <=?4) and a.correction_required=0 and a.applicant_id in (?5) ", nativeQuery = true)
    List<Long> findUserProfilesByCriteriaForFilteredApp(List<Integer> jobSkills, List<Integer> jobFuncList, long expMin,
                                                        long expMax, List<Long> applicantIds, List<Integer> jobSecFuncList, Pageable pageable);


}
