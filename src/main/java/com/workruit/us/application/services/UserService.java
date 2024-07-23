/**
 *
 */
package com.workruit.us.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.models.*;
import com.workruit.us.application.notification.NotificationService;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Santosh Bhima
 */
@Slf4j
@Component
public class UserService {

    private @Autowired PasswordEncoder passwordEncoder;
    private @Autowired UserRepository userRepository;
    private @Autowired EmailService emailService;
    private @Autowired UserVerificationRepository userVerificationRepository;

    private @Autowired RoleRepository roleRepository;
    private @Autowired UserRoleRepository userRoleRepository;
    private @Autowired DepartmentRepository departmentRepository;

    private @Autowired AlertService alertService;

    private @Autowired FirebaseMessagingService firebaseMessagingService;

    private @Autowired NotificationService notificationService;
    private @Autowired MessageSource messageSource;
    private @Autowired ImageService imageService;
    private @Autowired CompanyService companyService;


    @Value("${base.url}")
    private String baseUrl;

    public User login(String username, String password, Object token) {
        if (username.endsWith("gmail.com")
                || username.endsWith("yahoo.com") || username.endsWith("hotmail.com")) {
            throw new AuthenticationCredentialsNotFoundException("Please enter work email");
        }
        User user = userRepository.findByWorkEmail(username);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException(messageSource.getMessage("account.exist.fail.description", new Object[]{}, null));
        }
        if (!user.isEnabled()) {
            throw new AuthenticationCredentialsNotFoundException(messageSource.getMessage("user.email.verified.fail.description", new Object[]{}, null));
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            throw new AuthenticationCredentialsNotFoundException(messageSource.getMessage("signin.fail.description", new Object[]{}, null));
        }
    }

    public UserDetailsDTO getUserInfo(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = null;
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
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
                Optional<Role> optional = roleRepository.findById(userrole.getRoleId());
                if (optional.isPresent()) {
                    List role = new ArrayList<String>();
                    role.add(optional.get().getName());
                    userDetailsDTO.setRoles(role);
                }
            });
            userDetailsDTO.setEnabledRole(userRoles.get(0).isEnabled());

            CompanyDetailsDTO companyDetailsDTO = null;
            try {
                companyDetailsDTO = companyService.getCompany(user.getCompanyId());

            } catch (WorkruitException e) {
                throw new RuntimeException(e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            userDetailsDTO.setCompanyName(companyDetailsDTO.getCompanyName());
            userDetailsDTO.setCompanyLocation(companyDetailsDTO.getLocation());
            userDetailsDTO.setCompanyImage(companyDetailsDTO.getProfileImageData());
        }

        return userDetailsDTO;
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

    public User getUser(Long userId, String code) {
//        UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(userId, code);
//        if (userVerification == null) {
//            return null;
//        } else {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.get();

    }

    public InvitedUsersCountDTO getInvitedUsersCount(Long consultancyId, Long createdBy, String role, int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber, pageSize);
        Page<User> usersCount, pendingUsersCount;
        if (role.equals("COMPANY_ADMIN")) {

            usersCount = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, createdBy, "HR_MANAGER",
                    page);
            pendingUsersCount = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, false,
                    createdBy, "HR_MANAGER", page);

        } else {
            usersCount = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, createdBy, "CONSULTANCY_MANAGER",
                    page);
            pendingUsersCount = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, false,
                    createdBy, "CONSULTANCY_MANAGER", page);
        }


        InvitedUsersCountDTO invitedUsersCountDTO = new InvitedUsersCountDTO();
        invitedUsersCountDTO.setActivatedUsersCount(usersCount.getTotalElements());
        invitedUsersCountDTO.setPendingUsesCount(pendingUsersCount.getTotalElements());

        return invitedUsersCountDTO;
    }

    public InvitedUsedDTO getUsers(Long consultancyId, Long createdBy, String role, int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber, pageSize);
        Page<User> users = null;

        if (role.equals("COMPANY_ADMIN")) {
            users = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, createdBy, "HR_MANAGER",
                    page);
        } else {
            users = userRepository.findByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, createdBy, "CONSULTANCY_MANAGER",
                    page);
        }


        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users.getContent()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setFirstName(user.getFirstName());
            userDTO.setLastName(user.getLastName());
            userDTO.setEmail(user.getWorkEmail());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            if (user.getDepartmentId() != null) {
                userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
            }
            userDTO.setUserId(user.getUserId());
            userDTOs.add(userDTO);
        }
        InvitedUsedDTO response = new InvitedUsedDTO();
        response.setUsers(userDTOs);
        response.setTotalCount(users.getTotalElements());
        response.setTotalPages(users.getTotalPages());
        return response;
    }

    public InvitedUsedDTO getCollabratorUsers(Long consultancyId, Long userId, String role, int pageNumber, int pageSize) throws IOException {
        Pageable page = PageRequest.of(pageNumber, pageSize);

        User userRequested = userRepository.getById(userId);
        Page<User> users = null;
        List<UserDTO> userDTOs = new ArrayList<>();

        if (userRequested.getCreatedBy() == null) {

            if (role.equals("COMPANY_ADMIN")) {
                users = userRepository.findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, userId, "HR_MANAGER",
                        page);
            } else {
                users = userRepository.findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, userId, "CONSULTANCY_MANAGER",
                        page);
            }

            for (User user : users.getContent()) {
                String imageUrl = companyService.getCompanyImage(user.getCompanyId());
                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(user.getFirstName());
                userDTO.setLastName(user.getLastName());
                userDTO.setEmail(user.getWorkEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                userDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
                if (user.getDepartmentId() != null) {
                    userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                userDTO.setUserId(user.getUserId());
                userDTOs.add(userDTO);
            }
        } else {
            if (role.equals("COMPANY_ADMIN")) {
                users = userRepository.findSameDepartmentUsersIdsByUserIdAndEnabled(userRequested.getConsultancyId(), true, userRequested.getDepartmentId(), userRequested.getUserId()
                        , "HR_MANAGER", page);
            } else {
                users = userRepository.findSameDepartmentUsersIdsByUserIdAndEnabled(userRequested.getConsultancyId(), true, userRequested.getDepartmentId(), userRequested.getUserId()
                        , "HR_MANAGER", page);
            }
            for (User user : users.getContent()) {
                String imageUrl = companyService.getCompanyImage(user.getCompanyId());
                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(user.getFirstName());
                userDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
                userDTO.setLastName(user.getLastName());
                userDTO.setEmail(user.getWorkEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                if (user.getDepartmentId() != null) {
                    userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                userDTO.setUserId(user.getUserId());
                userDTOs.add(userDTO);
            }
        }
        InvitedUsedDTO response = new InvitedUsedDTO();
        response.setUsers(userDTOs);
        response.setTotalCount(users.getTotalElements());
        response.setTotalPages(users.getTotalPages());
        return response;
    }

    public List<UserDTO> getCollabUsersforSearch(Long consultancyId, Long userId, String searchText, String role) {


        User userRequested = userRepository.getById(userId);
        List<User> users = null;
        List<UserDTO> userDTOs = new ArrayList<>();

        if (userRequested.getCreatedBy() == null) {
            if (role.equals("COMPANY_ADMIN")) {
                users = userRepository.findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, userId, searchText, "HR_MANAGER");
            } else {
                users = userRepository.findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(consultancyId, true, userId, searchText, "CONSULTANCY_MANAGER");
            }
            for (User user : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(user.getFirstName());
                userDTO.setLastName(user.getLastName());
                userDTO.setEmail(user.getWorkEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                if (user.getDepartmentId() != null) {
                    userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                userDTO.setUserId(user.getUserId());
                userDTOs.add(userDTO);
            }
        } else {
            if (role.equals("HR_MANAGER")) {
                users = userRepository.findSameDepartmentUsersIdsByUserIdAndEnabled(userRequested.getConsultancyId(), true, userRequested.getDepartmentId(), userRequested.getUserId(),
                        searchText, "HR_MANAGER");
            } else {
                users = userRepository.findSameDepartmentUsersIdsByUserIdAndEnabled(userRequested.getConsultancyId(), true, userRequested.getDepartmentId(), userRequested.getUserId(),
                        searchText, "CONSULTANCY_MANAGER");
            }
            for (User user : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setFirstName(user.getFirstName());
                userDTO.setLastName(user.getLastName());
                userDTO.setEmail(user.getWorkEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                if (user.getDepartmentId() != null) {
                    userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
                }
                userDTO.setUserId(user.getUserId());
                userDTOs.add(userDTO);
            }
        }
//        InvitedUsedDTO response = new InvitedUsedDTO();
//        response.setUsers(userDTOs);
//        response.setTotalCount(users.getTotalElements());
//        response.setTotalPages(users.getTotalPages());
        return userDTOs;
//        List<User> users = userRepository.getCompanyInfoByUserList(companyId, searchText);
//        List<UserDTO> userDTOs = new ArrayList<>();
//        for (User user : users) {
//            UserDTO userDTO = new UserDTO();
//            userDTO.setFirstName(user.getFirstName());
//            userDTO.setLastName(user.getLastName());
//            userDTO.setEmail(user.getWorkEmail());
//            if (user.getDepartmentId() != null) {
//                userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
//            }
//            userDTO.setUserId(user.getUserId());
//            userDTOs.add(userDTO);
//        }
//        return userDTOs;
    }

    public PendingUsersDTO getPendingUsers(Long consultancyId, Long userId, String role, Integer pageNo, Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<UserVerification> userVerifications = userVerificationRepository.findUsersByConsultancyId(consultancyId, userId, role,
                pageable);
        List<BasicSignupDTO> basicSignupDTOs = new ArrayList<>();
        for (UserVerification userVerification : userVerifications.getContent()) {
            BasicSignupDTO basicSignupDTO = new BasicSignupDTO();
            User user = userRepository.findById(userVerification.getUserId()).get();
            basicSignupDTO.setEmail(user.getWorkEmail());
            if (user.getDepartmentId() != null) {
                basicSignupDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
            }
            long current = System.currentTimeMillis();
            long created = userVerification.getUpdatedDate().getTime();
            Duration duration = Duration.ofMillis(current - created);
            if (duration.toHours() > 48) {
                basicSignupDTO.setExpires(null);
            } else {
                TimeZone tz = TimeZone.getDefault();
                int offset = (int) TimeUnit.MILLISECONDS.toHours(tz.getOffset(System.currentTimeMillis()));
                basicSignupDTO.setExpires(String.valueOf(48 - duration.toHours() - offset));
            }
            basicSignupDTO.setMobileNumber(user.getPhoneNumber());
            basicSignupDTO.setUserVerificationId(userVerification.getUserVerificationId());
            basicSignupDTO.setInvitedCount(userVerification.getResendInvite());
            basicSignupDTOs.add(basicSignupDTO);

        }
        PendingUsersDTO response = new PendingUsersDTO();
        response.setTotalCount(userVerifications.getTotalElements());
        response.setTotalPages(userVerifications.getTotalPages());
        response.setBasicSignupDTO(basicSignupDTOs);
        return response;
    }

    public void resendInvite(Long verificationId, UserDTO userDTO, String roleName) throws MandrillApiError, IOException, AuthenticationException {
        String newOtp = RandomStringUtils.random(6, false, true);
        UserVerification userVerification = userVerificationRepository.findById(verificationId).get();


        long current = System.currentTimeMillis();
        long created = userVerification.getCreatedDate().getTime();
        Duration duration = Duration.ofMillis(current - created);
        if (duration.toHours() > 48) {
            userVerification.setResendInvite(userVerification.getResendInvite() + 1);
            UserVerification exsitingUserVerification = userVerificationRepository.findByUserId(userVerification.getUserId());
            if (exsitingUserVerification != null) {
                userVerificationRepository.delete(exsitingUserVerification);
            }

        } else {
            if (userDTO.getEmail().endsWith("gmail.com")
                    || userDTO.getEmail().endsWith("yahoo.com") || userDTO.getEmail().endsWith("hotmail.com")) {
                throw new AuthenticationCredentialsNotFoundException("Please enter work email");
            }
            User existingUser = userRepository.findByWorkEmail(userDTO.getEmail());
            if (existingUser != null) {
                throw new AuthenticationException("Already existing user with the same email.");
            }
        }
        //userVerification.setOtp(newOtp);
        userVerification.setCreatedDate(new Date());
        userVerification.setUpdatedDate(new Date());
        userVerification.setRoleName(roleName);

        userVerificationRepository.save(userVerification);
        User user = userRepository.findById(userVerification.getUserId()).get();
        Department department = departmentRepository.findByName(userDTO.getDepartment());
        if (department != null) {
            user.setDepartmentId(department.getDepartmentId());
        }

        user.setWorkEmail(userDTO.getEmail());
        userRepository.save(user);

        String content = baseUrl + "/redirect/?to=/auth/verification/user/" + user.getUserId() + "/" + userVerification.getOtp() + "/create_password/";
        List<MergeVar> globalMergeVars = new ArrayList();
        MergeVar mergeVar = new MergeVar();
        mergeVar.setName("name");
        mergeVar.setContent(user.getFirstName() + " " + user.getLastName());
        globalMergeVars.add(mergeVar);
        mergeVar = new MergeVar();
        mergeVar.setName("verifyurl");
        mergeVar.setContent(content);
        globalMergeVars.add(mergeVar);
        emailService.sendMail(user.getFirstName() + " " + user.getLastName(), user.getWorkEmail(),
                "resume-verify-email", globalMergeVars, content);
    }

    public int verifyUserWithPassword(Long userId, String code, PasswordDTO passwordDTO) throws WorkruitException, FirebaseMessagingException, AuthenticationException {

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && !optionalUser.get().getFirstName().isEmpty()) {
            return -2;
        }
        UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(userId, code);

        if (userVerification != null) {
            long current = System.currentTimeMillis();
            long created = userVerification.getCreatedDate().getTime();
            Duration duration = Duration.ofMillis(current - created);
            if (duration.toHours() > 48) {
                {
                    throw new AuthenticationException("link expired please contact admin.");
                }
            }
        }
        if (userVerification == null) {
            return -1;
        } else {
            if (StringUtils.equals(passwordDTO.getPassword(), passwordDTO.getConfirmPassword())) {
                User user = optionalUser.get();
                user.setEnabled(true);
                user.setUpdatedDate(new Date());
                user.setPassword(passwordEncoder.encode(passwordDTO.getPassword()));
                user.setEnabledDate(new Date());
                userRepository.save(user);
                userVerificationRepository.delete(userVerification);

            } else {
                throw new WorkruitException("Password & ConfirmPassword not same");
            }
        }
        return 1;

    }

    @Transactional
    public UserDTO updateUser(UserDTO userDTO, long userId) throws AuthenticationException, FirebaseMessagingException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());

            if (userDTO.getPhoneNumber() != null) {
                User existingUserMobileNumberCheck = userRepository.findByPhoneNumber(userDTO.getPhoneNumber());
                if (existingUserMobileNumberCheck != null) {
                    throw new AuthenticationException("Already existing user with the same Mobile Number.");
                }
                user.setPhoneNumber(userDTO.getPhoneNumber());
                user.setCountryCode(userDTO.getCountryCode());
            }
            if (userDTO.getNotificationToken() != null && !userDTO.getNotificationToken().equals(""))
                user.setNotificationToken(userDTO.getNotificationToken());
            userRepository.save(user);

            UserDTO userDto = new UserDTO();
            userDto.setCompanyId(user.getCompanyId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setRoleName(user.getRoleName());
            userDto.setUserId(user.getUserId());
            userDto.setCountryCode(user.getCountryCode());
            userDto.setConsultancyId(user.getConsultancyId());
            Department department = departmentRepository.findByDepartmentId(user.getDepartmentId());
            if (department != null) {
                userDto.setDepartment(department.getName());
            } else {
                userDto.setDepartment("NA");
            }
            userDto.setEmail(user.getWorkEmail());
            userDto.setPhoneNumber(user.getPhoneNumber());


            String message = user.getFirstName() + " " + user.getLastName() + " successfully activated their account.";
            alertService.saveAlertInfo(user.getCreatedBy(), message, user.getConsultancyId());

            String notificationMessage = user.getFirstName() + " " + user.getLastName() + " successfully activated their account.";
            List<Long> roleIds = Arrays.asList((long) 1, (long) 2);
            User mainConsultant = userRepository.findMainConsultancyUser(user.getConsultancyId(), roleIds);
            if (mainConsultant.getNotificationToken() != null && !mainConsultant.getNotificationToken().equals("")) {
                firebaseMessagingService.prepareNotifObject("Account activated", notificationMessage, mainConsultant.getNotificationToken());
                notificationService.saveNotification(mainConsultant.getUserId(), mainConsultant.getConsultancyId(), "Account activated", notificationMessage);
            }

            return userDto;
        } else {
            throw new AuthenticationException("User does not exist");
        }
    }

    @Transactional
    public Long createUser(UserDTO signupDTO, long consultancyId, long createdBy, String roleName) throws AuthenticationException {
        User user = new User();
        /**
         * Validate email and check for existing email.
         */
        log.debug("Validating user email:{}", signupDTO.getEmail());
        if (signupDTO.getEmail().endsWith("gmail.com")
                || signupDTO.getEmail().endsWith("yahoo.com") || signupDTO.getEmail().endsWith("hotmail.com")) {
            throw new AuthenticationCredentialsNotFoundException("Please enter work email");
        }
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

        User existingMobileUser = userRepository.findByPhoneNumber(signupDTO.getPhoneNumber());
        if (existingMobileUser != null) {
            throw new AuthenticationException("Already existing user with the same mobile number.");
        }

        user.setFirstName(signupDTO.getFirstName() == null ? "" : signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName() == null ? "" : signupDTO.getLastName());
        user.setWorkEmail(signupDTO.getEmail());
        user.setRoleName(signupDTO.getRoleName());
        user.setPhoneNumber(signupDTO.getPhoneNumber() == null ? "" : signupDTO.getPhoneNumber());
        user.setCreatedDate(new Date());
        user.setUpdatedDate(new Date());
        user.setPassword(UUID.randomUUID().toString());
        user.setConsultancyId(consultancyId);
        user.setCompanyId(signupDTO.getCompanyId());
        user.setCreatedBy(createdBy);
        if (signupDTO.getNotificationToken() != null && !signupDTO.getNotificationToken().equals(""))
            user.setNotificationToken(signupDTO.getNotificationToken());
        Department department = departmentRepository.findByName(signupDTO.getDepartment());
        if (department != null) {
            user.setDepartmentId(department.getDepartmentId());
        }

        userRepository.save(user);

//		Iterable<Role> roles = roleRepository.findAll();
//		roles.forEach(role -> {
//			Long roleId = role.getRoleId();
//			UserRole userRole = new UserRole();
//			userRole.setCreatedDate(new Date());
//			userRole.setUpdatedDate(new Date());
//			userRole.setUserId(user.getUserId());
//			userRole.setRoleId(roleId);
//			userRoleRepository.save(userRole);
//		});

        Role role = roleRepository.findByName(signupDTO.getRoleName());
        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setCreatedDate(new Date());
            userRole.setUpdatedDate(new Date());
            userRole.setUserId(user.getUserId());
            userRole.setRoleId(role.getRoleId());
            userRoleRepository.save(userRole);
        }

        String random = RandomStringUtils.random(6, false, true);

        UserVerification userVerification = new UserVerification();
        userVerification.setOtp(random);
        userVerification.setUserId(user.getUserId());
        userVerification.setCreatedDate(new Date());
        userVerification.setUpdatedDate(new Date());
        userVerification.setResendInvite(1);
        userVerification.setRoleName(roleName);
        userVerification.setConsultancyId(consultancyId);
        userVerificationRepository.save(userVerification);

        String content = baseUrl + "/redirect/?to=/auth/verification/user/" + user.getUserId() + "/" + random
                + "/create_password/";

        try {
            List<MergeVar> globalMergeVars = new ArrayList();
            MergeVar mergeVar = new MergeVar();
            mergeVar.setName("name");
            mergeVar.setContent(user.getFirstName() + " " + user.getLastName());
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

    @Transactional
    public void changePassword(ChangePasswordDTO forgotPasswordDTO, Long userId) throws AuthenticationException {

        User user = userRepository.findById(userId).get();

        if (!passwordEncoder.matches(forgotPasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new AuthenticationException("Current password is not correct");
        }

        if (user.getPassword().equals(forgotPasswordDTO.getConfirmPassword())) {
            throw new AuthenticationException("Password and Confirm password should not be same");
        }

        /**
         * Validation of password
         */
        if (StringUtils.isBlank(forgotPasswordDTO.getPassword())
                || StringUtils.isBlank(forgotPasswordDTO.getConfirmPassword())) {
            throw new AuthenticationException("Password or Confirm Password cannot be empty.");
        }

        log.debug("Checking whether the password matches the confirm password");
        if (StringUtils.isNotBlank(forgotPasswordDTO.getPassword())
                && StringUtils.isNotBlank(forgotPasswordDTO.getConfirmPassword())) {
            if (!forgotPasswordDTO.getPassword().equals(forgotPasswordDTO.getConfirmPassword())) {
                throw new AuthenticationException("Password & Confirm Password are not same.");
            }
        }
        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordDTO forgotPasswordDTO, Long userId, String code)
            throws AuthenticationException {
        User user = userRepository.findById(userId).get();
        UserVerification userVerification = userVerificationRepository.findByUserIdAndOtp(userId, code);
        if (user == null || userVerification == null) {
            throw new AuthenticationException("Invalid code");
        }

        if (!StringUtils.equals(forgotPasswordDTO.getPassword(), forgotPasswordDTO.getConfirmPassword())) {
            throw new AuthenticationException("Password and Confirm password are not same");
        }

        user.setPassword(passwordEncoder.encode(forgotPasswordDTO.getPassword()));
        userRepository.save(user);
        userVerificationRepository.delete(userVerification);
    }

    @Transactional
    public void updateUserNotificationToken(Long userId, String notificationToken) {
        User user = userRepository.findById(userId).get();
        user.setNotificationToken(notificationToken);
        userRepository.save(user);
    }

    @Transactional
    public void forgotPasswordGenerateCode(String emailAddress) throws AuthenticationException {
        User user = userRepository.findByWorkEmail(emailAddress);
        if (user == null) {
            throw new AuthenticationException("User does not exist");
        }

        if (!user.isEnabled()) {
            throw new AuthenticationCredentialsNotFoundException(messageSource.getMessage("user.email.verified.fail.description", new Object[]{}, null));
        }

        UserVerification userVerification = new UserVerification();
        userVerification.setOtp(RandomStringUtils.randomAlphanumeric(6));
        userVerification.setUserId(user.getUserId());
        userVerification.setCreatedDate(new Date());
        userVerification.setUpdatedDate(new Date());
        userVerificationRepository.save(userVerification);

//        String content = baseUrl + "/redirect/?to=/auth/verification/user/" + user.getUserId() + "/"
//                + userVerification.getOtp() + "/create_password/";

        String content = baseUrl + "/redirect/?to=/auth/verification/user/" + user.getUserId() + "/"
                + userVerification.getOtp() + "/forgot_password/";


        try {
            List<MergeVar> globalMergeVars = new ArrayList();
            MergeVar mergeVar = new MergeVar();
            mergeVar.setName("name");
            mergeVar.setContent(user.getFirstName() + " " + user.getLastName());
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
    }

}
