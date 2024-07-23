/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.services.FirebaseMessagingService;
import com.workruit.us.application.services.TalentViewService;
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
@RequestMapping("/talent")
public class TalentViewController {

    private @Autowired TalentViewService talentViewService;
    private @Autowired MessageSource messageSource;
    private @Autowired FirebaseMessagingService firebaseService;

    /**
     * Method to return talent based on filter search
     *
     * @param jobPostId
     * @param jobPostDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/{jobId}/filtersearch")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedUsersForJobByFilter(@PathVariable("jobId") long jobPostId,
                                                        @RequestBody JobPostDTO jobPostDTO, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                        @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            TalentViewResponse jobViewResponse = talentViewService.getMatchedUsersForJobByFilter(jobPostId, jobPostDTO,
                    pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(
                            messageSource.getMessage("telnet.search.job.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.search.job.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Get matched users by filter search for job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("telnet.search.job.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.search.job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Method to return matched users for a job - pre-matched
     *
     * @param jobPostId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/{jobId}/matchedusers")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedUsersForJob(@PathVariable("jobId") long jobPostId,
                                                @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            TalentViewResponse jobViewResponse = talentViewService.getMatchedUsersForJob(jobPostId, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("telnet.match.user.job.success.description", new Object[]{},
                            null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.match.user.job.success.title", new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched users for job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("telnet.match.user.job.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.match.user.job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * 2---View all matched profiles from consultancy for selected Job
     *
     * @param jobPostId
     * @param consultancyId
     * @return
     */
    @GetMapping("/{jobId}/matchedusers/consultancy/{consultancyId}/status/{status}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedProfilesForConsultancy(@PathVariable("jobId") long jobPostId,
                                                           @PathVariable("consultancyId") long consultancyId, @PathVariable("status") int status,
                                                           @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                           @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            TalentViewConsDetailsDTO jobViewResponse = talentViewService.getMatchedProfilesForConsultancy(jobPostId,
                    consultancyId, status, pageNo, pageSize);

            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.matched.user-profile.by.consultancy.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.matched.user-profile.by.consultancy.success.title", new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched profiles for consultancy failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.matched.user-profile.by.consultancy.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.matched.user-profile.by.consultancy.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Update matched applicant from consultancy state Save/Shortlist/Reject
     *
     * @param jobPostId
     * @param consultancyId
     * @param applicantId
     * @return
     */
    @PostMapping("/{jobId}/matchedusers/consultancy/{consultancyId}/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateJobApplicantState(@PathVariable("jobId") long jobPostId,
                                                  @PathVariable("consultancyId") long consultancyId, @PathVariable("applicantId") long applicantId,
                                                  @RequestBody @Valid UpdateJobStateDTO jsonReq) {
        try {
            String action = jsonReq.getAction();
            if (!("Save".equalsIgnoreCase(action) || "Shortlist".equalsIgnoreCase(action)
                    || "Reject".equalsIgnoreCase(action))) {
                ApiResponse apiResponse = ApiResponse.builder().data(-1)
                        .description(messageSource.getMessage(
                                "update.matched-applicant.consultancy.state.fail.description", new Object[]{}, null))
                        .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                        .title(messageSource.getMessage("update.matched.user-profile.by.consultancy.fail.title",
                                new Object[]{}, null))
                        .build();
                return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            talentViewService.updateJobApplicantState(jobPostId, consultancyId, applicantId, jsonReq, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(jobPostId)
                    .description(messageSource.getMessage(
                            "update.matched-applicant.consultancy.state.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("update.matched-applicant.consultancy.state.success.title",
                            new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get applicants from consultancy failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.matched.user-profile.by.consultancy.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.matched.user-profile.by.consultancy.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Direct matched users - not from consultancy
     *
     * @param applicantId
     * @return
     */
    @GetMapping("/matchedusers/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getProfileInfoForRecruiter(@PathVariable("applicantId") long applicantId) {
        try {
            ApplicantDTO jobViewResponse = talentViewService.getProfileInfoForJob(applicantId);

            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.applicant.profile.job-info.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.applicant.profile.job-info.success.title", new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(jobViewResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get profile info for job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.applicant.profile.job-info.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.applicant.profile.job-info.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Direct matched users - not from consultancy
     *
     * @param applicantId
     * @return
     */
    @GetMapping("/matchedusers/applicant/{applicantId}/job/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity<ApplicantDTO> getProfileInfoForJob(@PathVariable("applicantId") long applicantId,
                                                             @PathVariable("jobId") long jobId) {
        try {
            ApplicantDTO jobViewResponse = talentViewService.getProfileInfoForJob(applicantId, jobId);
            return new ResponseEntity<ApplicantDTO>(jobViewResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get profile info for job failed : ", e);
            return ControllerUtils.genericErrorMessage();
        }

    }

    @PostMapping("/sendNofitication")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity sendNofitication(@RequestBody NotificationTestDTO notificationTestDTO) {
        try {
            firebaseService.prepareNotifObject(notificationTestDTO.getTitle(), notificationTestDTO.getMessage(),
                    notificationTestDTO.getToken());
            ApiResponse apiResponse = ApiResponse.builder().data(20)
                    .description(messageSource.getMessage(
                            "update.job.applicant.state.for.applicant.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("update.job.applicant.state.for.applicant.success.title",
                            new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get profile info for job failed : ", e);
            return ControllerUtils.genericErrorMessage();
        }

    }

    /**
     * Direct matched users update job appl state - not from consultancy
     *
     * @param jobPostId
     * @param applicantId
     * @return
     */
    @PostMapping("/talent/{jobId}/matchedusers/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateJobApplicantStateForApplicant(@PathVariable("jobId") long jobPostId,
                                                              @PathVariable("applicantId") long applicantId, @RequestBody @Valid UpdateJobStateDTO jsonReq) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            String action = jsonReq.getAction();
            if (!("Save".equalsIgnoreCase(action) || "Shortlist".equalsIgnoreCase(action)
                    || "Reject".equalsIgnoreCase(action))) {
                ApiResponse apiResponse = ApiResponse.builder().data(-1)
                        .description(messageSource.getMessage(
                                "update.job.applicant.state.for.applicant.fail.description", new Object[]{}, null))
                        .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                        .title(messageSource.getMessage("update.job.applicant.state.for.applicant.fail.title",
                                new Object[]{}, null))
                        .build();
                return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (userDetailsDTO.getRoles().get(0).equalsIgnoreCase("CONSULTANCY_ADMIN")
                    || userDetailsDTO.getRoles().get(0).equalsIgnoreCase("CONSULTANCY_MANAGER")) {
                talentViewService.updateJobApplicantStateforConsultancy(jobPostId, userDetailsDTO.getConsultancyId(),
                        applicantId, jsonReq);
            } else {
                talentViewService.updateJobApplicantStateForApplicant(jobPostId, applicantId, jsonReq, userDetailsDTO.getConsultancyId());
            }
            ApiResponse apiResponse = ApiResponse.builder().data(jobPostId)
                    .description(messageSource.getMessage(
                            "update.job.applicant.state.for.applicant.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("update.job.applicant.state.for.applicant.success.title",
                            new Object[]{}, null))
                    .build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("update job applicant state for applicant failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("update.job.applicant.state.for.applicant.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("update.job.applicant.state.for.applicant.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/filter")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getTalentByFilter(@RequestBody TalentFilterDTO talentFilterDTO,
                                            @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                            @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(talentViewService.getUsersByFilter(talentFilterDTO, userDetailsDTO.getConsultancyId(),userDetailsDTO.getRoles().get(0),pageNo, pageSize))
                    .description(
                            messageSource.getMessage("telnet.search.filter.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.search.filter.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Get users by filter search failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("telnet.search.filter.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("telnet.search.filter.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/filter/{consultancyId}/applicants")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getApplicantsProfilesForConsultancy(@PathVariable("consultancyId") long consultancyId,
                                                              @RequestBody TalentFilterDTO talentFilterDTO,
                                                              @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                              @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(talentViewService.getApplicantsProfilesForConsultancy(talentFilterDTO, consultancyId, pageNo, pageSize))
                    .description(messageSource.getMessage("get.matched.user-profile.by.consultancy.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.matched.user-profile.by.consultancy.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched profiles for consultancy failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.matched.user-profile.by.consultancy.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.matched.user-profile.by.consultancy.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
