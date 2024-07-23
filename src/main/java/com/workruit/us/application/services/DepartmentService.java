/**
 *
 */
package com.workruit.us.application.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.workruit.us.application.dto.DepartmentDTO;
import com.workruit.us.application.models.Department;
import com.workruit.us.application.repositories.DepartmentRepository;

/**
 * @author Santosh Bhima
 */
@Service
public class DepartmentService {
    private @Autowired DepartmentRepository departmentRepository;

    public List<DepartmentDTO> getDepartments() {
        Iterable<Department> iterable = departmentRepository.findAll();
        List<DepartmentDTO> departmentDTOs = new ArrayList<>();
        iterable.forEach(department -> {
            DepartmentDTO departmentDTO = new DepartmentDTO();
            departmentDTO.setDepartmentId(department.getDepartmentId());
            departmentDTO.setName(department.getName());
            departmentDTO.setDescription(department.getDescription());
            departmentDTOs.add(departmentDTO);
        });
        return departmentDTOs;
    }

    @Transactional
    public Long createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setCreatedDate(new Date());
        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());
        department = departmentRepository.save(department);
        return department.getDepartmentId();
    }
}
