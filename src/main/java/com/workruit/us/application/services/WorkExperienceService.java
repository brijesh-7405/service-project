package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.WorkExperienceDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.WorkExperience;
import com.workruit.us.application.repositories.ApplicantRepository;
import com.workruit.us.application.repositories.WorkExperienceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class WorkExperienceService {

    private @Autowired WorkExperienceRepository workExperienceRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ModelMapper modelMapper;

    private @Autowired ApplicantService applicantService;


    public void updateApplicantWorkExperience(List<WorkExperienceDTO> workExperienceDTOList, Long applicantId,
                                              Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(
                        () -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<WorkExperience> workExperienceList = workExperienceRepository
                .findByApplicantId(applicant.getApplicantId());
        List<Long> allExperienceIds = workExperienceList.stream().map(WorkExperience::getWorkExperienceId)
                .collect(Collectors.toList());
        List<WorkExperience> addExperience = new ArrayList<>();

        workExperienceDTOList.stream().filter(Objects::nonNull).forEach(workExperienceDTO -> {
            if (allExperienceIds.contains(workExperienceDTO.getWorkExperienceId())) {
                // update entity
                addExperience.add(setWorkExperienceEntity(workExperienceDTO, applicantId));
                allExperienceIds.remove(workExperienceDTO.getWorkExperienceId());
            } else if (workExperienceDTO.getWorkExperienceId() == 0) {
                // add new entity
                addExperience.add(setWorkExperienceEntity(workExperienceDTO, applicantId));
            }
        });
        workExperienceRepository.saveAll(addExperience);
        workExperienceRepository.deleteAllById(allExperienceIds);
        applicant.setCorrectionRequired(!applicantService.isCorrectionSolved(applicantId));
        applicantRepository.save(applicant);
        applicantService.runJobMatcher(applicantId);
    }

    private WorkExperience setWorkExperienceEntity(WorkExperienceDTO workExperienceDTO, Long applicantId) {
        WorkExperience addOrUpdateEntity = new WorkExperience();
        addOrUpdateEntity.setWorkExperienceId(workExperienceDTO.getWorkExperienceId());
        addOrUpdateEntity.setApplicantId(applicantId);
        addOrUpdateEntity.setCompanyName(workExperienceDTO.getCompanyName());
        if (workExperienceDTO.getWorkExperienceId() == null || workExperienceDTO.getWorkExperienceId() == 0)
            addOrUpdateEntity.setCreatedDate(new Date());
        addOrUpdateEntity.setUpdatedDate(new Date());
        addOrUpdateEntity.setCurrentlyWorkingHere(workExperienceDTO.isCurrentlyWorkingHere());
        addOrUpdateEntity.setDescription(workExperienceDTO.getDescription());
        addOrUpdateEntity.setEndDate(workExperienceDTO.getEndDate());
        addOrUpdateEntity.setStartDate(workExperienceDTO.getStartDate());
        addOrUpdateEntity.setJobTitle(workExperienceDTO.getJobTitle());
        addOrUpdateEntity.setLocation(workExperienceDTO.getLocation());

        return addOrUpdateEntity;
    }

    public List<WorkExperienceDTO> getApplicantWorkExperience(Long applicantId, Long consultancyId)
            throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(
                        () -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<WorkExperience> workExperiences = workExperienceRepository.findByApplicantId(applicant.getApplicantId());
        return workExperiences.stream().map(workExperience -> {
            return modelMapper.map(workExperience, WorkExperienceDTO.class);
        }).collect(Collectors.toList());
    }

    public void deleteWorkExperience(Long applicantId, Long workExperienceId) throws WorkruitException {
        WorkExperience workExperience = workExperienceRepository.findByWorkExperienceIdAndApplicantId(workExperienceId,
                applicantId);
        if (workExperience != null) {
            workExperienceRepository.delete(workExperience);
        } else {
            throw new WorkruitException("No applicant workexperience match found");
        }
        applicantService.runJobMatcher(applicantId);
    }
}
