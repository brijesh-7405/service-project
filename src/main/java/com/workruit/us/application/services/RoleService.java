/**
 *
 */
package com.workruit.us.application.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.workruit.us.application.dto.RoleDTO;
import com.workruit.us.application.models.Role;
import com.workruit.us.application.repositories.RoleRepository;

/**
 * @author Santosh Bhima
 */
@Service
public class RoleService {
    private @Autowired RoleRepository roleRepository;

    public List<RoleDTO> getRoles() {
        Iterable<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = new ArrayList<>();
        roles.forEach(role -> {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setDescription(role.getDescription());
            roleDTO.setName(role.getName());
            roleDTO.setRoleId(role.getRoleId());
            roleDTOs.add(roleDTO);
        });
        return roleDTOs;
    }

    public Long createRole(RoleDTO roleDTO) {
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role = roleRepository.save(role);
        return role.getRoleId();
    }
}
