package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByApplicantId(Long applicantId);

    void deleteByApplicantId(Long applicantId);
}
