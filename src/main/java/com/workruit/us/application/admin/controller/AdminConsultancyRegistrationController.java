package com.workruit.us.application.admin.controller;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantJobs;
import com.workruit.us.application.admin.enums.ApplicantProfileStatus;
import com.workruit.us.application.admin.enums.TimePeriod;
import com.workruit.us.application.admin.service.AdminConsultancyRegistrationService;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.ConsultancyDetailsDTO;
import com.workruit.us.application.services.ConsultancyService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminConsultancyRegistrationController {

    private @Autowired MessageSource messageSource;
    private @Autowired AdminConsultancyRegistrationService adminConsultancyRegistrationService;
    private @Autowired ConsultancyService consultancyService;

    @PostMapping("/incompleteConsultancyRegistration")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getIncompleteConsultancyRegistration(@RequestParam(name = "consultancyName", required = false) String name,
                                                               @RequestParam(name = "period", required = false) TimePeriod period,
                                                               @RequestParam(name = "from", required = false) Date from,
                                                               @RequestParam(name = "to", required = false) Date to,
                                                               @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                               @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                               @RequestBody(required = false) IncompleteRegistrationFilter incompleteRegistrationFilter) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getIncompleteConsultancyRegistration(incompleteRegistrationFilter, name, from, to, pageNumber, pageSize, period))
                    .description(messageSource.getMessage("admin.get.incomplete.consultancy.registration.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.incomplete.consultancy.registration.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getIncompleteRegistration : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.incomplete.consultancy.registration.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.incomplete.consultancy.registration.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/completeConsultancyRegistration")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompleteRegistration(@RequestParam(name = "consultancyName", required = false) String name,
                                                  @RequestParam(name = "period", required = false) TimePeriod period,
                                                  @RequestParam(name = "from", required = false) Date from,
                                                  @RequestParam(name = "to", required = false) Date to,
                                                  @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                  @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                  @RequestBody(required = false) CompleteRegistrationRequestDTO completeRegistrationRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getCompleteConsultancyRegistration(completeRegistrationRequestDTO.getCompleteRegistrationFilter(), name, from, to, pageNumber, pageSize, completeRegistrationRequestDTO.getSortByDTO(), period))
                    .description(messageSource.getMessage("admin.get.complete.consultancy.registration.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.complete.consultancy.registration.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getCompleteRegistration : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.complete.consultancy.registration.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.complete.consultancy.registration.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getConsultancyProfile/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getConsultancyProfile(@PathVariable("consultancyId") long consultancyId) {
        try {
            ConsultancyDetailsDTO consultancyDetails = consultancyService.getConsultancyDetails(consultancyId, consultancyId);
            ApiResponse apiResponse = ApiResponse.builder().data(consultancyDetails)
                    .description(
                            messageSource.getMessage("consultancy.get-profile.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.get-profile.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get consultancy profile", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("consultancy.get-profile.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.get-profile.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/getConsultancyConsManager/{userId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getConsultancyConsManager(@PathVariable("userId") long userId, @RequestParam(name = "name", required = false) String name,
                                                    @RequestParam(name = "period", required = false) TimePeriod period,
                                                    @RequestParam(name = "from", required = false) Date from,
                                                    @RequestParam(name = "to", required = false) Date to,
                                                    @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                    @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getConsultancyConsManager(userId, name, from, to, pageNumber, pageSize, period))
                    .description(messageSource.getMessage("admin.get.consultancy.cons.managers.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.cons.managers.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getConsultancyConsManager : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.consultancy.cons.managers.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.cons.managers.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getMainConsultant/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getMainConsultant(@PathVariable("consultancyId") long consultancyId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getConsultancyMainCons(consultancyId))
                    .description(
                            messageSource.getMessage("admin.get.main.consultant.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.main.consultant.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get main consultant ", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.main.consultant.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.main.consultant.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/getConsultantManagerDetails/{userId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getConsultantManagerDetails(@PathVariable("userId") long userId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getConsultantManagerDetails(userId))
                    .description(
                            messageSource.getMessage("admin.get.consultancy.cons.manager.details.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.cons.manager.details.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get Consultant Manager Details", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.consultancy.cons.manager.details.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.cons.manager.details.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/getApplicants/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantsOfCons(@PathVariable("consultancyId") long consultancyId,
                                              @RequestParam(name = "name", required = false) String name,
                                              @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                              @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getApplicantsOfCons(consultancyId, name, pageNumber, pageSize))
                    .description(
                            messageSource.getMessage("admin.get.consultancy.user.uploaded.applicants.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.user.uploaded.applicants.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get Applicants count by Cons user: ", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.consultancy.user.uploaded.applicants.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.user.uploaded.applicants.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/uploadedApplicants/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getUploadedApplicants(@PathVariable("consultancyId") Long consultancyId,
                                                @RequestParam(name = "userId", required = false) Long userId,
                                                @RequestParam(name = "name", required = false) String name,
                                                @RequestParam(name = "period", required = false) TimePeriod period,
                                                @RequestParam(name = "from", required = false) Date from,
                                                @RequestParam(name = "to", required = false) Date to,
                                                @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                @RequestBody(required = false) UploadedApplicantRequestDTO uploadedApplicantRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getUploadedApplicants(consultancyId, name, from, to, pageNumber, pageSize, uploadedApplicantRequestDTO.getSortByDTO(), period, uploadedApplicantRequestDTO.getUploadedApplicantFilterDTO(), userId))
                    .description(
                            messageSource.getMessage("admin.get.consultancy.uploaded.applicants.details.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.uploaded.applicants.details.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get uploaded applicants Details", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.consultancy.uploaded.applicants.details.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.uploaded.applicants.details.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getAppliedJobs/{consultancyId}/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getAppliedJobs(@PathVariable("consultancyId") Long consultancyId,
                                         @PathVariable(name = "applicantId") Long applicantId,
                                         @RequestParam(name = "title", required = false) String title,
                                         @RequestParam(name = "period", required = false) TimePeriod period,
                                         @RequestParam(name = "from", required = false) Date from,
                                         @RequestParam(name = "to", required = false) Date to,
                                         @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                         @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                         @RequestBody(required = false) JobRequestDTO jobRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getApplicantJobs(consultancyId, applicantId, title, from, to, pageNumber, pageSize, jobRequestDTO.getSortByDTO(), period, jobRequestDTO.getJobFilterDTO(), ApplicantJobs.APPLIED_JOBS))
                    .description(
                            messageSource.getMessage("admin.get.applicant.applied.jobs.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.applicant.applied.jobs.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get applicant applied Jobs", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.applicant.applied.jobs.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.applicant.applied.jobs.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getRelevantJobs/{consultancyId}/applicant/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getRelevantJobs(@PathVariable("consultancyId") Long consultancyId,
                                          @PathVariable(name = "applicantId") Long applicantId,
                                          @RequestParam(name = "title", required = false) String title,
                                          @RequestParam(name = "period", required = false) TimePeriod period,
                                          @RequestParam(name = "from", required = false) Date from,
                                          @RequestParam(name = "to", required = false) Date to,
                                          @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                          @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                          @RequestBody(required = false) JobRequestDTO jobRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getApplicantJobs(consultancyId, applicantId, title, from, to, pageNumber, pageSize, jobRequestDTO.getSortByDTO(), period, jobRequestDTO.getJobFilterDTO(), ApplicantJobs.RELEVANT_JOBS))
                    .description(
                            messageSource.getMessage("admin.get.applicant.relevant.jobs.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.applicant.relevant.jobs.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get applicant relevant Jobs", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.applicant.relevant.jobs.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.applicant.relevant.jobs.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getJobsActivity/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobsActivity(@PathVariable("consultancyId") Long consultancyId,
                                          @RequestParam(name = "title", required = false) String title,
                                          @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                          @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                          @RequestBody(required = false) ConsActivityFilterDTO consActivityFilterDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getJobsActivity(consultancyId, title, pageNumber, pageSize, consActivityFilterDTO))
                    .description(
                            messageSource.getMessage("admin.get.consultancy.activity.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.activity.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get jobs activity ", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.consultancy.activity.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.activity.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getJobApplicants/{consultancyId}/{jobPostId}/{applicantStatus}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobApplicants(@PathVariable("jobPostId") long jobPostId,
                                           @PathVariable("consultancyId") long consultancyId,
                                           @PathVariable("applicantStatus") ApplicantProfileStatus applicantStatus,
                                           @RequestParam(name = "name", required = false) String name,
                                           @RequestParam(name = "period", required = false) TimePeriod period,
                                           @RequestParam(name = "from", required = false) Date from,
                                           @RequestParam(name = "to", required = false) Date to,
                                           @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                           @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                           @RequestBody(required = false) JobApplicantRequestDTO jobApplicantRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getApplicantData(consultancyId, jobPostId, jobApplicantRequestDTO.getJobApplicantFilter(), name, applicantStatus, from, to, pageNumber, pageSize, jobApplicantRequestDTO.getSortByDTOS(), period))
                    .description(
                            messageSource.getMessage("admin.get.job.applicants.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.applicants.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get job applicants", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.job.applicants.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.applicants.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/consultancyAlerts/{consultancyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyAlertData(@PathVariable(name = "consultancyId") Long consultancyId,
                                              @RequestBody(required = false) List<Long> userId,
                                              @RequestParam(name = "period", required = false) TimePeriod period,
                                              @RequestParam(name = "from", required = false) Date from,
                                              @RequestParam(name = "to", required = false) Date to,
                                              @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                              @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminConsultancyRegistrationService.getConsultancyAlertData(consultancyId, userId, pageNumber, pageSize, from, to, period))
                    .description(messageSource.getMessage("admin.get.consultancy.alert.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.alert.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getConsultancyAlertData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.consultancy.alert.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.consultancy.alert.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
