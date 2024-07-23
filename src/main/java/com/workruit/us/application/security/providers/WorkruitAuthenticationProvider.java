/**
 *
 */
package com.workruit.us.application.security.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.CompanyDetailsDTO;
import com.workruit.us.application.dto.UserDetailsDTO;
import com.workruit.us.application.models.Applicant;
import com.workruit.us.application.models.Role;
import com.workruit.us.application.models.User;
import com.workruit.us.application.models.UserRole;
import com.workruit.us.application.repositories.RoleRepository;
import com.workruit.us.application.repositories.UserRoleRepository;
import com.workruit.us.application.services.ApplicantService;
import com.workruit.us.application.services.CompanyService;
import com.workruit.us.application.services.ConsultancyService;
import com.workruit.us.application.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Santosh
 */
public class WorkruitAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(WorkruitAuthenticationProvider.class);
    private @Autowired UserDetailsService userDetailsService;
    private @Autowired UserService userService;
    private @Autowired CompanyService companyService;
    private @Autowired ConsultancyService consultancyService;
    private @Autowired UserRoleRepository userRoleRepository;
    private @Autowired RoleRepository roleRepository;
    private @Autowired ApplicantService applicantService;

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.authentication.AuthenticationProvider#
     * authenticate(org.springframework.security.core.Authentication)
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        logger.info("Authenticating the user");
        Map<String, String> details = (Map<String, String>) authentication.getDetails();
        String type = details.get("type");
        User user = null;
        if (type != null && type.equals("applicant")) {
            Applicant applicant = getApplicantService().login(authentication.getPrincipal().toString(),
                    authentication.getCredentials().toString(), authentication.getDetails());
            if (applicant != null) {
                UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
                userDetailsDTO.setEmail(applicant.getEmail());
                userDetailsDTO.setName(applicant.getFirstName() + " " + applicant.getLastName());
                userDetailsDTO.setPhone(applicant.getPhoneNumber());
                userDetailsDTO.setId(applicant.getApplicantId());
                return new UsernamePasswordAuthenticationToken(userDetailsDTO, null);
            } else {
                throw new AuthenticationCredentialsNotFoundException("Invalid username or password");
            }
        }
        if (type != null && type.equals("email_verify")) {
            user = getUserService().verifyUser(Long.parseLong(authentication.getPrincipal().toString()),
                    authentication.getCredentials().toString());
        } else if (type != null && type.equals("role_switch")) {
            user = getUserService().getUser(Long.parseLong(authentication.getPrincipal().toString()),
                    authentication.getCredentials().toString());

        } else {
            user = getUserService().login(authentication.getPrincipal().toString(),
                    authentication.getCredentials().toString(), authentication.getDetails());
        }
        if (user != null) {
            UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
            userDetailsDTO.setEmail(user.getWorkEmail());
            userDetailsDTO.setName(user.getFirstName() + " " + user.getLastName());
            userDetailsDTO.setFirstName(user.getFirstName());
            userDetailsDTO.setLastName(user.getLastName());
            userDetailsDTO.setPhone(user.getPhoneNumber());
            userDetailsDTO.setCode(user.getCountryCode());
            userDetailsDTO.setRoleName(user.getRoleName());
            userDetailsDTO.setId(user.getUserId());
            userDetailsDTO.setCompanyId(user.getCompanyId());
            userDetailsDTO.setConsultancyId(user.getConsultancyId());
            userDetailsDTO.setDepartmentId(user.getDepartmentId());
            userDetailsDTO.setEmailVerified(user.isEnabled());

            List<UserRole> userRoles = userRoleRepository.findByUserId(user.getUserId());
            userRoles.forEach(userrole -> {
                Optional<Role> optional = getRoleRepository().findById(userrole.getRoleId());
                if (optional.isPresent()) {
                    List role = new ArrayList<String>();
                    role.add(optional.get().getName());
                    userDetailsDTO.setRoles(role);
                }
            });
            userDetailsDTO.setEnabledRole(userRoles.get(0).isEnabled());
            try {
                CompanyDetailsDTO companyDetailsDTO = getCompanyService().getCompany(user.getCompanyId());
                userDetailsDTO.setCompanyName(companyDetailsDTO.getCompanyName());
                userDetailsDTO.setCompanyLocation(companyDetailsDTO.getLocation());
                userDetailsDTO.setProfileDetailsNotPresent(userDetailsDTO.getCompanyLocation() == null || userDetailsDTO.getCompanyLocation().equals(""));
//                if (type != null && userDetailsDTO.getRoles().contains("CONSULTANCY_ADMIN") && type.equals("CONSULTANCY_MANAGER")) {
//                    throw new AuthenticationCredentialsNotFoundException("Invalid username or password");
//                }
//                if (type != null && userDetailsDTO.getRoles().contains("COMPANY_ADMIN") && type.equals("HR_MANAGER")) {
//                    throw new AuthenticationCredentialsNotFoundException("Invalid username or password");
//                }
//                if (type != null && userDetailsDTO.getRoles().contains("CONSULTANCY_ADMIN") && type.equals("COMPANY_ADMIN")) {
//                    userDetailsDTO.setEnableAsConsultant(true);
//                    List role = new ArrayList<String>();
//                    role.add("COMPANY_ADMIN");
//                    userDetailsDTO.setRoles(role);
//                    if (userDetailsDTO.getCompanyLocation() == null || userDetailsDTO.getCompanyLocation().equals("")) {
//                        userDetailsDTO.setProfileDetailsNotPresent(true);
//                    } else {
//                        userDetailsDTO.setProfileDetailsNotPresent(false);
//                    }
//
//                } else if (type != null && userDetailsDTO.getRoles().contains("COMPANY_ADMIN") && type.equals("CONSULTANCY_ADMIN")) {
//                    userDetailsDTO.setEnableAsConsultant(true);
//                    List role = new ArrayList<String>();
//                    role.add("CONSULTANCY_ADMIN");
//                    userDetailsDTO.setRoles(role);
//                    ConsultancyDetailsDTO consultancyDetailsDTO = getConsultancyService().getConsultancyDetails(user.getConsultancyId());
//                    if (consultancyDetailsDTO.getLocation() == null || consultancyDetailsDTO.getLocation().equals("")) {
//                        userDetailsDTO.setProfileDetailsNotPresent(true);
//                    } else {
//                        userDetailsDTO.setProfileDetailsNotPresent(false);
//                    }
//                } else if (type != null && type.equals("CONSULTANCY_ADMIN")) {
//                    ConsultancyDetailsDTO consultancyDetailsDTO = getConsultancyService().getConsultancyDetails(user.getConsultancyId());
//                    if (consultancyDetailsDTO.getLocation() != null) {
//                        userDetailsDTO.setProfileDetailsNotPresent(false);
//                        userDetailsDTO.setCompanyLocation(consultancyDetailsDTO.getLocation());
//                    } else {
//                        userDetailsDTO.setProfileDetailsNotPresent(true);
//                    }
//                }

            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (WorkruitException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new UsernamePasswordAuthenticationToken(userDetailsDTO, null);
        } else {
            throw new AuthenticationCredentialsNotFoundException("Invalid username or password");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.security.authentication.AuthenticationProvider#supports(
     * java.lang.Class)
     */
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public CompanyService getCompanyService() {
        return companyService;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public ConsultancyService getConsultancyService() {
        return consultancyService;
    }

    public void setConsultancyService(ConsultancyService consultancyService) {
        this.consultancyService = consultancyService;
    }

    /**
     * @return the userRoleRepository
     */
    public UserRoleRepository getUserRoleRepository() {
        return userRoleRepository;
    }

    /**
     * @param userRoleRepository the userRoleRepository to set
     */
    public void setUserRoleRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * @return the roleRepository
     */
    public RoleRepository getRoleRepository() {
        return roleRepository;
    }

    /**
     * @param roleRepository the roleRepository to set
     */
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * @return the applicantService
     */
    public ApplicantService getApplicantService() {
        return applicantService;
    }

    /**
     * @param applicantService the applicantService to set
     */
    public void setApplicantService(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

}
