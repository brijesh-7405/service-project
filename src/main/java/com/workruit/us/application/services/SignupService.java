/**
 *
 */
package com.workruit.us.application.services;

import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.ApplicantSignupDTO;
import com.workruit.us.application.dto.RoleUpdateDTO;
import com.workruit.us.application.dto.SignupDTO;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Santosh Bhima
 */
@Slf4j
@Service
public class SignupService {

    private @Autowired PasswordEncoder passwordEncoder;

    private @Autowired UserRepository userRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired CompanyRepository companyRepository;
    private @Autowired EmailService emailService;
    private @Autowired UserVerificationRepository userVerificationRepository;

    private @Autowired MobileVerificationRepository mobileVerificationRepository;
    private @Autowired RoleRepository roleRepository;
    private @Autowired UserRoleRepository userRoleRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired CompanyService companyService;
    private @Autowired ConsultancyService consultancyService;

    private @Autowired ApplicantVerificationRepository applicantVerificationRepository;

    @Value("${base.url}")
    private String baseUrl;

    public Long saveApplicant(ApplicantSignupDTO signupDTO) throws AuthenticationException {
        Applicant applicant = new Applicant();

        /**
         * Validate email and check for existing email.
         */
        log.debug("Validating user email:{}", signupDTO.getEmail());
        if (StringUtils.isNotBlank(signupDTO.getEmail())) {
            Pattern pattern = Pattern.compile(CommonConstants.EMAIL_REGEX);
            Matcher matcher = pattern.matcher(signupDTO.getEmail());
            if (!matcher.matches()) {
                throw new AuthenticationException("Please enter valid email format");
            }
        }
        log.debug("Checking for existing user with email:{}", signupDTO.getEmail());
        User existingUser = userRepository.findByWorkEmail(signupDTO.getEmail());
        if (existingUser != null) {
            throw new AuthenticationException("Already existing user with the same email.");
        }

        /**
         * Validation of password
         */
        if (StringUtils.isBlank(signupDTO.getPassword()) || StringUtils.isBlank(signupDTO.getConfirmPassword())) {
            throw new AuthenticationException("Password or Confirm Password cannot be empty.");
        }

        log.debug("Checking whether the password matches the confirm password");
        if (StringUtils.isNotBlank(signupDTO.getPassword()) && StringUtils.isNotBlank(signupDTO.getConfirmPassword())) {
            if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
                throw new AuthenticationException("Password & Confirm Password are not same.");
            }
        }

        applicant.setFirstName(signupDTO.getFirstName());
        applicant.setLastName(signupDTO.getLastName());
        applicant.setEmail(signupDTO.getEmail());
        applicant.setPhoneNumber(signupDTO.getPhoneNumber());
        applicant.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        applicant.setCreatedDate(new Date());
        applicant.setUpdatedDate(new Date());
        applicant.setCountry(signupDTO.getCountry());
        applicant.setSkills(signupDTO.getSkills());
        applicantRepository.save(applicant);

        String random = RandomStringUtils.random(6, false, true);

        UserVerification userVerification = new UserVerification();
        userVerification.setOtp(random);
        userVerification.setUserId(applicant.getApplicantId());
        userVerificationRepository.save(userVerification);

        String content = baseUrl + "/redirect/?to=" + "/signup/applicant/" + applicant.getApplicantId() + "/"
                + random;
        try {
            emailService.sendMail(applicant.getFirstName() + " " + applicant.getLastName(), applicant.getEmail(),
                    "resume-verify-email", null, content);
        } catch (MandrillApiError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return applicant.getApplicantId();

    }

    @Transactional
    public Long updateUserRole(RoleUpdateDTO roleUpdateDTO) throws Exception {
        Role role = roleRepository.findByName(roleUpdateDTO.getRole());
        if (role == null) {
            throw new Exception("Role not found");
        }
        List<UserRole> userRoles = userRoleRepository.findByUserId(roleUpdateDTO.getUserId());
        Long roleId = role.getRoleId();
        if (userRoles != null && userRoles.size() > 0) {
            UserRole userRole = userRoles.get(0);
            userRole.setUpdatedDate(new Date());
            userRole.setEnabled(true);
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        } else {
            UserRole userRole = new UserRole();
            userRole.setCreatedDate(new Date());
            userRole.setUpdatedDate(new Date());
            userRole.setEnabled(true);
            userRole.setUserId(roleUpdateDTO.getUserId());
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        }
        return roleId;
    }

