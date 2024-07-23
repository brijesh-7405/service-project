/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.services.ActivityService;
import com.workruit.us.application.services.ConsultancyService;
import com.workruit.us.application.services.FirebaseMessagingService;
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
import java.util.Map;

/**
 * @author Mahesh
 */
@Slf4j
@RestController
@RequestMapping("/activity")
public class ActivityController {

    private final FirebaseMessagingService firebaseService;
    private @Autowired ActivityService activityService;
    private @Autowired MessageSource messageSource;
    private @Autowired ConsultancyService consultancyService;


    public ActivityController(FirebaseMessagingService firebaseService) {
        this.firebaseService = firebaseService;
    }

    /**
     * Activity page display with statistics
     *
     * @param recruiterId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/recruiter/{recruiterId}/filter/{filter}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobActivityForRecruiter(@PathVariable("recruiterId") long recruiterId,
                                                     @PathVariable("filter") int filter, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                     @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityViewResponse activityViewResponse = new ActivityViewResponse();
            if (filter == 0) {
                activityViewResponse = activityService.getJobActivityForRecruiter(recruiterId,
                        userDetailsDTO.getConsultancyId(), JobStatus.ACTIVE, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            } else {
                activityViewResponse = activityService.getJobActivityForRecruiter(userDetailsDTO.getId(),
                        userDetailsDTO.getConsultancyId(), JobStatus.ACTIVE, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            }
            ApiResponse apiResponse = ApiResponse.builder().data(activityViewResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getJobActivityForRecruiter : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/consultancy/{consultantId}/getProfile")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getConsultancyProfile(@PathVariable("consultantId") long consultantId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsultancyDetailsDTO consultancyDetailsDTO = consultancyService.getConsultancyDetails(consultantId, userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(consultancyDetailsDTO)
                    .description(
                            messageSource.getMessage("consultant.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultant.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultant.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultant.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Method to get all shortlisted profiles for job (Liked)
     *
     * @param recruiterId
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
//	@GetMapping("/recruiter/{recruiterId}/jobs/{jobId}/shortlisted")
//	@ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
//	public ResponseEntity getShortlistedProfilesForJob(@PathVariable("recruiterId") long recruiterId,
//			@PathVariable("jobId") long jobId, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
//			@RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
//		try {
//			ActivityShortlistedResponse activityShortlistedResponse = activityService
//					.getShortlistedProfilesForJob(recruiterId, jobId, pageNo, pageSize);
//			ApiResponse apiResponse = ApiResponse.builder().data(activityShortlistedResponse)
//					.description(messageSource.getMessage("activity.create.success.description", new Object[] {}, null))
//					.status(messageSource.getMessage("success.status", new Object[] {}, null))
//					.title(messageSource.getMessage("activity.create.success.title", new Object[] {}, null)).build();
//			return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//
//		} catch (Exception e) {
//			log.error("getShortlistedProfilesForJob failed : ", e);
//			ApiResponse apiResponse = ApiResponse.builder().data(-1)
//					.description(messageSource.getMessage("activity.create.fail.description", new Object[] {}, null))
//					.status(messageSource.getMessage("fail.status", new Object[] {}, null))
//					.title(messageSource.getMessage("activity.create.fail.title", new Object[] {}, null)).build();
//			return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
//		}
//
//	}
    @GetMapping("/recruiter/{recruiterId}/jobs/{jobId}/status/{status}/filter/{filter}/shortlisted")
    // @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getShortlistedProfilesForJob(@PathVariable("recruiterId") long recruiterId,
                                                       @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                       @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                       @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityShortlistedResponse activityShortlistedResponse = activityService
                    .getShortlistedProfilesForJob(userDetailsDTO.getConsultancyId(), recruiterId, jobId, status, filter, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityShortlistedResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getShortlistedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Schedule an interview for shortlisted profile
     *
     * @param recruiterId
     * @param jobId
     * @param applicantId
     * @param interviewDTO
     * @return
     */
    @PostMapping("/recruiter/{recruiterId}/jobs/{jobId}/shortlisted/applicant/{applicantId}/scheduleinterview")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity scheduleInterview(@PathVariable("recruiterId") long recruiterId,
                                            @PathVariable("jobId") long jobId, @PathVariable("applicantId") long applicantId,
                                            @RequestBody @Valid InterviewDTO interviewDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            Long interviewId = activityService.scheduleInterviewForApplicant(recruiterId, jobId, applicantId,
                    interviewDTO, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(
                            messageSource.getMessage("interview.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("scheduleInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.create.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Update the scheduled interview
     *
     * @param interviewId
     * @param interviewDTO
     * @return
     */
    @PutMapping("/scheduleinterview/{interviewId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateScheduledInterview(@PathVariable("interviewId") long interviewId,
                                                   @RequestBody @Valid InterviewDTO interviewDTO) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            interviewId = activityService.updateInterviewForApplicant(userDetailsDTO.getId(), interviewId, interviewDTO, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(
                            messageSource.getMessage("interview.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateScheduledInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all interviewed profiles for job
     *
     * @param recruiterId
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/recruiter/{recruiterId}/jobs/{jobId}/status/{status}/filter/{filter}/interview")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getInterviewProfilesForJob(@PathVariable("recruiterId") long recruiterId,
                                                     @PathVariable("jobId") long jobId, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                     @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                     @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityInterviewResponse activityInterviewResponse = activityService.getInterviewProfilesForJobData(
                    userDetailsDTO.getConsultancyId(), recruiterId, jobId, userDetailsDTO.getRoles().get(0), status, filter, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityInterviewResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getShortlistedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/recruiter/{recruiterId}/status/{status}/userinterviews")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllInterviewProfilesForUser(@PathVariable("recruiterId") long recruiterId,
                                                         @RequestParam(defaultValue = "0", name = "page") Integer pageNo, @PathVariable("status") int status,
                                                         @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityInterviewResponse activityInterviewResponse = activityService.getAllInterviewProfilesForJobData(
                    userDetailsDTO.getConsultancyId(), recruiterId, userDetailsDTO.getRoles().get(0), status, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityInterviewResponse)
                    .description(messageSource.getMessage("get.all-interview.profile.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-interview.profile.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getShortlistedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-interview.profile.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-interview.profile.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/recruiter/{recruiterId}/interviews")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getInterviewProfile(@PathVariable("recruiterId") long recruiterId,
                                              @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                              @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            ActivityInterviewResponse activityInterviewResponse = activityService
                    .getInterviewProfilesForRecruiter(recruiterId, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityInterviewResponse)
                    .description(messageSource.getMessage("get.interview-profile.success.description", new Object[]{},
                            null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.interview-profile.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getShortlistedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.interview-profile.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.interview-profile.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/submitfeedback/{interviewId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity submitFeedback(@PathVariable("interviewId") long interviewId,
                                         @RequestBody @Valid InterviewFeedbackDTO feedbackDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            interviewId = activityService.submitFeedback(interviewId, feedbackDTO, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(messageSource.getMessage("interview.feedback.submission.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("interview.feedback.submission.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateScheduledInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.feedback.submission.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.feedback.submission.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping("/submitfeedback/{interviewId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateFeedback(@PathVariable("interviewId") long interviewId,
                                         @RequestBody @Valid InterviewFeedbackDTO feedbackDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            interviewId = activityService.updateFeedback(interviewId, feedbackDTO, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(messageSource.getMessage("interview.feedback.submission.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("interview.feedback.submission.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateScheduledInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.feedback.submission.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.feedback.submission.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/getfeedback/{interviewId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getFeedback(@PathVariable("interviewId") long interviewId) {
        try {
            InterviewFeedbackDTO interviewFeedback = activityService.getFeedback(interviewId);
            ApiResponse apiResponse = ApiResponse.builder().data(interviewFeedback)
                    .description(messageSource.getMessage("interview.feedback.submission.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("interview.feedback.submission.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updateScheduledInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.feedback.submission.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.feedback.submission.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to update interview status hired/reject
     *
     * @param recruiterId
     * @param jobId
     * @param applicantId
     * @param requestBody
     * @return
     */
    @PostMapping("/recruiter/{recruiterId}/jobs/{jobId}/shortlisted/applicant/{applicantId}/interviewstatus")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateInterviewStatus(@PathVariable("recruiterId") long recruiterId,
                                                @PathVariable("jobId") long jobId, @PathVariable("applicantId") long applicantId,
                                                @RequestBody Map<String, Integer> requestBody) {
        try {
            Long interviewId = activityService.updateInterviewStatus(recruiterId, jobId, applicantId,
                    requestBody.get("status"));
            ApiResponse apiResponse = ApiResponse.builder().data(interviewId)
                    .description(messageSource.getMessage(InterviewStatus.HIRED.getValue() == requestBody.get("status")
                            ? "interview.selection.success.description"
                            : "interview.rejection.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))


                    .title(messageSource.getMessage("interview.status.update.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("scheduleInterview failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("interview.status.update.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("interview.status.update.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all Hired profiles for job
     *
     * @param recruiterId
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/recruiter/{recruiterId}/jobs/{jobId}/status/{status}/filter/{filter}/hired")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getHiredProfilesForJob(@PathVariable("recruiterId") long recruiterId,
                                                 @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                 @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                 @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityHiredResponse activityHiredResponse = activityService.getHiredProfilesForJob(userDetailsDTO.getConsultancyId(), recruiterId, jobId, userDetailsDTO.getRoles().get(0),
                    status, filter, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityHiredResponse)
                    .description(messageSource.getMessage("get.all-hired.profiles.job.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-hired.profiles.job.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getHiredProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-hired.profiles.job.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-hired.profiles.job.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/recruiter/{recruiterId}/jobs/{jobId}/applicant/{applicantId}/uploadoffer")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity uploadOfferForJob(@PathVariable("recruiterId") long recruiterId,
                                            @PathVariable("jobId") long jobId, @PathVariable("applicantId") long applicantId,
                                            @RequestBody OfferDetailsDTO offerDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            Long activityHiredResponse = activityService.uploadOffer(recruiterId, jobId, applicantId, offerDetails, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(activityHiredResponse)
                    .description(
                            messageSource.getMessage("job.offer.upload.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.offer.upload.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getHiredProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.offer.upload.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.offer.upload.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping("/offerdetails/{offerdetailsid}/updateoffer")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity uploadOfferForJob(@PathVariable("offerdetailsid") long offerdetailsid,
                                            @RequestBody OfferDetailsDTO offerDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            Long activityHiredResponse = activityService.updateOfferDetails(offerdetailsid, offerDetails, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(activityHiredResponse)
                    .description(
                            messageSource.getMessage("job.offer.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.offer.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getHiredProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.offer.update.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.offer.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

    /**
     * Method to get all rejected profiles for job
     *
     * @param recruiterId
     * @param jobId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/recruiter/{recruiterId}/jobs/{jobId}/status/{status}/filter/{filter}/rejected")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getRejectedProfilesForJob(@PathVariable("recruiterId") long recruiterId,
                                                    @PathVariable("jobId") long jobId, @PathVariable("status") int status, @PathVariable("filter") int filter,
                                                    @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                    @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ActivityRejectedResponse activityRejectedResponse = activityService.getRejectedProfilesForJob(userDetailsDTO.getConsultancyId(), recruiterId,
                    jobId, userDetailsDTO.getRoles().get(0), status, filter, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(activityRejectedResponse)
                    .description(messageSource.getMessage("get.all-rejected.profiles.job.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("get.all-rejected.profiles.job.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getRejectedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-rejected.profiles.job.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-rejected.profiles.job.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }
}
