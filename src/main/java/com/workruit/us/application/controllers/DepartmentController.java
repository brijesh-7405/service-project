/**
 *
 */
package com.workruit.us.application.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.workruit.us.application.controllers.utils.ControllerUtils;
import com.workruit.us.application.dto.DepartmentDTO;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.services.DepartmentService;

import io.swagger.annotations.ApiImplicitParam;

/**
 * @author Santosh Bhima
 */
@RestController
public class DepartmentController {

    private @Autowired DepartmentService departmentService;

    @SuppressWarnings("unchecked")
    @GetMapping("/departments")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity getDepartments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity(departmentService.getDepartments(), HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @PostMapping("/departments")
    @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer <<ACCESS_TOKEN>>")
    public ResponseEntity createDepartment(@RequestBody DepartmentDTO roleDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            return new ResponseEntity(departmentService.createDepartment(roleDTO), HttpStatus.CREATED);
        } catch (Exception e) {
            return ControllerUtils.genericErrorMessage();
        }
    }

}
