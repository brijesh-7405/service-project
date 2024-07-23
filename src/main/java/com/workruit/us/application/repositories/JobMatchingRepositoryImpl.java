/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.models.JobMatch;
import com.workruit.us.application.models.JobMatchConsultancy;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Mahesh
 */
@Repository
public class JobMatchingRepositoryImpl {

    private final static int BATCH_SIZE = 500;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void bulkSaveAllJobMatches(List<JobMatch> entities) throws Exception {

        String sql = "insert ignore into job_match (job_post_id, applicant_id, is_consultancy, recruiter_id, match_score, consultancy_user_id) "
                + "VALUES (?,?,?,?,?,?) ";
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int i = 0;
                for (JobMatch jobMatch : entities) {
                    preparedStatement.setLong(1, jobMatch.getJobPostId());
                    preparedStatement.setLong(2, jobMatch.getApplicantId());
                    preparedStatement.setBoolean(3, jobMatch.isConsultancy());
                    preparedStatement.setLong(4, jobMatch.getRecruiterId());
                    preparedStatement.setLong(5, jobMatch.getMatchScore());
                    preparedStatement.setLong(6, jobMatch.getConsultancyUserId() != null ? jobMatch.getConsultancyUserId() : 0);
                    preparedStatement.addBatch();
                    //Batch size: 50
                    if (++i % BATCH_SIZE == 0) {
                        preparedStatement.executeBatch();
                    }
                }
                preparedStatement.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        session.close();
    }


    @Transactional
    public void bulkSaveAllJobMatchConsultancy(List<JobMatchConsultancy> jobMatchConsultancyList) {
        String sql = "insert ignore into job_match_consultancy (job_post_id, applicant_id, consultancy_id, recruiter_id,match_score,consultancy_user_id,created_date,updated_date,recommended,recommended_score) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?) ";
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int i = 0;
                for (JobMatchConsultancy jobMatch : jobMatchConsultancyList) {
                    preparedStatement.setLong(1, jobMatch.getJobPostId());
                    preparedStatement.setLong(2, jobMatch.getApplicantId());
                    preparedStatement.setLong(3, jobMatch.getConsultancyId());
                    preparedStatement.setLong(4, jobMatch.getRecruiterId());
                    preparedStatement.setLong(5, jobMatch.getMatchScore());
                    preparedStatement.setLong(6, jobMatch.getConsultancyUserId() != null ? jobMatch.getConsultancyUserId() : 0);
                    preparedStatement.setDate(7, new Date(jobMatch.getCreatedDate().getTime()));
                    preparedStatement.setDate(8, new Date(jobMatch.getUpdatedDate().getTime()));
                    preparedStatement.setInt(9,jobMatch.getRecommended());
                    preparedStatement.setLong(10,jobMatch.getRecommendedScore());
                    preparedStatement.addBatch();
                    //Batch size: 50
                    if (++i % BATCH_SIZE == 0) {
                        preparedStatement.executeBatch();
                    }
                }
                preparedStatement.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        session.close();
    }
}
