/**
 *
 */
package com.workruit.us.application.controllers;

import com.amazonaws.HttpMethod;
import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.ImageService;
import io.swagger.annotations.ApiImplicitParam;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Santosh Bhima
 */
@RestController
public class ImageController {

    private @Autowired ImageService imageService;

    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/update-image")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity uploadImage(@RequestParam("file") String file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            String role = userDetailsDTO.getRoles().get(0);
            String saveImage = null;
            //  if (role != null && role.equals("COMPANY_ADMIN")) {
            saveImage = imageService.saveImage(file, role, userDetailsDTO.getCompanyId(), userDetailsDTO.getConsultancyId());
//            } else if (role != null && role.equals("CONSULTANCY_ADMIN")) {
//                saveImage = imageService.saveImage(file, role, userDetailsDTO.getConsultancyId());
//            }


            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("Image uploaded")
                    .status("Success").title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data("").description("Image uploaded failed")
                    .status("Failed").title("Failed").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/get-image")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity getImage(@RequestParam("id") String id) {
        try {
            String saveImage = imageService.getImage(id);
            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("Image uploaded")
                    .status("Success").title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse apiResponse = ApiResponse.builder().data("").description("Image uploaded failed")
                    .status("Failed").title("Failed").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/get-profileimage-upload-url")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity getUploadUrl() {
        try {
            String fileName = RandomStringUtils.randomAlphanumeric(32);
            String uploadUrl = imageService.generateUrl(fileName, HttpMethod.PUT);
            Map<String, String> fileData = new HashMap<>();
            fileData.put("uploadUrl", uploadUrl);
            fileData.put("key", fileName);
            ApiResponse apiResponse = ApiResponse.builder().data(fileData).description("File downloaded")
                    .title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }
}
