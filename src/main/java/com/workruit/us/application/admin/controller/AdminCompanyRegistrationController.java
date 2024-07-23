package com.workruit.us.application.admin.controller;


import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantStatus;
import com.workruit.us.application.admin.enums.TimePeriod;
import com.workruit.us.application.admin.service.AdminCompanyRegistrationService;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.CompanyDetailsDTO;
import com.workruit.us.application.services.CompanyService;
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
public class AdminCompanyRegistrationController {

    private @Autowired MessageSource messageSource;

    private @Autowired AdminCompanyRegistrationService adminRegistrationService;

    private @Autowired CompanyService companyService;

    @PostMapping("/incompleteCompanyRegistration")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getIncompleteRegistration(@RequestParam(name = "companyName", required = false) String name,
                                                    @RequestParam(name = "period", required = false) TimePeriod period,
                                                    @RequestParam(name = "from", required = false) Date from,
                                                    @RequestParam(name = "to", required = false) Date to,
                                                    @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                    @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                    @RequestBody(required = false) IncompleteRegistrationFilter incompleteRegistrationFilter) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getIncompleteCompanyRegistration(incompleteRegistrationFilter, name, from, to, pageNumber, pageSize, period))
                    .description(messageSource.getMessage("admin.get.incomplete.company.registration.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.incomplete.company.registration.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getIncompleteRegistration : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.incomplete.company.registration.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.incomplete.company.registration.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/completeCompanyRegistration")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompleteRegistration(@RequestParam(name = "companyName", required = false) String name,
                                                  @RequestParam(name = "period", required = false) TimePeriod period,
                                                  @RequestParam(name = "from", required = false) Date from,
                                                  @RequestParam(name = "to", required = false) Date to,
                                                  @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                  @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                  @RequestBody(required = false) CompleteRegistrationRequestDTO completeRegistrationRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompleteCompanyRegistration(completeRegistrationRequestDTO.getCompleteRegistrationFilter(), name, from, to, pageNumber, pageSize, completeRegistrationRequestDTO.getSortByDTO(), period))
                    .description(messageSource.getMessage("admin.get.complete.company.registration.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.complete.company.registration.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getCompleteRegistration : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.complete.company.registration.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.complete.company.registration.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/getCompanyProfile/{companyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyProfile(@PathVariable("companyId") long companyId) {
        try {
            CompanyDetailsDTO companyDetailsDTO = companyService.getCompany(companyId);
            ApiResponse apiResponse = ApiResponse.builder().data(companyDetailsDTO)
                    .description(
                            messageSource.getMessage("company.get-profile.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.get-profile.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get company profile", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("company.get-profile.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.get-profile.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/getCompanyHrManager/{userId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyHrManager(@PathVariable("userId") long userId, @RequestParam(name = "name", required = false) String name,
                                              @RequestParam(name = "period", required = false) TimePeriod period,
                                              @RequestParam(name = "from", required = false) Date from,
                                              @RequestParam(name = "to", required = false) Date to,
                                              @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                              @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompanyHrManager(userId, name, from, to, pageNumber, pageSize, period))
                    .description(messageSource.getMessage("admin.get.company.hr.managers.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.managers.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getCompanyHrManager : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.company.hr.managers.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.managers.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getMainCompanyHR/{companyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getMainCompanyHR(@PathVariable("companyId") long companyId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompanyMainHr(companyId))
                    .description(
                            messageSource.getMessage("admin.get.company.hr.main.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.main.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get main HR", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.hr.main.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.main.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/getHrManagerDetails/{userId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getHrManagerDetails(@PathVariable("userId") long userId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getHrManagerDetails(userId))
                    .description(
                            messageSource.getMessage("admin.get.company.hr.manager.details.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.manager.details.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get HR Manager Details", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.hr.manager.details.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.hr.manager.details.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getCompanyJobs/{companyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyJobs(@PathVariable("companyId") long companyId, @RequestParam(name = "title", required = false) String title,
                                         @RequestParam(name = "period", required = false) TimePeriod period,
                                         @RequestParam(name = "from", required = false) Date from,
                                         @RequestParam(name = "to", required = false) Date to,
                                         @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                         @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                         @RequestBody(required = false) CompanyJobsRequestDTO companyJobsRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompanyJobs(companyId, companyJobsRequestDTO.getCompanyJobFilter(), title, from, to, pageNumber, pageSize, companyJobsRequestDTO.getSortBy(), period))
                    .description(
                            messageSource.getMessage("admin.get.company.jobs.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.jobs.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get company jobs", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.jobs.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.jobs.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PutMapping("/getJobApplicants/{jobPostId}/{applicantStatus}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobApplicants(@PathVariable("jobPostId") long jobPostId,
                                           @PathVariable("applicantStatus") ApplicantStatus applicantStatus,
                                           @RequestParam(name = "name", required = false) String name,
                                           @RequestParam(name = "period", required = false) TimePeriod period,
                                           @RequestParam(name = "from", required = false) Date from,
                                           @RequestParam(name = "to", required = false) Date to,
                                           @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                           @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                           @RequestBody(required = false) JobApplicantRequestDTO jobApplicantRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getApplicantData(jobPostId, jobApplicantRequestDTO.getJobApplicantFilter(), name, applicantStatus, from, to, pageNumber, pageSize, jobApplicantRequestDTO.getSortByDTOS(), period))
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

    @PutMapping("/companyAlerts/{companyId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyAlertData(@PathVariable(name = "companyId") Long companyId,
                                              @RequestBody(required = false) List<Long> userIds,
                                              @RequestParam(name = "period", required = false) TimePeriod period,
                                              @RequestParam(name = "from", required = false) Date from,
                                              @RequestParam(name = "to", required = false) Date to,
                                              @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                              @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompanyAlertData(companyId, userIds, pageNumber, pageSize, from, to, period))
                    .description(messageSource.getMessage("admin.get.company.alert.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.alert.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getCompanyAlertData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.company.alert.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.alert.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/getCompanyJobDetails/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyJobDetails(@PathVariable("jobId") long jobId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getCompanyJobDetails(jobId))
                    .description(
                            messageSource.getMessage("admin.get.company.job.details.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.details.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get company job details", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.job.details.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.details.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PutMapping("/applicantDataForJobPost/{jobId}/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getApplicantDataForJobPost(@PathVariable("jobId") long jobId,
                                                     @PathVariable("applicantId") long applicantId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getApplicantDataForJobPost(applicantId, jobId))
                    .description(
                            messageSource.getMessage("admin.get.job.applicant.data.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.applicant.data.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get applicant data for job ", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.job.applicant.data.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.applicant.data.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @PutMapping("/getCompanyJob/relevantApplicants/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getRelevantProfiles(@PathVariable("jobId") long jobId,
                                              @RequestParam(name = "name", required = false) String name,
                                              @RequestParam(name = "period", required = false) TimePeriod period,
                                              @RequestParam(name = "from", required = false) Date from,
                                              @RequestParam(name = "to", required = false) Date to,
                                              @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                              @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                              @RequestBody(required = false) ApplicantProfileRequestDTO applicantProfileRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getRelevantProfiles(jobId, applicantProfileRequestDTO.getApplicantProfileFilterDTO(), name, from, to, pageNumber, pageSize, applicantProfileRequestDTO.getSortByDTO(), period))
                    .description(
                            messageSource.getMessage("admin.get.company.job.relevant.applicant.profiles.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.relevant.applicant.profiles.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get company job get relevant profiles", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.job.relevant.applicant.profiles.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.relevant.applicant.profiles.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getCompanyJob/interestedApplicants/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getInterestedProfiles(@PathVariable("jobId") long jobId,
                                                @RequestParam(name = "name", required = false) String name,
                                                @RequestParam(name = "period", required = false) TimePeriod period,
                                                @RequestParam(name = "from", required = false) Date from,
                                                @RequestParam(name = "to", required = false) Date to,
                                                @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                                @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                                @RequestBody(required = false) ApplicantProfileRequestDTO applicantProfileRequestDTO) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getInterestedProfiles(jobId, applicantProfileRequestDTO.getApplicantProfileFilterDTO(), name, from, to, pageNumber, pageSize, applicantProfileRequestDTO.getSortByDTO(), period))
                    .description(
                            messageSource.getMessage("admin.get.company.job.interested.applicant.profiles.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.interested.applicant.profiles.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get company job get interested profiles", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.company.job.interested.applicant.profiles.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.job.interested.applicant.profiles.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @PutMapping("/getJobHistoryApplicants/{jobPostId}/{applicantId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobApplicantsHistory(@PathVariable("jobPostId") long jobPostId, @PathVariable("applicantId") long applicantId) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminRegistrationService.getApplicantDataWithStatus(jobPostId, applicantId))
                    .description(
                            messageSource.getMessage("admin.get.job.history.applicant.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.history.applicant.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while get job applicants", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{e.getMessage()}, null))
                    .description(messageSource.getMessage("admin.get.job.history.applicant.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.job.history.applicant.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

}