    @Transactional
    public Long saveUser(SignupDTO signupDTO) throws Exception {

        User user = new User();

        /**
         * Validate email and check for existing email.
         */
        log.debug("Validating user email:{}", signupDTO.getEmail());
        if (StringUtils.isNotBlank(signupDTO.getEmail())) {
            Pattern pattern = Pattern.compile(CommonConstants.EMAIL_REGEX);
            Matcher matcher = pattern.matcher(signupDTO.getEmail());
            if (!matcher.matches() || signupDTO.getEmail().endsWith("gmail.com")
                    || signupDTO.getEmail().endsWith("yahoo.com") || signupDTO.getEmail().endsWith("hotmail.com")) {
                throw new AuthenticationException("Please enter valid email format/work email");
            }
        }
        log.debug("Checking for existing user with email:{}", signupDTO.getEmail());
        User existingUser = userRepository.findByWorkEmail(signupDTO.getEmail());
        if (existingUser != null) {
            throw new AuthenticationException("Already existing user with the same email.");
        }

        User existingUserMobileNumberCheck = userRepository.findByPhoneNumber(signupDTO.getPhoneNumber());
        if (existingUserMobileNumberCheck != null) {
            throw new AuthenticationException("Already existing user with the same Mobile Number.");
        }

        /**
         * Validation of password
         */
        if (StringUtils.isBlank(signupDTO.getPassword()) || StringUtils.isBlank(signupDTO.getConfirmPassword())) {
            throw new AuthenticationException("Password or Confirm Password cannot be empty.");
        }

        log.debug("Checking whether the password matches the confirm password");
        if (StringUtils.isNotBlank(signupDTO.getPassword()) && StringUtils.isNotBlank(signupDTO.getConfirmPassword())) {
            if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
                throw new AuthenticationException("Password & Confirm Password are not same.");
            }
        }

        user.setFirstName(signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName());
        user.setWorkEmail(signupDTO.getEmail());
        user.setPhoneNumber(signupDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        user.setCreatedDate(new Date());
        user.setUpdatedDate(new Date());
        user.setCountryCode(signupDTO.getCountryCode());
        user.setRoleName(signupDTO.getRoleName());
        user.setNotificationToken(signupDTO.getNotificationToken());

        Company company = new Company();
        company.setName(signupDTO.getCompanyName());
        company.setCreatedDate(new Date());
        company.setUpdatedDate(new Date());
        companyRepository.save(company);

        Consultancy consultancy = new Consultancy();
        consultancy.setName(signupDTO.getCompanyName());
        consultancy.setCreatedDate(new Date());
        consultancy.setUpdatedDate(new Date());
        consultancyRepository.save(consultancy);

        user.setConsultancyId(consultancy.getConsultancyId());
        user.setCompanyId(company.getCompanyId());
        userRepository.save(user);

        Role role = roleRepository.findByName(signupDTO.getRole());
        if (role == null) {
            throw new Exception("Role not found");
        }
        Long roleId = role.getRoleId();
        UserRole userRole = new UserRole();
        userRole.setCreatedDate(new Date());
        userRole.setUpdatedDate(new Date());
        userRole.setUserId(user.getUserId());
        userRole.setEnabled(false);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);

        String random = RandomStringUtils.random(6, false, true);

        UserVerification userVerification = new UserVerification();
        userVerification.setOtp(random);
        userVerification.setUserId(user.getUserId());
        userVerification.setConsultancyId(consultancy.getConsultancyId());
        userVerification.setCreatedDate(new Date());
        userVerification.setUpdatedDate(new Date());
        userVerificationRepository.save(userVerification);


        String content = baseUrl + "/redirect/?to=auth/verification/" + role.getName() + "/" + user.getUserId() + "/"
                + random;
        try {
            List<MergeVar> globalMergeVars = new ArrayList();
            MergeVar mergeVar = new MergeVar();
            mergeVar.setName("name");
            mergeVar.setContent(signupDTO.getFirstName() + " " + signupDTO.getLastName());
            globalMergeVars.add(mergeVar);
            mergeVar = new MergeVar();
            mergeVar.setName("verifyurl");
            mergeVar.setContent(content);
            globalMergeVars.add(mergeVar);

            emailService.sendMail(user.getFirstName() + " " + user.getLastName(), user.getWorkEmail(),
                    "resume-verify-email", globalMergeVars, content);
        } catch (MandrillApiError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user.getUserId();
    }

    public boolean verifyApplicant(Long userId, String code) {
        ApplicantVerification applicantVerification = applicantVerificationRepository.findByApplicantIdAndOtp(userId,
                code);
        if (applicantVerification == null) {
            return false;
        } else {
            Optional<Applicant> optionalUser = applicantRepository.findById(userId);
            if (optionalUser.isPresent()) {
                optionalUser.get().setEnabled(true);
                applicantRepository.save(optionalUser.get());
                applicantVerificationRepository.delete(applicantVerification);
            }
            return true;
        }
    }

    public User verifyUser(Long userId, String code) {
        UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(userId, code);
        if (userVerification == null) {
            return null;
        } else {
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                optionalUser.get().setEnabled(true);
                optionalUser.get().setEnabledDate(new Date());
                userRepository.save(optionalUser.get());
                userVerificationRepository.delete(userVerification);
            }
            return optionalUser.get();
        }
    }


    @Transactional
    public void sendOtp(String mobileNumber) throws AuthenticationException {
        /**
         * Validate phone and check for existing phone.
         */
        log.debug("Sending OTP for mobile:{}", mobileNumber);
        if (StringUtils.isNotBlank(mobileNumber)) {
            Pattern pattern = Pattern.compile(CommonConstants.MOBILE_REGEX);
            Matcher matcher = pattern.matcher(mobileNumber);
            if (!matcher.matches()) {
                throw new AuthenticationException("Please enter valid mobile number format");
            }
        }

        // Generate OTP using a SDK

        MobileVerification mobileVerification = new MobileVerification();
        mobileVerification.setMobileNumber(mobileNumber);
        mobileVerification.setOtp("test");

        mobileVerificationRepository.save(mobileVerification);
    }
}
