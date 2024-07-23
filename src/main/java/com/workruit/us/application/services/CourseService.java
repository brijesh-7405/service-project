package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.CourseDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.Course;
import com.workruit.us.application.repositories.ApplicantRepository;
import com.workruit.us.application.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Component
public class CourseService {

    private @Autowired CourseRepository courseRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ApplicantService applicantService;

    public void updateApplicantCourse(List<CourseDTO> courseDTOs, Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));
        List<Course> oldCourses = courseRepository.findByApplicantId(applicantId);
        List<Course> currentCourses = new ArrayList<>();
        for (CourseDTO courseDTO : courseDTOs) {
            Course course = new Course();
            course.setCourseId(courseDTO.getCourseId());
            course.setApplicantId(applicantId);
            course.setCourseDuration(courseDTO.getCourseDuration());
            course.setCourseTitle(courseDTO.getCourseTitle());
            course.setDescription(courseDTO.getDescription());
            if (courseDTO.getCourseId() == null || courseDTO.getCourseId() == 0) {
                course.setCreatedDate(new Date());
            }
            course.setInstitutionName(courseDTO.getInstitutionName());
            course.setStillPursuing(courseDTO.isStillPursuing());
            course.setUpdatedDate(new Date());
            courseRepository.save(course);
            currentCourses.add(course);
        }
        Set<Long> courseIds = currentCourses.stream().map(e -> e.getCourseId()).collect(Collectors.toSet());
        oldCourses.removeIf(e -> courseIds.contains(e.getCourseId()));
        for (Course course : oldCourses) {
            courseRepository.deleteById(course.getCourseId());
        }
        applicant.setCorrectionRequired(!applicantService.isCorrectionSolved(applicantId));
        applicantRepository.save(applicant);
    }

    public List<CourseDTO> getApplicantCourse(Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));
        List<Course> courses = courseRepository.findByApplicantId(applicantId);
        return courses.stream().map(courseDTO -> {
            CourseDTO course = new CourseDTO();
            course.setCourseDuration(courseDTO.getCourseDuration());
            course.setDescription(courseDTO.getDescription());
            course.setCourseTitle(courseDTO.getCourseTitle());
            course.setInstitutionName(courseDTO.getInstitutionName());
            course.setStillPursuing(courseDTO.isStillPursuing());
            course.setCourseId(courseDTO.getCourseId());
            return course;
        }).collect(Collectors.toList());
    }
}