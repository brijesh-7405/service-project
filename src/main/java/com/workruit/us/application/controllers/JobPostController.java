/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.enums.RecommendedState;
import com.workruit.us.application.exception.CreateJobFailedException;
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

import javax.validation.Valid;
import java.util.List;

/**
 * @author Mahesh
 */
@Slf4j
@RestController
public class JobPostController {

    private @Autowired JobPostService jobService;
    private @Autowired MessageSource messageSource;

    /**
     * @return
     */
    @GetMapping("/jobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllJobs() {
        List<JobPostDTO> jobs = jobService.findAllJobs();

        ApiResponse apiResponse = ApiResponse.builder().data(jobs)
                .description(messageSource.getMessage("get.all-jobs.success.description", new Object[]{}, null))
                .status(messageSource.getMessage("success.status", new Object[]{}, null))
                .title(messageSource.getMessage("get.all-jobs.success.title", new Object[]{}, null)).build();
        ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        return responseEntity;
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

    @GetMapping("/jobs/user/{userId}/jobCounts")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity jobCounts(@PathVariable("userId") long userId) {
        try {
            JobViewResponse jobViewResponse = jobService.getUserJobsCount(userId);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("job.count.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.count.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Job count failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.count.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs/user/{userId}/activejobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllActiveJobsForUser(@PathVariable("userId") long userId,
                                                  @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                  @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(userId, JobStatus.ACTIVE, 4, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.active.job.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.active.job.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get All Active jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.active.job.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/status/{status}/filterActivejobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllFilterActivejobssForUser(@PathVariable("status") int status,
                                                         @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                         @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            List<JobFilterDTO> jobViewResponse = jobService.findAllJobsbyUser(userDetailsDTO.getId(), userDetailsDTO.getConsultancyId(), status, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.active.job.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.active.job.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get All Active jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.active.job.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/user/{userId}/status/{status}/allPostedJobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllJobsForUser(@PathVariable("userId") long userId, @PathVariable("status") int status,
                                            @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                            @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            JobViewResponse jobViewResponse = null;
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            if (status == 1) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.PENDING, status, pageNo, pageSize);
            } else if (status == 2) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.ACTIVE, status, pageNo, pageSize);
            } else if (status == 3) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.CLOSED, status, pageNo, pageSize);

            }
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-jobs.for.user.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-jobs.for.user.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get all jobs for user failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-jobs.for.user.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/jobs/user/{userId}/status/{status}/jobStatus/{jobstatus}/postedJobsInfo")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getPostedJobsInfo(@PathVariable("userId") long userId, @PathVariable("status") int status, @PathVariable("jobstatus") int jobstatus,
                                            @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                            @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            JobViewResponse jobViewResponse = null;
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            if (jobstatus == 1) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.PENDING, status, pageNo, pageSize);
            } else if (jobstatus == 2) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.ACTIVE, status, pageNo, pageSize);
            } else if (jobstatus == 3) {
                jobViewResponse = jobService.getDashboardAllPostedJobsForUserIncludingTeam(userId,
                        userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0), JobStatus.CLOSED, status, pageNo, pageSize);
            }

            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-jobs.for.user.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-jobs.for.user.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get all jobs for user failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-jobs.for.user.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/user/{userId}/pendingjobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllPendingJobsForUser(@PathVariable("userId") long userId,
                                                   @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                   @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(userId, JobStatus.PENDING, 4, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.pending.all-jobs.for.user.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.pending.all-jobs.for.user.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get all-pending jobs for user failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.pending.all-jobs.for.user.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/user/{userId}/closedjobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllClosedJobsForUser(@PathVariable("userId") long userId,
                                                  @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                  @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(userId, JobStatus.CLOSED, 4, pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-closed.job.for.user.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-closed.job.for.user.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get Closed jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-closed.job.for.user.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/manager/{managerId}/activejobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllActiveJobsForManager(@PathVariable("managerId") long managerId,
                                                     @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                     @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            // TODO: Get all users under manager as well
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(managerId, JobStatus.ACTIVE, 4, pageNo,
                    pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-active.job.for.manager.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-active.job.for.manager.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get active jobs for manager : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-active.job.for.manager.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/manager/{managerId}/pendingjobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllPendingJobsForManager(@PathVariable("managerId") long managerId,
                                                      @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                      @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            // TODO: Get all users under manager as well
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(managerId, JobStatus.PENDING, 4, pageNo,
                    pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-pending.job.for.manager.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-pending.job.for.manager.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get pending jobs for manager failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-pending.job.for.manager.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/jobs/manager/{managerId}/closedjobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getAllClosedJobsForManager(@PathVariable("managerId") long managerId,
                                                     @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                     @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {

            // TODO: Get all users under manager as well
            JobViewResponse jobViewResponse = jobService.getAllJobsForUser(managerId, JobStatus.PENDING, 4, pageNo,
                    pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.all-closed.job.for.manager.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-closed.job.for.manager.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get closed jobs for manager failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-closed.job.for.manager.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/jobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity createJobPost(@RequestBody @Valid JobPostDTO jobPostDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            jobPostDto.setUserId(userDetailsDTO.getId());

            Long jobId = jobService.saveJobPost(jobPostDto, userDetailsDTO.getConsultancyId());
            ApiResponse apiResponse = ApiResponse.builder().data(jobId)
                    .description(messageSource.getMessage("job.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (CreateJobFailedException e) {
            log.error("save job failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.create.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
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

    @DeleteMapping("/jobs/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity deleteJobById(@PathVariable("jobId") long jobId) throws JobNotFoundException {
        Long deletedJobId = jobService.deleteJobById(jobId);

        ApiResponse apiResponse = ApiResponse.builder().data(deletedJobId)
                .description(messageSource.getMessage("job.delete.success.description", new Object[]{}, null))
                .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                .title(messageSource.getMessage("job.delete.success.title", new Object[]{}, null)).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/jobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity deleteAllJobs() {
        jobService.deleteAllJobs();
        ApiResponse apiResponse = ApiResponse.builder().data(-1)
                .description(messageSource.getMessage("jobs.delete.success.description", new Object[]{}, null))
                .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                .title(messageSource.getMessage("jobs.delete.success.title", new Object[]{}, null)).build();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/jobs/jobmatcher")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity runJobMatcher() {
        try {
            int batch = 100;
            // jobService.runJobMatcherForApplicant(905L, 100);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("jobs.matcher.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("jobs.matcher.success.title", new Object[]{}, null)).build();
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            log.error("run job matcher failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("jobs.matcher.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("jobs.matcher.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    // @UserAuthorized(userRoles = {Role.Roles.HR_MANAGER})
    @PostMapping("changeStatus/{jobId}/{status}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateJobStatus(@PathVariable("jobId") long jobId, @PathVariable("status") String status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(jobService.changeJobStatus(jobId, userDetailsDTO.getConsultancyId(), status))
                    .description(messageSource.getMessage("update.job.status.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("update.job.status.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("changeStatus : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("update.job.status.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("update.job.status.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/relevantApplicants/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getRelevantApplicants(@PathVariable("jobId") long jobId,
                                                @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            ApiResponse apiResponse = ApiResponse.builder().data(jobService.getProfileMatchedApplicants(jobId, RecommendedState.RELEVANT.getValue(), pageNo, pageSize))
                    .description(messageSource.getMessage("get.relevant.applicants.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.relevant.applicants.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get relevant applicants failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.relevant.applicants.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.relevant.applicants.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/suggestedApplicants/{jobId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getSuggestedApplicants(@PathVariable("jobId") long jobId,
                                                 @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                 @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            ApiResponse apiResponse = ApiResponse.builder().data(jobService.getProfileMatchedApplicants(jobId, RecommendedState.SUGGESTED.getValue(), pageNo, pageSize))
                    .description(messageSource.getMessage("get.suggested.applicants.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.suggested.applicants.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get suggested applicants failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.suggested.applicants.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.suggested.applicants.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
