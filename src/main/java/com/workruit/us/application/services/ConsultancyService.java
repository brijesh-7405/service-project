/**
 *
 */
package com.workruit.us.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.CompanyDetailsDTO;
import com.workruit.us.application.dto.ConsultancyDetailsDTO;
import com.workruit.us.application.dto.DepartmentUserDto;
import com.workruit.us.application.dto.UserByDepartmentDTO;
import com.workruit.us.application.models.Client;
import com.workruit.us.application.models.Company;
import com.workruit.us.application.models.Consultancy;
import com.workruit.us.application.models.User;
import com.workruit.us.application.repositories.ClientRepository;
import com.workruit.us.application.repositories.CompanyRepository;
import com.workruit.us.application.repositories.ConsultancyRepository;
import com.workruit.us.application.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class ConsultancyService {

    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ClientRepository clientRepository;
    private @Autowired ModelMapper modelMapper;
    private @Autowired CompanyService companyService;
    private @Autowired UserRepository userRepository;

    private @Autowired ImageService imageService;
    private @Autowired CompanyRepository companyRepository;

    public void updateConsultancy(ConsultancyDetailsDTO consultancyDetailsDTO, Long consultancyId, Long companyId)
            throws WorkruitException, ParseException, JsonProcessingException {
        Optional<Consultancy> optionalConsultancy = consultancyRepository.findById(consultancyId);
        if (optionalConsultancy.isPresent()) {
            Consultancy consultancy = optionalConsultancy.get();
            //consultancy.setName(consultancyDetailsDTO.getName().isEmpty());
            consultancy.setAbout(consultancyDetailsDTO.getAbout());
            Set<Client> clients = new HashSet<>();
            if (!consultancyDetailsDTO.getClients().isEmpty() && consultancyDetailsDTO.getClients().size() > 0) {
                consultancyDetailsDTO.getClients().stream().forEach(c -> {
                    Client client = new Client();
                    if (c.getClientId() != 0)
                        client.setClientId(c.getClientId());
                    client.setClientName(c.getClientName());
                    client.setLink(c.getLink());
                    clients.add(client);
                });
            }

            Set<Client> removeClientSet = new HashSet<>();
            removeClientSet.addAll(consultancy.getClients());
            List<Long> updateClientSet = clients.stream().filter(e -> e.getClientId() != null)
                    .collect(Collectors.toSet()).stream().map(e -> e.getClientId()).collect(Collectors.toList());
            removeClientSet.removeIf(e -> updateClientSet.contains(e.getClientId()));

            consultancy.setClients(clients);
            consultancy.setUpdatedDate(new Date());
            if (consultancyDetailsDTO.getDomains() != null)
                consultancy.setDomains(consultancyDetailsDTO.getDomains().stream().map(Object::toString)
                        .collect(Collectors.joining(",")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (consultancyDetailsDTO.getFoundedDate() != null) {
                Date foundedDate = simpleDateFormat.parse(consultancyDetailsDTO.getFoundedDate());
                consultancy.setFoundedDate(foundedDate);
            }
            consultancy.setIndustryTypes(consultancyDetailsDTO.getIndustryType());
            consultancy.setNumberOfEmployees(consultancyDetailsDTO.getNumberOfEmployees());
            consultancy.setWebsite(consultancyDetailsDTO.getWebsite());
            //consultancy.setProfileImageUrl(consultancyDetailsDTO.getProfileImageUrl());

            consultancy.setLocation(consultancyDetailsDTO.getLocation());
            consultancy.setFacebookLink(consultancyDetailsDTO.getFacebookLink());
            consultancy.setTwitterLink(consultancyDetailsDTO.getTwitterLink());
            consultancy.setLinkedinLink(consultancyDetailsDTO.getLinkedinLink());
            consultancy.setOverallTalentPool(consultancyDetailsDTO.getOverallTalentPool());
            consultancy.setNumberOfApplicantsHired(consultancyDetailsDTO.getNumberOfHiredApplicants());

            consultancyRepository.save(consultancy);

            for (Client removeClient : removeClientSet) {
                clientRepository.deleteById(removeClient.getClientId());
            }
            try {
                CompanyDetailsDTO companyDetailsDTO = companyService.getCompany(companyId);
                if (companyDetailsDTO != null)
                    companyService.updateCompanywithConsultancyDetails(consultancyDetailsDTO, companyId);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            throw new WorkruitException("Consultancy not found for the given id:" + consultancyId);
        }
    }

    //
    public void updateConsultancywithCompanyDetails(Company consultancyDetailsDTO, Long consultancyId)
            throws WorkruitException, ParseException {
        Optional<Consultancy> optionalConsultancy = consultancyRepository.findById(consultancyId);
        if (optionalConsultancy.isPresent()) {
            Consultancy consultancy = optionalConsultancy.get();
            // consultancy.setName(consultancyDetailsDTO.getName());
            consultancy.setAbout(consultancyDetailsDTO.getAbout());
//			Set<Client> clients = new HashSet<>();
//			if (!consultancyDetailsDTO.getClients().isEmpty() && consultancyDetailsDTO.getClients().size() > 0) {
//				consultancyDetailsDTO.getClients().stream().forEach(c -> {
//					Client client = new Client();
//					if (c.getClientId() != 0)
//						client.setClientId(c.getClientId());
//					client.setClientName(c.getClientName());
//					client.setLink(c.getLink());
//					clients.add(client);
//				});
//			}

//			Set<Client> removeClientSet = new HashSet<>();
//			removeClientSet.addAll(consultancy.getClients());
//			List<Long> updateClientSet = clients.stream().filter(e -> e.getClientId() != null)
//					.collect(Collectors.toSet()).stream().map(e -> e.getClientId()).collect(Collectors.toList());
//			removeClientSet.removeIf(e -> updateClientSet.contains(e.getClientId()));
//
//			consultancy.setClients(clients);
            consultancy.setUpdatedDate(new Date());
//			if (consultancyDetailsDTO.getDomains() != null)
//				consultancy.setDomains(consultancyDetailsDTO.getDomains().stream().map(Object::toString)
//						.collect(Collectors.joining(",")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (consultancyDetailsDTO.getFoundedDate() != null) {
                //Date foundedDate = simpleDateFormat.parse(consultancyDetailsDTO.getFoundedDate());
                consultancy.setFoundedDate(consultancyDetailsDTO.getFoundedDate());
            }
            consultancy.setIndustryTypes(consultancyDetailsDTO.getIndustryTypes());
            consultancy.setNumberOfEmployees(String.valueOf(consultancyDetailsDTO.getCompanySize()));
            consultancy.setWebsite(consultancyDetailsDTO.getWebsite());
            consultancy.setProfileImageUrl(consultancyDetailsDTO.getProfileImageUrl());

            consultancy.setLocation(consultancyDetailsDTO.getLocation());
            consultancy.setFacebookLink(consultancyDetailsDTO.getFacebookLink());
            consultancy.setTwitterLink(consultancyDetailsDTO.getTwitterLink());
            consultancy.setLinkedinLink(consultancyDetailsDTO.getLinkedinLink());
            consultancy.setOverallTalentPool(consultancyDetailsDTO.getOverallTalentPool());
            // consultancy.setNumberOfApplicantsHired(consultancyDetailsDTO.getOverallTalentPool() != null ? Long.parseLong(consultancyDetailsDTO.getOverallTalentPool()) : 0);

            consultancyRepository.save(consultancy);

//			for (Client removeClient : removeClientSet) {
//				clientRepository.deleteById(removeClient.getClientId());
//			}

        } else {
            throw new WorkruitException("Consultancy not found for the given id:" + consultancyId);
        }
    }

    public ConsultancyDetailsDTO getConsultancyDetails(Long consultancyId, Long userId) throws ParseException, WorkruitException, JsonProcessingException {
        Optional<Consultancy> optionalConsultancy = consultancyRepository.findById(consultancyId);
        if (optionalConsultancy.isPresent()) {
            Consultancy consultancy = optionalConsultancy.get();
            ConsultancyDetailsDTO consultancyDetailsDTO = new ConsultancyDetailsDTO();
            consultancyDetailsDTO.setAbout(consultancy.getAbout());
            if (consultancy.getClients() != null)
//                consultancyDetailsDTO.setClients(consultancy.getClients().stream()
//                        .map(c -> modelMapper.map(c, ClientDTO.class)).collect(Collectors.toSet()));
                if (consultancy.getDomains() != null)
                    consultancyDetailsDTO
                            .setDomains(Stream.of(consultancy.getDomains().split(",", -1)).collect(Collectors.toList()));
            consultancyDetailsDTO.setFacebookLink(consultancy.getFacebookLink());
            if (consultancy.getFoundedDate() != null) {
                consultancyDetailsDTO.setFoundedDate(consultancy.getFoundedDate().toString());
            }
            consultancyDetailsDTO.setIndustryType(consultancy.getIndustryTypes());
            consultancyDetailsDTO.setNumberOfEmployees(consultancy.getNumberOfEmployees());
            consultancyDetailsDTO.setLinkedinLink(consultancy.getLinkedinLink());
            consultancyDetailsDTO.setOverallTalentPool(consultancy.getOverallTalentPool());
            consultancyDetailsDTO.setLocation(consultancy.getLocation());
            consultancyDetailsDTO.setName(consultancy.getName());
            consultancyDetailsDTO.setWebsite(consultancy.getWebsite());
            consultancyDetailsDTO.setProfileImageUrl(consultancy.getProfileImageUrl());
            String imageUrl = consultancy.getProfileImageUrl();
            try {
                consultancyDetailsDTO.setProfileImageData(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                consultancyDetailsDTO.setProfileImageData(null);
            }

            List<Long> idList = new ArrayList<>();
            idList.add(consultancy.getConsultancyId());

            List<User> usersList = userRepository.findByConsultancyUsersIds(idList);

            Optional<Company> optionalCompany = companyRepository.findById(usersList.get(0).getCompanyId());
            if (optionalCompany.isPresent()) {
                consultancyDetailsDTO.setClients(optionalCompany.get().getClients());
                if (optionalCompany.get().getDomains() != null && !optionalCompany.get().getDomains().isEmpty()) {
                    List<String> list = new ArrayList<>();
                    Collections.addAll(list, optionalCompany.get().getDomains().split(","));
                    consultancyDetailsDTO.setDomains(list);
                }
            }

            consultancyDetailsDTO.setTwitterLink(consultancy.getTwitterLink());
            consultancyDetailsDTO.setNumberOfHiredApplicants(consultancy.getNumberOfApplicantsHired());

            return consultancyDetailsDTO;
        }
        return null;
    }


    public UserByDepartmentDTO getUsersByDepartment(Long consultancyId, Long userId, Integer pageNo, Integer pageSize) throws WorkruitException {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("userId").ascending());
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new WorkruitException(String.format("User is not found with id: %s", userId)));
        Page<User> users = userRepository.findByConsultancyIdAndDepartmentId(consultancyId, currentUser.getDepartmentId(), pageable);
        List<DepartmentUserDto> list = new ArrayList<>();
        for (User user : users) {
            DepartmentUserDto userDto = new DepartmentUserDto();
            userDto.setUserId(user.getUserId());
            String name = "";
            if (user.getFirstName() != null) {
                name = user.getFirstName();
            }
            if (user.getLastName() != null) {
                name += " " + user.getLastName();
            }
            userDto.setName(name);
            list.add(userDto);
        }
        UserByDepartmentDTO userByDepartmentDTO = new UserByDepartmentDTO();
        userByDepartmentDTO.setUsers(list);
        userByDepartmentDTO.setTotalPages(users.getTotalPages());
        userByDepartmentDTO.setTotalCount(users.getTotalElements());
        return userByDepartmentDTO;

    }

}
