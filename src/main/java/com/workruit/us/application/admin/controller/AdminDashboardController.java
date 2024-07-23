package com.workruit.us.application.admin.controller;

import com.workruit.us.application.admin.dto.JobStatsFilterDTO;
import com.workruit.us.application.admin.dto.OverallActivityFilterDTO;
import com.workruit.us.application.admin.dto.PendingJobFilterDTO;
import com.workruit.us.application.admin.service.AdminDashboardService;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.JobPostDTO;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.exception.JobNotFoundException;
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

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminDashboardController {

    private @Autowired MessageSource messageSource;

    private @Autowired AdminDashboardService adminDashboardService;

    private @Autowired JobPostService jobService;

    @GetMapping("/dashboard/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getDashboardStats() {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getDashBoardStats())
                    .description(messageSource.getMessage("get.admin.dashboard.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.admin.dashboard.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetAdminDashboradStats : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.admin.dashboard.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.admin.dashboard.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/company-registration/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyRegistrationDataByMonthly(@RequestParam(name = "period", required = false) String period,
                                                              @RequestParam(name = "from", required = false) Date from,
                                                              @RequestParam(name = "to", required = false) Date to) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getCompanyRegistrationDataByMonthly(period, from, to))
                    .description(messageSource.getMessage("admin.get.company.registration.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.registration.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetCompanyRegistrationDataByMonthly : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.company.registration.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.registration.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/company-registration/overall-stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyRegistrationData() {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getCompanyRegistrationData())
                    .description(messageSource.getMessage("admin.get.company.registration.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.registration.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetCompanyRegistrationData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.company.registration.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.company.registration.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employer-registration/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getEmployerRegistrationDataByMonthly(@RequestParam(name = "period", required = false) String period,
                                                               @RequestParam(name = "from", required = false) Date from,
                                                               @RequestParam(name = "to", required = false) Date to) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getEmployerRegistrationDataByMonthly(period, from, to))
                    .description(messageSource.getMessage("admin.get.employer.registration.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.employer.registration.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetEmployerRegistrationDataByMonthly : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.employer.registration.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.employer.registration.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/employer-registration/overall-stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getEmployerRegistrationData() {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getEmployerRegistrationData())
                    .description(messageSource.getMessage("admin.get.employer.registration.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.employer.registration.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetEmployerRegistrationData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.employer.registration.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.employer.registration.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/overall-stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobData() {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getJobData())
                    .description(messageSource.getMessage("admin.get.jobs.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.jobs.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetJobData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.jobs.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.jobs.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/jobs/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getJobDataByMonthly(@RequestBody(required = false) JobStatsFilterDTO jobStatsFilterDTO,
                                              @RequestParam(name = "period", required = false) String period,
                                              @RequestParam(name = "from", required = false) Date from,
                                              @RequestParam(name = "to", required = false) Date to) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getJobDataByMonthlyAndFilter(jobStatsFilterDTO, period, from, to))
                    .description(messageSource.getMessage("admin.get.jobs.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.jobs.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetJobDataByMonthly : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.jobs.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.jobs.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/activity/overall-stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getActivityStatsData() {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getActivityStatsData())
                    .description(messageSource.getMessage("admin.get.activity.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.activity.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetActivityStatsData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.activity.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.activity.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/activity/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getActivityStatsDataByMonthlyAndFilter(@RequestBody(required = false) JobStatsFilterDTO jobStatsFilterDTO,
                                                                 @RequestParam(name = "period", required = false) String period,
                                                                 @RequestParam(name = "from", required = false) Date from,
                                                                 @RequestParam(name = "to", required = false) Date to) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getActivityStatsDataByMonthlyAndFilter(jobStatsFilterDTO, period, from, to))
                    .description(messageSource.getMessage("admin.get.activity.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.activity.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("GetActivityStatsData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.activity.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.activity.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/pendingJobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getPendingJobsData(@RequestBody(required = false) PendingJobFilterDTO pendingJobFilterDTO,
                                             @RequestParam(name = "searchByTitle", required = false) String jobTitle,
                                             @RequestParam(name = "from", required = false) Date from,
                                             @RequestParam(name = "to", required = false) Date to) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getPendingJobsData(pendingJobFilterDTO, jobTitle, from, to))
                    .description(messageSource.getMessage("admin.get.pending.jobs.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.pending.jobs.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getPendingJobsData : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.pending.jobs.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.pending.jobs.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/overallActivity")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getOverallActivity(@RequestBody(required = false) OverallActivityFilterDTO overallActivityFilterDTO,
                                             @RequestParam(name = "searchByName", required = false) String name,
                                             @RequestParam(name = "from", required = false) Date from,
                                             @RequestParam(name = "to", required = false) Date to,
                                             @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                             @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getOverallActivityData(overallActivityFilterDTO, name, from, to, pageNumber, pageSize))
                    .description(messageSource.getMessage("admin.get.overall.activity.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.overall.activity.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getOverallActivity : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.overall.activity.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.overall.activity.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/overallAlerts")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getOverallAlerts(@RequestParam(name = "companyName", required = false) String name,
                                           @RequestParam(name = "from", required = false) Date from,
                                           @RequestParam(name = "to", required = false) Date to,
                                           @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                           @RequestParam(defaultValue = "10", name = "size") int pageSize) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getOverallAlertData(name, from, to, pageNumber, pageSize))
                    .description(messageSource.getMessage("admin.get.overall.alert.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.overall.alert.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getOverallAlerts : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("admin.get.overall.alert.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("admin.get.overall.alert.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/jobs/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updateJobPost(@PathVariable("jobId") long jobId, @RequestBody JobPostDTO jobPostDTO) {
        Long updateJobId = null;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            jobPostDTO.setUserId(userDetailsDTO.getId());
            String userName = userDetailsDTO.getName();
            updateJobId = jobService.updateJobPost(jobId, jobPostDTO, userName, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(updateJobId)
                    .description(messageSource.getMessage("job.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.update.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (JobNotFoundException e) {
            log.error("update job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("update job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("error.unknown", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.update.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getJobById(@PathVariable("jobId") long jobId) {
        try {
            JobPostDTO jobPostDTO = jobService.findJobPostById(jobId);
            ApiResponse apiResponse = ApiResponse.builder().data(jobPostDTO)
                    .description(messageSource.getMessage("job.get.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.get.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (JobNotFoundException e) {
            log.error("Job not found : " + e.getMessage());
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.get.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/searchCompany")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyName(@RequestParam(name = "companyName", required = false) String name) {
        try {
            ApiResponse apiResponse = ApiResponse.builder().data(adminDashboardService.getCompanyNameOnSearch(name))
                    .description(
                            messageSource.getMessage("company.get.name.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.get.name.success.title", new Object[]{}, null))
                    .build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while searching for company name", e);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(-1)
                    .description(messageSource.getMessage("company.get.name.fail.description",
                            new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.get.name.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}
