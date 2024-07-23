/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.repositories.UserRepository;
import com.workruit.us.application.services.AlertService;
import io.swagger.annotations.ApiImplicitParam;
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
@RestController
public class AlertsController {

    private @Autowired AlertService alertService;
    private @Autowired MessageSource messageSource;
    private @Autowired UserRepository userRepository;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @GetMapping("alerts")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity alerts(@RequestParam("page") int page, @RequestParam("size") int size,
                                 @RequestParam(value = "userId", required = false) Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity(alertService.alerts(userDetailsDTO.getConsultancyId(), page, size, userId),
                    HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("user.change-password.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.change-password.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }
}
