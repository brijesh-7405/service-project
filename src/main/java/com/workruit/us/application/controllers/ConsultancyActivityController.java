/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.service.SaveProfileService;
import com.workruit.us.application.services.ConsultancyActivityService;
import com.workruit.us.application.services.JobPostService;
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

/**
 * @author Mahesh
 */
@Slf4j
@RestController
@RequestMapping("/consactivity")
public class ConsultancyActivityController {

    private @Autowired ConsultancyActivityService consultancyActivityService;
    private @Autowired MessageSource messageSource;
    private @Autowired JobPostService jobPostService;
    private @Autowired SaveProfileService saveProfileService;

    /**
     * Consultancy Activity page display with statistics
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/consultancy/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getConsultancyJobActivity(@PathVariable("consultancyId") long consultancyId,
                                                    @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                    @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            ConsActivityViewResponse activityViewResponse = consultancyActivityService
                    .getUserJobActivityForConsultancy(consultancyId, pageNo, 100);
            ApiResponse apiResponse = ApiResponse.builder().data(activityViewResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getConsultancyJobActivity : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all applied profiles for job
     *
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/consultancy/{consultancyId}/jobs/{jobId}/status/{status}/filter/{filter}/applied")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAppliedProfilesForJob(@PathVariable("consultancyId") long consultancyId,
                                                   @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                   @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsActivityAppliedResponse activityAppliedResponse = consultancyActivityService
                    .getAppliedProfilesForJob(consultancyId, userDetailsDTO.getId(), jobId, status, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityAppliedResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getAppliedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all interviewed profiles for job
     *
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/consultancy/{consultancyId}/jobs/{jobId}/status/{status}/filter/{filter}/interview")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getInterviewProfilesForJob(@PathVariable("consultancyId") long consultancyId,
                                                     @PathVariable("jobId") long jobId, @PathVariable("status") Integer status, @PathVariable("filter") int filter,
                                                     @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                     @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsActivityInterviewResponse activityInterviewResponse = consultancyActivityService
                    .getInterviewProfilesForJob(consultancyId, userDetailsDTO.getId(), jobId, userDetailsDTO.getRoles().get(0), status, filter, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityInterviewResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getInterviewProfilesForJob cons failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/interviewsList")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getInterviewProfilesForDashboard(
            @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
            @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsActivityInterviewResponse activityInterviewResponse = consultancyActivityService
                    .getInterviewProfilesForDashboard(userDetailsDTO.getId(), userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityInterviewResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getInterviewProfilesForJob cons failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Submit feedback for interview by consultancy
     *
     * @param interviewId
     * @param feedbackDTO
     * @return
     */
    @PostMapping("/submitfeedback/{interviewId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity submitFeedback(@PathVariable("interviewId") long interviewId,
                                         @RequestBody @Valid InterviewFeedbackDTO feedbackDTO) {
        try {
            interviewId = consultancyActivityService.submitFeedback(interviewId, feedbackDTO);
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Submit feedback failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all Hired profiles for job
     *
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/consultancy/{consultancyId}/jobs/{jobId}/status/{status}/filter/{filter}/hired")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getHiredProfilesForJob(@PathVariable("consultancyId") long consultancyId,
                                                 @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                 @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                 @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsActivityHiredResponse activityHiredResponse = consultancyActivityService
                    .getHiredProfilesForJob(consultancyId, userDetailsDTO.getId(), jobId, status, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityHiredResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getHiredProfilesForJob cons failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all Rejected profiles for job
     *
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/consultancy/{consultancyId}/jobs/{jobId}/status/{status}/filter/{filter}/rejected")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getRejectedProfilesForJob(@PathVariable("consultancyId") long consultancyId,
                                                    @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                    @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                    @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsActivityRejectedResponse activityHiredResponse = consultancyActivityService
                    .getRejectedProfileForJob(consultancyId, userDetailsDTO.getId(), jobId, status, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityHiredResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getRejectedProfilesForJob in consultance failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/consultancy/jobs/{filter}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getConsultancyJobActivityStatus(@PathVariable("filter") int filter,
                                                          @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                          @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(consultancyActivityService.getConsActivityJobList(userDetailsDTO.getId(),
                            userDetailsDTO.getConsultancyId(), filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize))
                    .description(messageSource.getMessage("consultancy.job.activity.filter.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.activity.filter.success.title", new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getConsultancyJobActivity : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.job.activity.filter.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.activity.filter.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/consultancy/dashboardJobs/{filter}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getConsultancyDashboardJobActivityStatus(@PathVariable("filter") long filter,
                                                                   @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                                   @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(consultancyActivityService.getConsDashboardActivityJobList(userDetailsDTO.getId(),
                            userDetailsDTO.getConsultancyId(), filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize))
                    .description(messageSource.getMessage("consultancy.job.activity.filter.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.activity.filter.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getConsultancyJobActivity : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.job.activity.filter.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.activity.filter.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }


    @PostMapping("/job/{jobPostId}/applicant/{applicantId}/interview/status/{status}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateApplicantInterviewStatus(@PathVariable("jobPostId") long jobPostId,
                                                         @PathVariable("applicantId") long applicantId,
                                                         @PathVariable("status") int status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            consultancyActivityService.updateApplicantInterviewStatus(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), jobPostId, applicantId, status);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data("")
                    .description(messageSource.getMessage("consultancy.applicant.job.interview.status.update.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.applicant.job.interview.status.update.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (WorkruitException e) {
            log.error("updateApplicantInterviewStatus : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.applicant.job.interview.status.update.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("updateApplicantInterviewStatus : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.applicant.job.interview.status.update.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.applicant.job.interview.status.update.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
