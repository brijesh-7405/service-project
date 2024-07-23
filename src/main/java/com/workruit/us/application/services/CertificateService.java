package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.CertificationDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.Certification;
import com.workruit.us.application.repositories.ApplicantRepository;
import com.workruit.us.application.repositories.CertificateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    @Autowired
    private ApplicantRepository applicantRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private ModelMapper modelMapper;
    private @Autowired ApplicantService applicantService;

    public List<CertificationDTO> getApplicantCertificate(Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<Certification> certificationList = certificateRepository.findByApplicantId(applicant.getApplicantId());
        return certificationList.stream().map(certificateDTO -> {
            return modelMapper.map(certificateDTO, CertificationDTO.class);
        }).collect(Collectors.toList());
    }

    public Long updateApplicantCertificate(List<CertificationDTO> certificationDTOList, Long applicantId, Long consultancyId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));

        List<Certification> certificationList = certificateRepository.findByApplicantId(applicant.getApplicantId());
        List<Long> allCertificateIds = certificationList.stream().map(Certification::getCertificationId).collect(Collectors.toList());
        List<Certification> addCertificate = new ArrayList<>();

        certificationDTOList.stream()
                .filter(Objects::nonNull)
                .forEach(certificateDTO -> {
                    if (allCertificateIds.contains(certificateDTO.getCertificationId())) {
                        // update entity
                        addCertificate.add(setCertificationEntity(certificateDTO, applicantId));
                        allCertificateIds.remove(certificateDTO.getCertificationId());
                    } else if (certificateDTO.getCertificationId() == 0) {
                        // add new entity
                        addCertificate.add(setCertificationEntity(certificateDTO, applicantId));
                    }
                });
        List<Certification> savedCertificate = certificateRepository.saveAll(addCertificate);
        certificateRepository.deleteAllById(allCertificateIds);
        applicant.setCorrectionRequired(!applicantService.isCorrectionSolved(applicantId));
        applicantRepository.save(applicant);
        return savedCertificate.size() > 0 ? savedCertificate.get(0).getCertificationId() : 0;
    }

    public Optional<Certification> getCertificateById(Long certificateId) {
        return certificateRepository.findById(certificateId);
    }

    public void deleteCertificate(Long certificateId) throws WorkruitException {
        Certification certificate = certificateRepository.findById(certificateId).orElseThrow(() -> new WorkruitException("Certificate not found with id " + certificateId));
        certificateRepository.delete(certificate);
    }

    private Certification setCertificationEntity(CertificationDTO certificationDTO, Long applicantId) {
        Certification certification = new Certification();
        certification.setApplicantId(applicantId);
        certification.setCertificationId(certificationDTO.getCertificationId());
        certification.setTitle(certificationDTO.getTitle());
        certification.setDescription(certificationDTO.getDescription());
        certification.setValidFrom(certificationDTO.getValidFrom());
        certification.setValidTo(certificationDTO.getValidTo());
        certification.setUploadCertificate(certificationDTO.getUploadCertificate());
        certification.setDoesNotExpire(certificationDTO.isDoesNotExpire());
        if (certificationDTO.getCertificationId() == null || certificationDTO.getCertificationId() == 0)
            certification.setCreatedDate(new Date());
        certification.setUpdatedDate(new Date());
        return certification;
    }
}
