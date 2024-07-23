/**
 *
 */
package com.workruit.us.application.controllers;

import com.amazonaws.HttpMethod;
import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.ApiResponse;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.FileService;
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
public class FileController {

    private @Autowired FileService fileService;

    @SuppressWarnings("rawtypes")
//    @PostMapping(value = "/upload-file", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
//    @ResponseBody
//    public ResponseEntity uploadImage(@RequestParam("file") @RequestPart MultipartFile file) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
//            String saveImage = fileService.save(file, userDetailsDTO.getId());
//            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("File uploaded")
//                    .title("Success").build();
//            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//        } catch (Exception e) {
//            return ControllerUtils.genericErrorMessage();
//        }
//    }

    @PostMapping(value = "/update-user-file")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity uploadUserFiles(@RequestParam("file") String file, @RequestParam(name = "userId") Long userId,
                                          @RequestParam(name = "fileType") String fileType) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            String saveImage = fileService.saveUserFiles(file, userId, 0L, fileType);
            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("File uploaded")
                    .title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @PostMapping(value = "/update-user-certificate")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity uploadUserCerificateFiles(@RequestParam("file") String file, @RequestParam(name = "userId") Long userId,
                                                    @RequestParam(name = "certificateID") Long certificateID, @RequestParam(name = "fileType") String fileType) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            String saveImage = fileService.saveUserFiles(file, userDetailsDTO.getId(), certificateID, fileType);
            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("File uploaded")
                    .title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @SuppressWarnings("rawtypes")
    @GetMapping("/get-file")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity getImage(@RequestParam("id") String id) {
        try {
            String saveImage = fileService.get(id);
            ApiResponse apiResponse = ApiResponse.builder().data(saveImage).description("File downloaded")
                    .title("Success").build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @GetMapping("/get-file-upload-url")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity getUploadUrl() {
        try {
            String fileName = RandomStringUtils.randomAlphanumeric(32);
            String uploadUrl = fileService.generateUrl(fileName, HttpMethod.PUT);
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

    @GetMapping("/get-csv-file-upload-url")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    @ResponseBody
    public ResponseEntity getCSVUploadUrl() {
        try {
            String fileName = RandomStringUtils.randomAlphanumeric(32);
            String uploadUrl = fileService.generateCsvUrl(fileName, HttpMethod.PUT);
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
