/**
 *
 */
package com.workruit.us.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.CompanyDetailsDTO;
import com.workruit.us.application.dto.ConsultancyDetailsDTO;
import com.workruit.us.application.models.Company;
import com.workruit.us.application.models.CompanyClient;
import com.workruit.us.application.repositories.ClientRepository;
import com.workruit.us.application.repositories.CompanyRepository;
import com.workruit.us.application.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Santosh Bhima
 */
@Service
public class CompanyService {

    ObjectMapper objectMapper = new ObjectMapper();
    private @Autowired CompanyRepository companyRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired ConsultancyService consultantService;
    private @Autowired ImageService imageService;
    private @Autowired ModelMapper modelMapper;
    @Autowired
    private ClientRepository clientRepository;

    public void updateCompany(CompanyDetailsDTO companyDetailsDTO, Long companyId, Long consultancyId, Long userId)
            throws WorkruitException, ParseException, JsonProcessingException {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            company.setAbout(companyDetailsDTO.getAbout() != null ? companyDetailsDTO.getAbout() : company.getAbout());
            company.setUpdatedDate(new Date());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (companyDetailsDTO.getFoundedDate() != null) {
                Date foundedDate = simpleDateFormat.parse(companyDetailsDTO.getFoundedDate());
                company.setFoundedDate(foundedDate);
            }
            company.setIndustryTypes(companyDetailsDTO.getIndustryType() != null ? companyDetailsDTO.getIndustryType() : company.getIndustryTypes());
            company.setWebsite(companyDetailsDTO.getWebsite() != null ? companyDetailsDTO.getWebsite() : company.getWebsite());
            company.setProfileImageUrl(companyDetailsDTO.getProfileImageUrl() != null ? companyDetailsDTO.getProfileImageUrl() : company.getProfileImageUrl());

            company.setFacebookLink(companyDetailsDTO.getFacebookLink() != null ? companyDetailsDTO.getFacebookLink() : company.getFacebookLink());
            company.setTwitterLink(companyDetailsDTO.getTwitterLink() != null ? companyDetailsDTO.getTwitterLink() : company.getTwitterLink());
            company.setLinkedinLink(companyDetailsDTO.getLinkedinLink() != null ? companyDetailsDTO.getLinkedinLink() : company.getLinkedinLink());
            company.setFounderName(companyDetailsDTO.getFounderName() != null && companyDetailsDTO.getFounderName().toString() != null ? companyDetailsDTO.getFounderName().toString() : company.getFounderName());
            company.setCompanySize(companyDetailsDTO.getNumberOfEmployees() != null ? companyDetailsDTO.getNumberOfEmployees() : company.getCompanySize());
            company.setHeadquarters(companyDetailsDTO.getHeadquarters() != null ? companyDetailsDTO.getHeadquarters() : company.getHeadquarters());
            company.setLocation(companyDetailsDTO.getLocation() != null ? companyDetailsDTO.getLocation() : company.getLocation());
            company.setAwards(companyDetailsDTO.getAwards() != null ? companyDetailsDTO.getAwards() : company.getAwards());
            company.setRecognisation(companyDetailsDTO.getRecognisation() != null ? companyDetailsDTO.getRecognisation() : company.getRecognisation());
            company.setProductAndServices(companyDetailsDTO.getProductAndServices() != null && companyDetailsDTO.getProductAndServices().toString() != null ? companyDetailsDTO.getProductAndServices().toString() : company.getProductAndServices());
            if (companyDetailsDTO.getDomains() != null)
                company.setDomains(companyDetailsDTO.getDomains().stream().map(Object::toString)
                        .collect(Collectors.joining(",")));


            Set<CompanyClient> clients = new HashSet<>();
            Set<CompanyClient> removeClientSet = new HashSet<>();
            if (companyDetailsDTO.getClients() != null && !companyDetailsDTO.getClients().isEmpty() && companyDetailsDTO.getClients().size() > 0) {
                companyDetailsDTO.getClients().stream().forEach(c -> {
                    CompanyClient client = new CompanyClient();
                    if (c.getClientId() != 0)
                        client.setClientId(c.getClientId());
                    client.setClientName(c.getClientName());
                    client.setLink(c.getLink());
                    clients.add(client);
                });
                removeClientSet.addAll(company.getClients());
                List<Long> updateClientSet = clients.stream().filter(e -> e.getClientId() != null)
                        .collect(Collectors.toSet()).stream().map(e -> e.getClientId()).collect(Collectors.toList());
                removeClientSet.removeIf(e -> updateClientSet.contains(e.getClientId()));
                company.setClients(clients);
            } else if (companyDetailsDTO.getClients() != null && companyDetailsDTO.getClients().isEmpty() && companyDetailsDTO.getClients().size() == 0) {
                if (company.getClients() != null && !company.getClients().isEmpty() && company.getClients().size() > 0)
                    removeClientSet.addAll(company.getClients());
                company.setClients(clients);
            }
            company.setUpdatedDate(new Date());
            company.setOverallTalentPool((companyDetailsDTO.getOverallTalentPool() != null && !companyDetailsDTO.getOverallTalentPool().isEmpty()) ? companyDetailsDTO.getOverallTalentPool() : company.getOverallTalentPool());

            companyRepository.save(company);

            for (CompanyClient removeClient : removeClientSet) {
                companyRepository.deleteClientById(removeClient.getClientId());
            }
            ConsultancyDetailsDTO consultancyDetailsDTO = consultantService.getConsultancyDetails(consultancyId, userId);
            if (consultancyDetailsDTO != null)
                consultantService.updateConsultancywithCompanyDetails(company, consultancyId);
        } else {
            throw new WorkruitException("Company not found for the given id:" + companyId);
        }
    }

    public void updateCompanywithConsultancyDetails(ConsultancyDetailsDTO companyDetailsDTO, Long companyId)
            throws WorkruitException, ParseException {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            company.setAbout(companyDetailsDTO.getAbout());
            company.setUpdatedDate(new Date());
            if (companyDetailsDTO.getFoundedDate() != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date foundedDate = simpleDateFormat.parse(companyDetailsDTO.getFoundedDate());
                company.setFoundedDate(foundedDate);
            }
            company.setIndustryTypes(companyDetailsDTO.getIndustryType());
            company.setWebsite(companyDetailsDTO.getWebsite());
            company.setProfileImageUrl(companyDetailsDTO.getProfileImageUrl());
            company.setFacebookLink(companyDetailsDTO.getFacebookLink());
            company.setTwitterLink(companyDetailsDTO.getTwitterLink());
            company.setLinkedinLink(companyDetailsDTO.getLinkedinLink());
            // company.setFounderName(companyDetailsDTO.getFounderName().toString());
            company.setProfileImageUrl(companyDetailsDTO.getProfileImageUrl());
            // company.setCompanySize(companyDetailsDTO.getNumberOfEmployees());
            // company.setHeadquarters(companyDetailsDTO.getHeadquarters());
            company.setLocation(companyDetailsDTO.getLocation());
            // company.setAwards(companyDetailsDTO.getAwards());
            // company.setRecognisation(companyDetailsDTO.getRecognisation());
            // company.setProductAndServices(companyDetailsDTO.getProductAndServices().toString());
            companyRepository.save(company);
        } else {
            throw new WorkruitException("Company not found for the given id:" + companyId);
        }
    }

    public String getCompanyName(Long companyId) {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            if (company != null && company.getName() != null) {
                return company.getName();
            } else {
                return "";
            }
        }
        return "";
    }

    public String getCompanyLocation(Long companyId) {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            if (company != null && company.getLocation() != null) {
                return company.getLocation();
            } else {
                return "";
            }
        }
        return "";
    }

    public String getCompanyImage(Long companyId) {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            if (company != null && company.getLocation() != null) {
                return company.getProfileImageUrl();
            } else {
                return null;
            }
        }
        return null;
    }

    public CompanyDetailsDTO getCompany(Long companyId)
            throws WorkruitException, JsonProcessingException {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            CompanyDetailsDTO companyDetailsDTO = new CompanyDetailsDTO();
            companyDetailsDTO.setAbout(company.getAbout());
            companyDetailsDTO.setCompanyName(company.getName());
            if (company.getFoundedDate() != null) {
                companyDetailsDTO.setFoundedDate(company.getFoundedDate().toString());
            }
            if (company.getFounderName() != null) {
                companyDetailsDTO.setFounderName(objectMapper.readValue(company.getFounderName(), JsonNode.class));
            }
            companyDetailsDTO.setIndustryType(company.getIndustryTypes());
            companyDetailsDTO.setWebsite(company.getWebsite());
            companyDetailsDTO.setProfileImageUrl(company.getProfileImageUrl());
            String imageUrl = company.getProfileImageUrl();
            try {
                companyDetailsDTO.setProfileImageData(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                companyDetailsDTO.setProfileImageData(null);
            }
            companyDetailsDTO.setFacebookLink(company.getFacebookLink());
            companyDetailsDTO.setTwitterLink(company.getTwitterLink());
            companyDetailsDTO.setLinkedinLink(company.getLinkedinLink());
            companyDetailsDTO.setNumberOfEmployees(company.getCompanySize());
            companyDetailsDTO.setHeadquarters(company.getHeadquarters());
            companyDetailsDTO.setLocation(company.getLocation());
            companyDetailsDTO.setAwards(company.getAwards());
            companyDetailsDTO.setOverallTalentPool(company.getOverallTalentPool());
            companyDetailsDTO.setRecognisation(company.getRecognisation());
            if (company.getClients() != null)
                companyDetailsDTO.setClients(company.getClients().stream()
                        .map(c -> modelMapper.map(c, CompanyClient.class)).collect(Collectors.toSet()));
            if (company.getDomains() != null)
                companyDetailsDTO
                        .setDomains(Stream.of(company.getDomains().split(",", -1)).collect(Collectors.toList()));
            companyDetailsDTO.setFacebookLink(company.getFacebookLink());
            if (company.getProductAndServices() != null) {
                companyDetailsDTO
                        .setProductAndServices(objectMapper.readValue(company.getProductAndServices(), JsonNode.class));
            }
            return companyDetailsDTO;
        } else {
            throw new WorkruitException("Company not found for the given id:" + companyId);
        }
    }
}
