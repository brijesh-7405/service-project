/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.CompanyDetailsDTO;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.CompanyService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController
public class CompanyController {

    private @Autowired CompanyService companyService;
    private @Autowired MessageSource messageSource;

    @SuppressWarnings("rawtypes")
    @PutMapping("/company")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateCompanyProfile(@RequestBody CompanyDetailsDTO companyDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            companyService.updateCompany(companyDTO, userDetailsDTO.getCompanyId(), userDetailsDTO.getConsultancyId(), userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(userDetailsDTO.getCompanyId())
                    .description(messageSource.getMessage("company.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.create.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("company.create.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("company.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @GetMapping("/company")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCompanyProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            CompanyDetailsDTO companyDetailsDTO = companyService.getCompany(userDetailsDTO.getCompanyId());
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
}
