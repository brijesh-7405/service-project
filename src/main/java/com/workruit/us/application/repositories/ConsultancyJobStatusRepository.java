package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.ConsultancyJobStatusResultSet;
import com.workruit.us.application.models.ConsultancyJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ConsultancyJobStatusRepository extends JpaRepository<ConsultancyJobStatus, Long> {

    Optional<ConsultancyJobStatus> findByJobPostIdAndConsultancyUserId(long jobId, long userId);

    @Query(value = "select * from consultancy_job_status cjs where cjs.job_post_id=?1 and cjs.consultancy_user_id in(?2) ", nativeQuery = true)
    Optional<ConsultancyJobStatus> findByJobPostIdAndConsultancyUserId(long jobId, List<Long> userIds);


    @Query(value = "SELECT (7-ABS(DATEDIFF(cjs.created_date, CURDATE()))) as daysLeft,jp.title as jobTitle, cjs.consultancy_id as consultancyId,cjs.consultancy_user_id as consultancyUserId,cjs.job_post_id as jobPostId FROM consultancy_job_status cjs\n" +
            "inner join job_post jp on jp.job_post_id=cjs.job_post_id\n" +
            "WHERE cjs.status = 1 \n" +
            "AND (ABS(DATEDIFF(cjs.created_date, CURDATE())) = 5 OR ABS(DATEDIFF(cjs.created_date, CURDATE())) = 2) ", nativeQuery = true)
    List<ConsultancyJobStatusResultSet> findAllBySavedStatus();

    @Query(value = " select consultancy_job_status_id from consultancy_job_status cjs where cjs.status=1 and cjs.created_date <= CURRENT_DATE - 7 ", nativeQuery = true)
    List<ConsultancyJobStatus> getAllExpiredSavedJobs();

    @Modifying
    @Transactional
    @Query(value = "DELETE cj FROM consultancy_job_status cj INNER JOIN (SELECT distinct consultancy_job_status_id FROM consultancy_job_status   GROUP BY consultancy_job_status_id  )" +
            " cjs on cjs.consultancy_job_status_id = cj.consultancy_job_status_id and cj.status=1 and cj.created_date <= CURRENT_DATE - 7", nativeQuery = true)
    void deleteSavedJobs();

}
