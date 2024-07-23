/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.*;
import com.workruit.us.application.services.ConsultancyJobService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mahesh
 */
@Slf4j
@RestController
@RequestMapping("/consjobs")
public class ConsultancyJobController {

    private @Autowired ConsultancyJobService conJobService;
    private @Autowired MessageSource messageSource;

    /**
     * Default job matcher results - load jobs for uploaded users
     *
     * @param consultancyId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/{consId}/matchedjobs")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedJobsForConsultancy(@PathVariable("consId") long consultancyId,
                                                       @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                       @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsJobViewResponse jobViewResponse = conJobService.getMatchedJobsForConsultancy(consultancyId, userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNo,
                    pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("job.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/saved-matched-jobs/status/{status}/filter/{filter}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedJobsForConsultancy(@PathVariable("status") int status, @PathVariable("filter") int filter, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                       @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsJobViewResponse jobViewResponse = conJobService.getSavedMatchedJobsForConsultancy(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), status, filter, userDetailsDTO.getRoles().get(0), pageNo,
                    pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("get.saved.matched.job.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.saved.matched.job.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.saved.matched.job.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.saved.matched.job.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    /**
     * Get applicant info for the matched job
     *
     * @param consultancyId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/{consId}/matchedjobs/{jobPostId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getMatchedApplicantsForJobByConsultancy(@PathVariable("consId") long consultancyId,
                                                                  @PathVariable("jobPostId") long jobPostId, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                                  @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsJobApplicantResponse jobViewResponse = conJobService
                    .getMatchedApplicantsForJobByConsultancy(consultancyId, userDetailsDTO.getId(), jobPostId, userDetailsDTO.getRoles().get(0), pageNo, pageSize);
            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("job.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get matched jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    /**
     * Method for bulk apply with question and answers
     *
     * @param jobPostId
     * @param consBulkApply
     * @return
     */
    @PostMapping("/matchedjobs/{jobPostId}/bulkapply")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity bulkApplyApplicantsForJob(@PathVariable("jobPostId") long jobPostId,
                                                    @RequestBody List<ConsBulkApplyDTO> consBulkApply) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            conJobService.bulkApplyApplicantsForJob(userDetailsDTO.getConsultancyId(), jobPostId,
                    userDetailsDTO.getId(), consBulkApply);
            ApiResponse apiResponse = ApiResponse.builder().data("")
                    .description(messageSource.getMessage("job.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Bulk apply jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("job.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("job.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }


    @PostMapping("/matchedjobs/{jobPostId}/status/{status}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity updatedConsultancyJobStatus(@PathVariable("jobPostId") long jobPostId, @PathVariable("status") int status) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            conJobService.updatedConsultancyJobStatus(userDetailsDTO.getId(),
                    userDetailsDTO.getConsultancyId(), jobPostId, status);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data("")
                    .description(messageSource.getMessage("consultancy.job.status.updated.success.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.status.updated.success.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("updatedConsultancyJobStatus : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.job.status.updated.fail.description",
                            new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null)).title(messageSource
                            .getMessage("consultancy.job.status.updated.fail.title", new Object[]{}, null))
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/matchedjobs/filter")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity filterMatchedJobs(@RequestBody MatchedJobsFilter matchedJobsFilter,
                                            @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                            @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ConsJobApplicantResponse applicantViewResponse = conJobService.filterMatchedApplicants(matchedJobsFilter, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNo,
                    pageSize, false);
            ConsJobViewResponse jobViewResponse = null;
//            if (applicantViewResponse.getConsJobApplicantDTO().size() > 0) {
            jobViewResponse = conJobService.filterMatchedJobs(matchedJobsFilter, userDetailsDTO.getConsultancyId(),userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), applicantViewResponse.getConsJobApplicantDTO(), pageNo,
                    pageSize);
            // }

            ApiResponse apiResponse = ApiResponse.builder().data(jobViewResponse)
                    .description(messageSource.getMessage("consultancy.filter.matched.jobs.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.filter.matched.jobs.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get Filter matched jobs failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.filter.matched.jobs.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.filter.matched.jobs.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @PostMapping("/matchedApplicants/filter")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity filterMatchedApplicants(@RequestBody MatchedJobsFilter matchedJobsFilter,
                                                  @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                  @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ConsJobApplicantResponse applicantViewResponse = conJobService.filterMatchedApplicants(matchedJobsFilter, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNo,
                    pageSize, true);

            ApiResponse apiResponse = ApiResponse.builder().data(applicantViewResponse)
                    .description(messageSource.getMessage("consultancy.filter.matched.applicants.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.filter.matched.applicants.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get Filter matched applicants failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("consultancy.filter.matched.applicants.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("consultancy.filter.matched.applicants.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }
}
