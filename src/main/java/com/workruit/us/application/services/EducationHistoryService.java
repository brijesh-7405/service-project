package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.EducationHistoryDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.ApplicantDetails;
import com.workruit.us.application.models.Degrees;
import com.workruit.us.application.models.EducationHistory;
import com.workruit.us.application.repositories.ApplicantDetailsRepository;
import com.workruit.us.application.repositories.ApplicantRepository;
import com.workruit.us.application.repositories.DegreesRepository;
import com.workruit.us.application.repositories.EducationHistoryRepository;
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
public class EducationHistoryService {

    private @Autowired EducationHistoryRepository educationHistoryRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ModelMapper modelMapper;
    private @Autowired ApplicantService applicantService;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired DegreesRepository degreesRepository;


    public void updateApplicantEducationHistory(List<EducationHistoryDTO> educationHistoryDTOList, Long applicantId, Long consultancyId)
            throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<EducationHistory> educationHistoryList = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
        List<Long> allHistoryIds = educationHistoryList.stream().map(EducationHistory::getEducationHistoryId).collect(Collectors.toList());
        List<EducationHistory> addExperienceList = new ArrayList<>();

        educationHistoryDTOList.stream()
                .filter(Objects::nonNull)
                .forEach(educationHistoryDTO -> {
                    if (allHistoryIds.contains(educationHistoryDTO.getEducationHistoryId())) {
                        // update entity
                        addExperienceList.add(setEducationHistoryEntity(educationHistoryDTO, applicantId));
                        allHistoryIds.remove(educationHistoryDTO.getEducationHistoryId());
                    } else if (educationHistoryDTO.getEducationHistoryId() == 0) {
                        // add new entity
                        addExperienceList.add(setEducationHistoryEntity(educationHistoryDTO, applicantId));
                    }
                });
        ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicantId);
        if (addExperienceList != null && !addExperienceList.isEmpty()) {
            EducationHistory educationHistory = addExperienceList.get(addExperienceList.size() - 1);
            Degrees degrees = degreesRepository.findByShortTitle(educationHistory.getDegree());
            if (degrees != null) {
                applicantDetails.setDegreeId(Long.valueOf(degrees.getDegreeId()));
                applicantDetailsRepository.save(applicantDetails);
            }
        }
        educationHistoryRepository.saveAll(addExperienceList);
        educationHistoryRepository.deleteAllById(allHistoryIds);
        applicant.setCorrectionRequired(!applicantService.isCorrectionSolved(applicantId));
        applicantRepository.save(applicant);
        applicantService.runJobMatcher(applicantId);
    }

    public List<EducationHistoryDTO> getApplicantEducationHistory(Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicantId);
        return educationHistories.stream().map(educationHistory -> {
            return modelMapper.map(educationHistory, EducationHistoryDTO.class);
        }).collect(Collectors.toList());
    }

    private EducationHistory setEducationHistoryEntity(EducationHistoryDTO educationHistoryDTO, Long applicantId) {
        EducationHistory educationHistory = new EducationHistory();
        educationHistory.setApplicantId(applicantId);
        educationHistory.setEducationHistoryId(educationHistoryDTO.getEducationHistoryId());
        educationHistory.setCollegeName(educationHistoryDTO.getCollegeName());
        if (educationHistoryDTO.getEducationHistoryId() == null || educationHistoryDTO.getEducationHistoryId() == 0)
            educationHistory.setCreatedDate(new Date());
        educationHistory.setCurrentlyStudying(educationHistoryDTO.isCurrentlyStudying());
        educationHistory.setDegree(educationHistoryDTO.getDegree());
        educationHistory.setDescription(educationHistoryDTO.getDescription());
        educationHistory.setEndDate(educationHistoryDTO.getEndDate());
        educationHistory.setField(educationHistoryDTO.getField());
        educationHistory.setLocation(educationHistoryDTO.getLocation());
        educationHistory.setStartDate(educationHistoryDTO.getStartDate());
        educationHistory.setUpdatedDate(new Date());

        return educationHistory;
    }
}