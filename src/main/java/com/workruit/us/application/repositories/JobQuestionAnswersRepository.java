package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.JobQuestionAnswerResultSet;
import com.workruit.us.application.models.JobQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.workruit.us.application.models.JobQuestionAnswers;

import java.util.List;


@Repository
public interface JobQuestionAnswersRepository extends JpaRepository<JobQuestionAnswers, Long> {


    @Query(value = "SELECT jq.question_id as questionId,jq.question_title as questionTitle,jq.question_type as questionType,jqa.question_ans_value as questionAnsValue FROM job_question_answers jqa " +
            " inner join job_question jq on jq.question_id=jqa.question_id " +
            " WHERE jqa.job_post_id = :jobPostId AND jqa.application_id = :applicantId", nativeQuery = true)
    List<JobQuestionAnswerResultSet> findByJobPostIdApplicantId(Long jobPostId, Long applicantId);


}
