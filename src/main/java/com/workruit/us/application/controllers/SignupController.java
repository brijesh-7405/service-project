/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.ApplicantSignupDTO;
import com.workruit.us.application.dto.RoleUpdateDTO;
import com.workruit.us.application.dto.SignupDTO;
import com.workruit.us.application.models.User;
import com.workruit.us.application.services.SignupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController
public class SignupController {

    private @Autowired SignupService signupService;
    private @Autowired MessageSource messageSource;

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Valid SignupDTO signupDTO) {
        try {
            Long userId = signupService.saveUser(signupDTO);

            ApiResponse apiResponse = ApiResponse.builder().data(userId)
                    .description(messageSource.getMessage("signup.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            return responseEntity;
        } catch (AuthenticationException ae) {
            log.error("Error while saving the user", ae);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{ae.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @ResponseBody
    @PostMapping("/updateUserRole")
    public ResponseEntity updateRole(@RequestBody @Valid RoleUpdateDTO roleUpdateDTO) {
        try {
            Long roleId = signupService.updateUserRole(roleUpdateDTO);
            ApiResponse apiResponse = ApiResponse.builder().data(roleId)
                    .description(messageSource.getMessage("signup.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            return responseEntity;
        } catch (AuthenticationException ae) {
            log.error("Error while saving the user", ae);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{ae.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PostMapping("/applicant/signup")
    public ResponseEntity applicantSignup(@RequestBody @Valid ApplicantSignupDTO signupDTO) {
        try {
            Long userId = signupService.saveApplicant(signupDTO);
            ApiResponse apiResponse = ApiResponse.builder().data(userId)
                    .description(messageSource.getMessage("applicant.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            return responseEntity;
        } catch (AuthenticationException ae) {
            log.error("Error while saving the user", ae);

            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.create.fail.description", new Object[]{ae.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("applicant.create.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("applicant.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            return responseEntity;
        }
    }

    @ResponseBody
    @GetMapping("/signup/applicant/{userId}/{code}")
    public ResponseEntity verifyApplicant(@PathVariable("userId") Long userId, @PathVariable("code") String code) {
        User output = signupService.verifyUser(userId, code);
        if (output != null) {
            ApiResponse apiResponse = ApiResponse.builder().data(userId)
                    .description(messageSource.getMessage("email.verify.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("email.verify.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } else {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("email.verify.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @ResponseBody
    @GetMapping("/signup/user/{userId}/{code}")
    public ResponseEntity verifyUser(@PathVariable("userId") Long userId, @PathVariable("code") String code) {
        User output = signupService.verifyUser(userId, code);
        if (output != null) {
            ApiResponse apiResponse = ApiResponse.builder().data(userId)
                    .description(messageSource.getMessage("email.verify.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("email.verify.success.title", new Object[]{}, null)).build();

            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("email.verify.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PostMapping("/signup/sendOtp")
    public ResponseEntity sendOtp(@RequestParam("mobile") String mobileNumber) {

        try {
            signupService.sendOtp(mobileNumber);
            ApiResponse apiResponse = ApiResponse.builder().data(mobileNumber)
                    .description(messageSource.getMessage("send.otp.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("send.otp.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (AuthenticationException ae) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("send.otp.fail.description", new Object[]{ae.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("send.otp.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("send.otp.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("send.otp.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
