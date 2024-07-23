/**
 *
 */
package com.workruit.us.application.controllers;

import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.services.UserService;
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

/**
 * @author Santosh Bhima
 */
@Slf4j
@RestController("/user")
public class UserController {

    private @Autowired UserService userService;
    private @Autowired MessageSource messageSource;

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PostMapping
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity createUser(@RequestBody @Valid UserDTO userDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            Long userId = userService.createUser(userDTO, userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0));
            ApiResponse apiResponse = ApiResponse.builder().data(userId)
                    .description(messageSource.getMessage("signup.create.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("signup.create.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("signup.create.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PutMapping
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity updateUser(@RequestBody @Valid UserDTO userDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            UserDTO user = userService.updateUser(userDTO, userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data(user)
                    .description(messageSource.getMessage("user.update.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.update.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;

        } catch (Exception e) {
            log.error("Error while updating the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("user.update.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.update.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PutMapping("/reInvite/{verificationId}")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity reInvite(@RequestBody @Valid UserDTO userDTO,
                                   @PathVariable("verificationId") Long verificationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            userService.resendInvite(verificationId, userDTO, userDetailsDTO.getRoles().get(0));

            ApiResponse apiResponse = ApiResponse.builder().data("")
                    .description(messageSource.getMessage("user.re-invite.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.re-invite.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("user.re-invite.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.re-invite.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @ResponseBody
    @GetMapping("/getUserInfo")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(userService.getUserInfo(userDetailsDTO.getId()))
                    .description(messageSource.getMessage("get.pending.users.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.pending.users.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.pending.users.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.pending.users.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }


    @SuppressWarnings("rawtypes")
    @ResponseBody
    @GetMapping("/pending")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getPendingUsers(@RequestParam(defaultValue = "0", name = "page") Integer pageNo,
                                          @RequestParam(defaultValue = "10", name = "size") Integer pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

            ApiResponse apiResponse = ApiResponse.builder().data(userService.getPendingUsers(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNo, pageSize))
                    .description(messageSource.getMessage("get.pending.users.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.pending.users.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.pending.users.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.pending.users.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @GetMapping("/list")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getUsers(@RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(userService.getUsers(userDetailsDTO.getConsultancyId(),
                            userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNumber, pageSize))
                    .description(messageSource.getMessage("get.all-users.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-users.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while getting the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-users.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-users.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @ResponseBody
    @GetMapping("/collabList")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getCollabratorsUsers(@RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(userService.getCollabratorUsers(userDetailsDTO.getConsultancyId(),
                            userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNumber, pageSize))
                    .description(messageSource.getMessage("get.all-users.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-users.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while getting the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("get.all-users.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("get.all-users.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @GetMapping("/invitedUsersCount")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getInvitedUsersCount(@RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ResponseEntity responseEntity = new ResponseEntity<>(userService.getInvitedUsersCount(userDetailsDTO.getConsultancyId(),
                    userDetailsDTO.getId(), userDetailsDTO.getRoles().get(0), pageNumber, pageSize), HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while getting the user", e);
            log.error("Error while getting the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("search.user.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("search.user.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @GetMapping("/search")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity userSearch(@RequestParam("text") String searchText) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            ApiResponse apiResponse = ApiResponse.builder().data(userService.getCollabUsersforSearch(userDetailsDTO.getConsultancyId(), userDetailsDTO.getId(), searchText, userDetailsDTO.getRoles().get(0)))
                    .description(messageSource.getMessage("search.user.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("search.user.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while getting the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("search.user.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("search.user.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @SuppressWarnings("rawtypes")
    @ResponseBody
    @PostMapping("/forgot-password")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity changePassword(@RequestBody ChangePasswordDTO forgotPasswordDTO) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            userService.changePassword(forgotPasswordDTO, userDetailsDTO.getId());
            ApiResponse apiResponse = ApiResponse.builder().data("")
                    .description(messageSource.getMessage("user.change-password.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.change-password.success.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
            return responseEntity;
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("user.change-password.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.change-password.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @ResponseBody
    @PostMapping("/verify/{userId}/{code}")
    public ResponseEntity verifyUser(@PathVariable("userId") Long userId, @PathVariable("code") String code,
                                     @RequestBody PasswordDTO passwordDTO) {
        try {
            int output = userService.verifyUserWithPassword(userId, code, passwordDTO);
            if (output == 1) {
                ApiResponse apiResponse = ApiResponse.builder().data(userId)
                        .description(
                                messageSource.getMessage("email.verify.success.description", new Object[]{}, null))
                        .status(messageSource.getMessage("success.status", new Object[]{}, null))
                        .title(messageSource.getMessage("email.verify.success.title", new Object[]{}, null)).build();

                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            } else if (output == -1) {
                ApiResponse apiResponse = ApiResponse.builder().data(userId)
                        .description(messageSource.getMessage("email.verify.fail.description", new Object[]{}, null))
                        .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                        .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
                return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
            } else if (output == -2) {
                ApiResponse apiResponse = ApiResponse.builder().data(userId)
                        .description(messageSource.getMessage("password.exists", new Object[]{}, null))
                        .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                        .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
                return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Error while saving the user", e);
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("email.verify.fail.description", new Object[]{e.getMessage()}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
        ApiResponse apiResponse = ApiResponse.builder().data(-1)
                .description(messageSource.getMessage("email.verify.fail.description", new Object[]{null}, null))
                .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                .title(messageSource.getMessage("email.verify.fail.title", new Object[]{}, null)).build();
        ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }

    @ResponseBody
    @GetMapping("/forgotPassword")
    public ResponseEntity forgotPassword(@RequestParam("emailAddress") String emailAddress) {
        try {
            userService.forgotPasswordGenerateCode(emailAddress);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("email.send.success.message", new Object[]{}, null))
                    .description(
                            messageSource.getMessage("generate.forget-password.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.forgot-password.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (AuthenticationException ae) {
            ApiResponse apiResponse = ApiResponse.builder().data(-1)
                    .description(messageSource.getMessage("user.forgot-password.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.forgot-password.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{}, null))
                    .description(messageSource.getMessage("user.forgot-password.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.forgot-password.fail.title", new Object[]{}, null)).build();
            ResponseEntity responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            return responseEntity;
        }
    }

    @ResponseBody
    @PostMapping("/forgotPassword")
    public ResponseEntity forgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTO,
                                         @RequestParam("code") String code, @RequestParam("user_id") Long userId) {
        try {
            userService.forgotPassword(forgotPasswordDTO, userId, code);
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("email.send.success.message", new Object[]{}, null))
                    .description(
                            messageSource.getMessage("generate.forget-password.success.description", new Object[]{}, null))
                    .status(messageSource.getMessage("success.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.forgot-password.success.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (AuthenticationException ae) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .data(messageSource.getMessage("error.unknown", new Object[]{}, null))
                    .description(messageSource.getMessage("user.forgot-password.fail.description", new Object[]{}, null))
                    .status(messageSource.getMessage("fail.status", new Object[]{}, null))
                    .title(messageSource.getMessage("user.forgot-password.fail.title", new Object[]{}, null)).build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data("Unknown error occured").description("Forgot Password")
                    .status("Success").title("Forgot Password").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}