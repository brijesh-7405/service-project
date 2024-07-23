/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.services.*;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController
public class ApplicantController {

    private @Autowired WorkExperienceService workExperienceService;
    private @Autowired InternshipService internshipService;
    private @Autowired EducationHistoryService educationHistoryService;
    private @Autowired ProjectService projectService;
    private @Autowired CourseService courseService;
    private @Autowired ApplicantService applicantService;
    private @Autowired ApplicantDetailsService applicantDetailsService;
    private @Autowired ReferenceService referenceService;
    private @Autowired PublicationService publicationService;
    private @Autowired MessageSource messageSource;
    private @Autowired CertificateService certificateService;

    private @Autowired SocialMediaLinksService socialMediaLinksService;

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/profile/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity profile(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(applicantService.profile(applicantId))
                    .description(
                            messageSource.getMessage("applicant.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get applicant profile failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/applicant/profile/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateProfile(@Valid @RequestBody UpdateApplicantProfileAndDetailsDTO updateApplicantProfileAndDetailsDTO, @PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            applicantService.updateProfileAndDetails(updateApplicantProfileAndDetailsDTO, applicantId, userDetailsDTO);

            ApiResponse apiResponse = ApiResponse.builder().data("")
                    .description(
                            messageSource.getMessage("applicant.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (AuthenticationException ae) {
            log.error("Error while saving the user", ae);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{ae.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            return responseEntity;
        } catch (Exception e) {
            log.error("Update applicant profile failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/publication/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updatePublication(@RequestBody List<PublicationDTO> publicationDTO, @PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            publicationService.updatePublication(publicationDTO, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.publication.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.publication.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updatePublication : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.publication.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.publication.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/publication/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getPublications(@PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(publicationService.getPublications(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.publication.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.publication.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getPublications : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.publication.get.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.publication.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/reference/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateReference(@RequestBody List<ReferenceDTO> referenceDTOs, @PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            referenceService.updateReference(referenceDTOs, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.reference.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.reference.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateReference : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.reference.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.reference.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/reference/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getReferences(@PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(referenceService.getReferences(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.reference.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.reference.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getReferences : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.reference.get.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.reference.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/applicant/details")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantProfile(@RequestBody ApplicantDetailsDTO applicantDetailsDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            applicantDetailsService.updateApplicantDetails(applicantDetailsDTO, userDetailsDTO.getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/details")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity<>(applicantDetailsService.getApplicantDetails(userDetailsDTO.getConsultancyId()),
                    HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/work-experience/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantExperience(@PathVariable("applicantId") Long applicantId, @RequestBody List<WorkExperienceDTO> workExperienceDTOList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            workExperienceService.updateApplicantWorkExperience(workExperienceDTOList, applicantId, userDetailsDTO.getConsultancyId());

            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.work.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.work.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant experience failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.work.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.work.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/internship/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantInternship(@RequestBody List<InternshipDTO> internshipDTOs, @PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            internshipService.updateInternship(internshipDTOs, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.internship.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.internship.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateApplicantInternship : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.internship.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.internship.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/education/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantEducationHistory(@PathVariable("applicantId") Long applicantId, @RequestBody List<EducationHistoryDTO> educationHistoryDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            educationHistoryService.updateApplicantEducationHistory(educationHistoryDTO, applicantId, userDetailsDTO.getConsultancyId());

            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.education.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.education.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant education history failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.education.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.education.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/project/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantProject(@PathVariable("applicantId") Long applicantId, @RequestBody List<ProjectDTO> projectDTOList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            projectService.updateApplicantProjects(projectDTOList, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.project.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.project.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant project failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.project.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.project.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/applicant/course/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantCourse(@RequestBody List<CourseDTO> courseDTO, @PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            courseService.updateApplicantCourse(courseDTO, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.courses.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.courses.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("updateApplicantCourse : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.courses.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.courses.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/work-experience/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantExperience(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(workExperienceService.getApplicantWorkExperience(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.work.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.work.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get applicant experience failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.work.get.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.work.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/internship/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantInternship(@PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(internshipService.getInternship(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.internship.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.internship.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getApplicantInternship : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.internship.get.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.internship.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/education/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantEducationHistory(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(educationHistoryService.getApplicantEducationHistory(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.education.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.education.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant education failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.education.get.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.education.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/project/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantProject(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(projectService.getApplicantProjects(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.project.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.project.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get applicant project failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.project.get.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.project.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/applicant/course/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantCourse(@PathVariable Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(courseService.getApplicantCourse(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.course.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.course.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getApplicantCourse : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.course.get.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.course.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @SuppressWarnings("rawtypes")
    @DeleteMapping("/applicant/work-experience/{workExperienceId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity deleteApplicantExperience(@PathVariable("workExperienceId") Long workExperienceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            workExperienceService.deleteWorkExperience(userDetailsDTO.getConsultancyId(), workExperienceId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }


    @GetMapping("/applicant/certificate/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantCertificates(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(certificateService.getApplicantCertificate(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.certificate.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get applicant certificate failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.certificate.get.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.get.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/applicant/certificate/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantCertificate(@PathVariable("applicantId") Long applicantId, @Valid @RequestBody List<CertificationDTO> certificationDTOList) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            Long certificateId = certificateService.updateApplicantCertificate(certificationDTOList, applicantId, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(certificateService.getApplicantCertificate(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("applicant.certificate.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant certificate failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.certificate.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/applicant/certificate/{certificateId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity deleteApplicantCertificate(@PathVariable("certificateId") Long certificateId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            certificateService.getCertificateById(certificateId).orElseThrow(() -> new WorkruitException(String.format("Certificate is not found with id: %s", certificateId)));
            certificateService.deleteCertificate(certificateId);

            ApiResponse apiResponse = ApiResponse.builder().data(userDetailsDTO.getId())
                    .description(
                            messageSource.getMessage("applicant.certificate.delete.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.delete.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Delete applicant certificate failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.certificate.delete.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.certificate.delete.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/applicant/uploaded-applicant")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity recentUploaded(@RequestParam(name = "firstName", required = false) String firstName, @RequestParam(name = "recentUploaded", required = true) boolean recentUploaded,
                                         @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                         @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(applicantService.recentUpload(userDetailsDTO.getId(), userDetailsDTO.getConsultancyId(), firstName, recentUploaded, userDetailsDTO.getRoles().get(0), pageNo, pageSize))
                    .description(
                            messageSource.getMessage("upload.applicant.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("upload.applicant.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("recentUploaded : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("upload.applicant.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("upload.applicant.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity deleteApplicant(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            applicantService.deleteApplicant(applicantId, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());

            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.delete.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.delete.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Delete applicant failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.delete.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.delete.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/applicant/{applicantId}/status/{status}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateApplicantCertificate(@PathVariable("applicantId") Long applicantId, @PathVariable("status") int status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            applicantService.updateApplicantStatus(applicantId, userDetailsDTO.getConsultancyId(), status);
            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.status.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.status.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update applicant certificate failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.status.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.status.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/applicant/uploaded-applicant/filter")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity recentUploadedFilter(@RequestBody ApplicantFilterDTO applicantFilterDTO, @RequestParam(name = "recentUploaded", required = true) boolean recentUploaded,
                                               @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                               @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(applicantService.filterRecentUploaded(applicantFilterDTO, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), recentUploaded, userDetailsDTO.getRoles().get(0), pageNo, pageSize))
                    .description(
                            messageSource.getMessage("upload.applicant.filter.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("upload.applicant.filter.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("recentUploaded : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("upload.applicant.filter.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("upload.applicant.filter.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/applicant/social-media-links/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateSocialMediaLinks(@PathVariable("applicantId") Long applicantId, @RequestBody SocialMediaLinksDTO socialMediaLinksDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            socialMediaLinksService.updateSocialMediaLinks(socialMediaLinksDTO, applicantId, userDetailsDTO.getConsultancyId());

            ApiResponse apiResponse = ApiResponse.builder().data(applicantId)
                    .description(
                            messageSource.getMessage("applicant.social.media.links.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.social.media.links.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Update social media link failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.social.media.links.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.social.media.links.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/applicant/social-media-links/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getSocialMediaLinks(@PathVariable("applicantId") Long applicantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(socialMediaLinksService.getApplicantSocialMediaLinks(applicantId, userDetailsDTO.getConsultancyId()))
                    .description(
                            messageSource.getMessage("get.applicant.social.media.links.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.applicant.social.media.links.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("get applicant social media link failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.applicant.social.media.links.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.applicant.social.media.links.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/applicant/profile")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity createProfile(@Valid @RequestBody UpdateApplicantProfileAndDetailsDTO updateApplicantProfileAndDetailsDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(applicantService.createProfileAndDetails(updateApplicantProfileAndDetailsDTO, userDetailsDTO))
                    .description(
                            messageSource.getMessage("applicant.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (AuthenticationException ae) {
            log.error("Error while saving the user", ae);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(ae.getLocalizedMessage())
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            return responseEntity;
        } catch (Exception e) {
            log.error("Create applicant profile failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
