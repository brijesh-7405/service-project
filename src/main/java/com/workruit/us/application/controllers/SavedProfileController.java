/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.SavedProfilesResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.service.SaveProfileService;
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
 * @author Mahesh
 */
@Slf4j
@RestController
@RequestMapping("/saved")
public class SavedProfileController {

    private @Autowired SaveProfileService saveProfileService;
    private @Autowired MessageSource messageSource;

    @GetMapping("/recruiter/{recruiterId}/status/{status}/filter/{filter}/savedprofiles")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    public ResponseEntity getSavedProfilesForJob(@PathVariable("recruiterId") long recruiterId, @PathVariable("status") int status, @PathVariable("filter") int filter, @RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                                 @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            SavedProfilesResponse savedProfilesResponse = saveProfileService.getSavedProfilesForJob(recruiterId, userDetailsDTO.getCompanyId(), status, filter, pageNo,
                    pageSize, userDetailsDTO.getConsultancyId(), userDetailsDTO.getRoles().get(0));
            ApiResponse apiResponse = ApiResponse.builder().data(savedProfilesResponse)
                    .description(messageSource.getMessage("activity.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("getSavedProfilesForJob failed : ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("activity.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("activity.create.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }

    }

}
