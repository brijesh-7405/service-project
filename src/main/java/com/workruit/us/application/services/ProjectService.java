package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.ProjectDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.Project;
import com.workruit.us.application.repositories.ApplicantRepository;
import com.workruit.us.application.repositories.ProjectRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Component
public class ProjectService {

    private @Autowired ProjectRepository projectRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ModelMapper modelMapper;
    private @Autowired ApplicantService applicantService;

    public void updateApplicantProjects(List<ProjectDTO> projectDTOList, Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<Project> projectList = projectRepository.findByApplicantId(applicant.getApplicantId());
        List<Long> allProjectIds = projectList.stream().map(Project::getProjectId).collect(Collectors.toList());
        List<Project> addProject = new ArrayList<>();

        projectDTOList.stream()
                .filter(Objects::nonNull)
                .forEach(projectDTO -> {
                    if (allProjectIds.contains(projectDTO.getProjectId())) {
                        // update entity
                        addProject.add(setProjectEntity(projectDTO, applicantId));
                        allProjectIds.remove(projectDTO.getProjectId());
                    } else if (projectDTO.getProjectId() == 0) {
                        // add new entity
                        addProject.add(setProjectEntity(projectDTO, applicantId));
                    }
                });
        projectRepository.saveAll(addProject);
        projectRepository.deleteAllById(allProjectIds);
        applicant.setCorrectionRequired(!applicantService.isCorrectionSolved(applicantId));
        applicantRepository.save(applicant);
    }

    public List<ProjectDTO> getApplicantProjects(Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<Project> projects = projectRepository.findByApplicantId(applicantId);
        return projects.stream().map(project -> {
            return modelMapper.map(project, ProjectDTO.class);
        }).collect(Collectors.toList());
    }

    private Project setProjectEntity(ProjectDTO projectDTO, Long applicantId) {
        Project project = new Project();
        project.setApplicantId(applicantId);
        project.setProjectId(projectDTO.getProjectId());
        if (projectDTO.getProjectId() == null || projectDTO.getProjectId() == 0)
            project.setCreatedDate(new Date());
        project.setCreatedDate(projectDTO.getStartDate() != null ? projectDTO.getStartDate() : new Date());
        project.setDescription(projectDTO.getDescription());
        project.setEndDate(projectDTO.getEndDate());
        project.setInstitutionName(projectDTO.getInstitutionName());
        project.setLocation(projectDTO.getLocation());
        project.setProjectOngoing(projectDTO.isProjectOngoing());
        project.setRole(projectDTO.getRole());
        project.setStartDate(projectDTO.getStartDate());
        project.setTitle(projectDTO.getTitle());
        project.setUpdatedDate(new Date());

        return project;
    }
}