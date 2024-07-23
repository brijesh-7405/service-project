/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.ApplicantService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController
public class UserImportController {

    private @Autowired ApplicantService applicantService;
    private @Autowired MessageSource messageSource;

    //	@UserAuthorized(userRoles = { Roles.HR_MANAGER})
    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/applicants/update-csv")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity uploadCSV(@RequestParam("file") @RequestPart String file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().title(messageSource.getMessage("success.status", new Object[]{}, null))
                    .description(messageSource.getMessage("success.status", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .data(applicantService.createUploadJob(file, userDetailsDTO.getId(), userDetailsDTO.getConsultancyId())).build();
            //.data(applicantService.createUploadJob("OTrp8ZH5FZ5YfOWSxAaxShd5dY7yUhnm", 957, 519)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Error while importing users", e);
            return ControllerUtils.genericErrorMessage();
        }
    }

    //@UserAuthorized(userRoles = { Roles.HR_MANAGER })
    @SuppressWarnings("rawtypes")
    @GetMapping(value = "/applicants/upload-status")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity uploadStatus(@RequestParam("jobId") Long jobId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse =
                    ApiResponse.builder().title(messageSource.getMessage("success.status", new Object[]{}, null))
                            .description(messageSource.getMessage("success.status", new Object[]{}, null))
                            .status(messageSource.getMessage("success.status", new Object[]{}, null))
                            .data(applicantService.getJobStatus(jobId, userDetailsDTO.getId())).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            log.error("Error while importing users", e);
            return ControllerUtils.genericErrorMessage();
        }

    }
}
