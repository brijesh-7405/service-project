/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.DashboardService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController
public class DashboardController {

    private @Autowired DashboardService dashboardService;
    private @Autowired MessageSource messageSource;


    @SuppressWarnings({"unchecked", "rawtypes"})
    @GetMapping("/dashboard")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity dashboard(@RequestParam("page") int page, @RequestParam("size") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity(dashboardService.dashboard(userDetailsDTO.getConsultancyId(), page, size),
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception while getting dashboard", e);
            return ControllerUtils.genericErrorMessage();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @GetMapping("/dashboardStats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity dashboardStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity(
                    dashboardService.dashboardStats(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0)),
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("Exception while getting dashboard", e);
            return ControllerUtils.genericErrorMessage();
        }
    }

    @GetMapping("/dashboard/consultancy/stats")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity dashboardConsultancy() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(dashboardService.dashboardConsStats(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0)))
                    .description(
                            messageSource.getMessage("dashboard.consultancy.stats.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("dashboard.consultancy.stats.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("dashboardConsultancy: ", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("dashboard.consultancy.stats.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("dashboard.consultancy.stats.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
