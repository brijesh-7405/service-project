package com.workruit.us.application.repositories;

import com.workruit.us.application.models.JobQuestionValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobQuestionValuesRepository extends JpaRepository<JobQuestionValues, Long> {

}