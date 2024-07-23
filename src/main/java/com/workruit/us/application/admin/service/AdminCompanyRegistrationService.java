package com.workruit.us.application.admin.service;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantStatus;
import com.workruit.us.application.admin.enums.TimePeriod;
import com.workruit.us.application.admin.repository.AdminRegistrationRepositoryImpl;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.dto.ApplicantDTO;
import com.workruit.us.application.dto.InterviewDTO;
import com.workruit.us.application.dto.InterviewFeedbackDTO;
import com.workruit.us.application.dto.OfferDetailsDTO;
import com.workruit.us.application.enums.Currency;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.enums.JobType;
import com.workruit.us.application.enums.SalaryType;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import com.workruit.us.application.services.ActivityService;
import com.workruit.us.application.services.TalentViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class AdminCompanyRegistrationService {

    @Autowired
    private AdminRegistrationRepositoryImpl adminRegistrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobMatchConsultancyRepository jobMatchConsultancyRepository;

    @Autowired
    private TalentViewService talentViewService;
    private @Autowired InterviewRepository interviewRepository;
    private @Autowired ActivityService activityService;
    private @Autowired OfferDetailsRepository offerDetailsRepository;
    private @Autowired DepartmentRepository departmentRepository;

    public PaginationResponse getIncompleteCompanyRegistration(IncompleteRegistrationFilter incompleteRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) {

        Map<String, Object> result = adminRegistrationRepository.getIncompleteCompanyRegistration(incompleteRegistrationFilter, name, from, to, pageNumber, pageSize, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<IncompleteRegistrationDTO> incompleteRegistrationDTOs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse incompleteRegistrationResponse = new PaginationResponse();
        for (Object[] row : data) {
            IncompleteRegistrationDTO incompleteRegistrationDTO = new IncompleteRegistrationDTO();
            incompleteRegistrationDTO.setId(((BigInteger) row[0]).longValue());
            incompleteRegistrationDTO.setName((row[1]).toString());
            incompleteRegistrationDTO.setSignupDate(sdf.format(((Date) row[2])));
            incompleteRegistrationDTO.setIsEmailVerified(((Byte) row[3]).intValue() == 1 ? "Yes" : "No");
            incompleteRegistrationDTO.setIsSignupCompleted("No");
            incompleteRegistrationDTO.setLocation((String) row[4]);
            incompleteRegistrationDTO.setIsRegistrationStep1Completed(row[4] != null ? "Yes" : "No");
            incompleteRegistrationDTO.setIsRegistrationStep2Completed(row[5] != null ? "Yes" : "No");
            incompleteRegistrationDTOs.add(incompleteRegistrationDTO);
        }
        incompleteRegistrationResponse.setData(incompleteRegistrationDTOs);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        incompleteRegistrationResponse.setTotalCount(totalCount);
        incompleteRegistrationResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return incompleteRegistrationResponse;
    }


    public PaginationResponse getCompleteCompanyRegistration(CompleteRegistrationFilter completeRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        Map<String, Object> result = adminRegistrationRepository.getCompleteCompanyRegistration(completeRegistrationFilter, name, from, to, pageNumber, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<CompleteCompanyRegistrationDTO> completeRegistrationDTOs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse completeRegistrationResponse = new PaginationResponse();
        for (Object[] row : data) {
            CompleteCompanyRegistrationDTO completeRegistrationDTO = new CompleteCompanyRegistrationDTO();
            completeRegistrationDTO.setCompanyId(((BigInteger) row[0]).longValue());
            completeRegistrationDTO.setCompanyName((String) row[1]);
            completeRegistrationDTO.setRegistrationDate(row[2] != null ? sdf.format(((Date) row[2])) : "");
            completeRegistrationDTO.setLocation((String) row[3]);
            completeRegistrationDTO.setIndustryType((String) row[4]);
            completeRegistrationDTO.setAccountStatus("ACTIVE");
            completeRegistrationDTO.setShortlisted(((BigInteger) row[5]).longValue());
            completeRegistrationDTO.setUnderInterview(((BigInteger) row[6]).longValue());
            completeRegistrationDTO.setUnderHire(((BigInteger) row[7]).longValue());
            completeRegistrationDTO.setUnderReject(((BigInteger) row[8]).longValue());
            completeRegistrationDTO.setRegisteredEmployer(((BigInteger) row[9]).longValue());
            completeRegistrationDTO.setPendingRegistrationEmployer(((BigInteger) row[10]).longValue());
            completeRegistrationDTO.setPendingJob(row[11] != null ? ((BigInteger) row[11]).longValue() : 0);
            completeRegistrationDTO.setActiveJob(row[12] != null ? ((BigInteger) row[12]).longValue() : 0);
            completeRegistrationDTO.setClosedJob(row[13] != null ? ((BigInteger) row[13]).longValue() : 0);
            completeRegistrationDTO.setVacancies(row[14] != null ? ((BigDecimal) row[14]).longValue() : 0);
            completeRegistrationDTO.setLastActive(row[15] != null ? sdf.format(((Date) row[15])) : "");
            completeRegistrationDTOs.add(completeRegistrationDTO);
        }
        completeRegistrationResponse.setData(completeRegistrationDTOs);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        completeRegistrationResponse.setTotalCount(totalCount);
        completeRegistrationResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return completeRegistrationResponse;
    }

    public UserDTO getCompanyMainHr(Long companyId)
            throws WorkruitException {
        Optional<Company> optionalCompany = companyRepository.findById(companyId);
        if (optionalCompany.isPresent()) {
            return generateCompanyUserDTO(userRepository.findMainCompanyHrUser(companyId));
        } else {
            throw new WorkruitException("Company not found for the given id:" + companyId);
        }
    }

    public UserDTO getHrManagerDetails(Long userId)
            throws WorkruitException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            return generateCompanyUserDTO(optionalUser.get());
        } else {
            throw new WorkruitException("User not found for the given id:" + userId);
        }
    }

    public PaginationResponse getCompanyHrManager(Long userId, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) throws WorkruitException {

        Map<String, Object> result = adminRegistrationRepository.getHrManagerOfCompany(userId, name, from, to, pageNumber, pageSize, period);
        List<UserDTO> companyUserDTOS = new ArrayList<>();
        PaginationResponse hrManagerResponse = new PaginationResponse();
        List<User> data = (List<User>) result.get("data");
        for (User user : data) {
            companyUserDTOS.add(generateCompanyUserDTO(user));
        }
        hrManagerResponse.setData(companyUserDTOS);
        long totalCount = (long) result.get("count");
        hrManagerResponse.setTotalCount(totalCount);
        hrManagerResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return hrManagerResponse;
    }

    private UserDTO generateCompanyUserDTO(User user) throws WorkruitException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        UserDTO companyUserDTO = new UserDTO();
        if (user != null) {
            companyUserDTO.setUserId(user.getUserId());
            companyUserDTO.setFirstName(user.getFirstName());
            companyUserDTO.setLastName(user.getLastName());
            companyUserDTO.setCreatedDate(sdf.format(user.getCreatedDate()));
            companyUserDTO.setIsActivated(user.isEnabled() ? "Yes" : "No");
            companyUserDTO.setIsClickedOnLink(user.isEnabled() ? "Yes" : "No");
            if (user.getEnabledDate() != null) {
                companyUserDTO.setEnabledAccountDate(sdf.format(user.getEnabledDate()));
            }
            if (user.getUpdatedDate() != null && user.getCreatedDate() != null) {
                companyUserDTO.setUpdatedProfile(user.getUpdatedDate().equals(user.getCreatedDate()) ? "No" : "Yes");
            }
            companyUserDTO.setWorkEmail(user.getWorkEmail());
            companyUserDTO.setContactNumber(user.getPhoneNumber());
            companyUserDTO.setRole(user.getRoleName());
            if (user.getDepartmentId() != null && departmentRepository.findById(user.getDepartmentId()).isPresent()) {
                companyUserDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
            }
        } else {
            throw new WorkruitException("User not found");
        }
        return companyUserDTO;
    }


    public PaginationResponse getCompanyJobs(Long userId, CompanyJobFilter companyJobFilter, String title, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Map<String, Object> result = adminRegistrationRepository.getCompanyJobs(userId, companyJobFilter, title, from, to, pageNumber, pageSize, sortBy, period);
        List<CompanyJobDTO> companyJobDTOs = new ArrayList<>();
        PaginationResponse companyJobResponse = new PaginationResponse();
        List<Object[]> data = (List<Object[]>) result.get("data");
        for (Object[] row : data) {
            JobPost jobPost = (JobPost) row[0];
            CompanyJobDTO companyJobDTO = new CompanyJobDTO();
            companyJobDTO.setJobPostId(jobPost.getJobPostId());
            companyJobDTO.setStatus(jobPost.getStatus());
            companyJobDTO.setJobTitle(jobPost.getTitle());
            companyJobDTO.setJobLocation(jobPost.getLocation());
            if (jobPost.getJobApplyBy() != null)
                companyJobDTO.setApplyBy(sdf.format(jobPost.getJobApplyBy()));
            companyJobDTO.setPostedDate(sdf.format(jobPost.getCreatedDate()));
            companyJobDTO.setVacancies(jobPost.getVacancies());
            companyJobDTO.setRelevantProfiles(((Long) row[1]).longValue());
            companyJobDTO.setInterestedProfiles(((Long) row[2]).longValue());
            companyJobDTO.setShortlistedProfiles(((Long) row[3]).longValue());
            companyJobDTO.setUnderInterviewProfiles(((Long) row[4]).longValue());
            companyJobDTO.setUnderHiredProfiles(((Long) row[5]).longValue());
            companyJobDTO.setUnderRejectedProfiles(((Long) row[6]).longValue());
            List<Long> userIds = new ArrayList<>();
            String[] collaboratorIds = null;
            if (jobPost.getCollaboratorId() != null && !jobPost.getCollaboratorId().isEmpty()) {
                collaboratorIds = jobPost.getCollaboratorId().split(",");
                for (String id : collaboratorIds) {
                    userIds.add(Long.parseLong(id.trim()));
                }
            }
            userIds.add(jobPost.getUserId());
            List<User> users = userRepository.findAllById(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUserId, Function.identity()));
            User jobOwner = userMap.get(jobPost.getUserId());
            if (jobOwner != null) {
                String firstName = jobOwner.getFirstName() != null ? jobOwner.getFirstName() : "";
                String lastName = jobOwner.getLastName() != null ? jobOwner.getLastName() : "";
                Map<Long, String> jobOwnerMap = new HashMap<>();
                jobOwnerMap.put(jobOwner.getUserId(), firstName + " " + lastName);
                companyJobDTO.setJobOwner(jobOwnerMap);
            }
            if (collaboratorIds != null && collaboratorIds.length > 0) {
                Map<Long, String> collaboratorMap = new HashMap<>();
                for (String id : collaboratorIds) {
                    User collaborator = userMap.get(Long.parseLong(id));
                    if (collaborator != null) {
                        String firstName = collaborator.getFirstName() != null ? collaborator.getFirstName() : "";
                        String lastName = collaborator.getLastName() != null ? collaborator.getLastName() : "";
                        collaboratorMap.put(collaborator.getUserId(), firstName + " " + lastName);
                    }
                }
                companyJobDTO.setCollaborator(collaboratorMap);
            }
            companyJobDTOs.add(companyJobDTO);
        }
        companyJobResponse.setData(companyJobDTOs);
        long totalCount = (long) result.get("count");
        companyJobResponse.setTotalCount(totalCount);
        companyJobResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return companyJobResponse;
    }

    public PaginationResponse getApplicantData(Long jobPostId, JobApplicantFilter jobApplicantFilter, String name, ApplicantStatus applicantStatus, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) throws WorkruitException {
        Map<String, Object> result = adminRegistrationRepository.getApplicantData(jobPostId, jobApplicantFilter, name, applicantStatus, from, to, pageNo, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse jobApplicantDataResponse = new PaginationResponse();
        List<JobApplicantDataDTO> jobApplicantDataDTOs = new ArrayList<>();
        for (Object[] row : data) {
            JobApplicantDataDTO jobApplicantDataDTO = new JobApplicantDataDTO();
            jobApplicantDataDTO.setApplicantId(((BigInteger) row[0]).longValue());
            jobApplicantDataDTO.setFirstName(row[1] != null ? row[1].toString() : "");
            jobApplicantDataDTO.setLastName(row[2] != null ? row[2].toString() : "");
            jobApplicantDataDTO.setConsultancyName(row[3] != null ? row[3].toString() : "");
            jobApplicantDataDTO.setPrimaryJobFunction(row[4] != null ? row[4].toString() : "");
            jobApplicantDataDTO.setSecondaryJobFunction(row[5] != null ? row[5].toString() : "");
            jobApplicantDataDTO.setLocation(row[6] != null ? row[6].toString() : "");

            List<String> updatedBy = new ArrayList<>();
            String updatedByRecruiterFirstName = row[7] != null ? row[7].toString() : "";
            String updatedByRecruiterLastName = row[8] != null ? row[8].toString() : "";
            updatedBy.add(updatedByRecruiterFirstName + " " + updatedByRecruiterLastName);
            String updatedByConsFirstName = row[9] != null ? row[9].toString() : "";
            String updatedByConsLastName = row[10] != null ? row[10].toString() : "";
            updatedBy.add(updatedByConsFirstName + " " + updatedByConsLastName);
            jobApplicantDataDTO.setStatusUpdatedBy(updatedBy.stream().collect(Collectors.joining(",")));
            if (row[11] != null)
                jobApplicantDataDTO.setDateOfLastUpdate(sdf.format((Date) row[11]));

            jobApplicantDataDTO.setProfileScore(((Integer) row[12]).intValue());

            if (applicantStatus == ApplicantStatus.SHORTLISTED) {
                if (((Integer) row[13]).intValue() == 1 && ((Integer) row[14]).intValue() == 1) {
                    jobApplicantDataDTO.setApplicantStatus("Matched");
                } else {
                    jobApplicantDataDTO.setApplicantStatus("Shortlisted");
                }
            } else {
                jobApplicantDataDTO.setApplicantStatus(InterviewStatus.getByValue(((BigInteger) row[15]).intValue()).toString());
            }
            jobApplicantDataDTOs.add(jobApplicantDataDTO);
        }
        jobApplicantDataResponse.setData(jobApplicantDataDTOs);
        long totalCount = (Long) result.get("count");
        jobApplicantDataResponse.setTotalCount(totalCount);
        jobApplicantDataResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return jobApplicantDataResponse;
    }

    public PaginationResponse getCompanyAlertData(Long companyId, List<Long> userIds, int pageNumber, int pageSize, Date from, Date to, TimePeriod period) {
        Map<String, Object> result = adminRegistrationRepository.getCompanyAlertData(companyId, userIds, pageNumber, pageSize, from, to, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a | dd MMM yyyy");
        PaginationResponse overallAlertResponse = new PaginationResponse();
        List<OverallAlertDTO> overallAlertDTOS = new ArrayList<>();
        for (Object[] row : data) {
            OverallAlertDTO overallAlertDTO = new OverallAlertDTO();
            overallAlertDTO.setMsg((row[0]).toString());
            overallAlertDTO.setCompanyName((row[1]).toString());
            overallAlertDTO.setAlertDate(sdf.format(((Date) row[2])));
            overallAlertDTOS.add(overallAlertDTO);
        }
        overallAlertResponse.setData(overallAlertDTOS);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        overallAlertResponse.setTotalCount(totalCount);
        overallAlertResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return overallAlertResponse;
    }

    public CompanyJobDetailsDTO getCompanyJobDetails(long jobId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        CompanyJobDetailsDTO companyJobDetailsDTO = new CompanyJobDetailsDTO();
        Object[] results = adminRegistrationRepository.getCompanyJobDetails(jobId);
        if (results != null) {
            companyJobDetailsDTO.setJobTitle(results[0] != null ? results[0].toString() : "");
            companyJobDetailsDTO.setJobFunction(results[1] != null ? results[1].toString() : "");
            companyJobDetailsDTO.setJobPostedBy((results[2] != null ? results[2].toString() : "") + " " + (results[22] != null ? results[22].toString() : ""));
            companyJobDetailsDTO.setPostedDate(sdf.format((Date) results[3]));
            companyJobDetailsDTO.setDescription(results[4] != null ? results[4].toString() : "");
            companyJobDetailsDTO.setSkills(results[5] != null ? results[5].toString() : "");
            companyJobDetailsDTO.setQuestionnaire(results[6] != null ? results[6].toString() : "");
            if (results[7] != null && !results[7].toString().equals("")) {
                List<Long> collaboratorList = Arrays.stream(results[7].toString().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                List<User> users = userRepository.findAllById(collaboratorList);
                String collaboratorNames = users.stream()
                        .filter(Objects::nonNull)
                        .map(user -> {
                            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
                            String lastName = user.getLastName() != null ? user.getLastName() : "";
                            return firstName + " " + lastName;
                        })
                        .collect(Collectors.joining(", "));
                companyJobDetailsDTO.setCollaborator(collaboratorNames);
            }
            companyJobDetailsDTO.setApplyBy(results[8] != null ? sdf.format((Date) results[8]) : "");
            companyJobDetailsDTO.setEduQualification(results[9] != null ? results[9].toString() : "");
            companyJobDetailsDTO.setJobType(JobType.getByValue(((Integer) results[10]).intValue()).toString());
            companyJobDetailsDTO.setLocation(results[11] != null ? results[11].toString() : "");
            companyJobDetailsDTO.setExperience(((Double) results[12]).longValue());
            companyJobDetailsDTO.setSalary(((Double) results[13]).longValue());
            companyJobDetailsDTO.setSalaryType(SalaryType.valueOf(results[14].toString()).toString());
            companyJobDetailsDTO.setCurrency(Currency.valueOf(results[15].toString()).toString());
            companyJobDetailsDTO.setSupplementalPay(results[16] != null ? results[16].toString() : "");
            companyJobDetailsDTO.setVacancies(((BigInteger) results[17]).longValue());
            companyJobDetailsDTO.setNoticePeriod(results[18] != null ? results[18].toString() : "");
            companyJobDetailsDTO.setCitizenship(results[19] != null ? results[19].toString() : "");
            companyJobDetailsDTO.setBenefits(results[20] != null ? results[20].toString() : "");
            companyJobDetailsDTO.setEthnicity(results[21] != null ? results[21].toString() : "");
        }

        return companyJobDetailsDTO;
    }

    public JobPostApplicantDataDTO getApplicantDataForJobPost(Long applicantId, Long jobPostId) throws Exception {
        JobPostApplicantDataDTO jobPostApplicantDataDTO = new JobPostApplicantDataDTO();
        ApplicantDTO applicantDTO = talentViewService.getProfileInfoForJob(applicantId, jobPostId);
        Interview interview = interviewRepository.findByJobPostIdAndApplicantId(jobPostId, applicantId);
        if (interview != null) {
            InterviewDTO interviewDTO = new InterviewDTO();
            interviewDTO.setInterviewDate(interview.getInterviewDate());
            interviewDTO.setInterviewDescription(interview.getInterviewDescription());
            interviewDTO.setInterviewEndTime(interview.getInterviewEndTime().toLocalTime().toString());
            interviewDTO.setInterviewLocation(interview.getInterviewLocation());
            interviewDTO.setInterviewTitle(interview.getInterviewTitle());
            interviewDTO.setInterviewMode(interview.getInterviewMode());
            interviewDTO.setInterviewStartTime(interview.getInterviewEndTime().toLocalTime().toString());
            interviewDTO.setInterviewVideoLink(interview.getInterviewVideoLink());
            interviewDTO.setInterviewId(interview.getInterviewId());
            InterviewFeedbackDTO feedbackDTO = activityService.getFeedback(interview.getInterviewId());
            OfferDetails offerDetailsObj = offerDetailsRepository.findByJobPostIdAndApplicantId(jobPostId, applicantId);
            if (offerDetailsObj != null) {
                OfferDetailsDTO offerDetailsDTO = new OfferDetailsDTO();
                offerDetailsDTO.setJoiningDate(offerDetailsObj.getJoiningDate());
                offerDetailsDTO.setOfferStatus(offerDetailsObj.getOfferStatus());
                offerDetailsDTO.setOfferUrl(offerDetailsObj.getOfferUrl());
                offerDetailsDTO.setOfferSignedUrl(offerDetailsObj.getOfferSignedUrl());
                jobPostApplicantDataDTO.setOfferDetailsDTO(offerDetailsDTO);
            }
            jobPostApplicantDataDTO.setApplicantDTO(applicantDTO);
            jobPostApplicantDataDTO.setInterviewDTO(interviewDTO);
            jobPostApplicantDataDTO.setInterviewFeedbackDTO(feedbackDTO);
        }

        return jobPostApplicantDataDTO;
    }

    public PaginationResponse getRelevantProfiles(long jobId, ApplicantProfileFilterDTO applicantProfileFilterDTO, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        Map<String, Object> results = adminRegistrationRepository.getRelevantProfiles(jobId, applicantProfileFilterDTO, name, from, to, pageNumber, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) results.get("data");
        PaginationResponse relevantProfilesResponse = new PaginationResponse();
        List<ApplicantProfilesDTO> applicantProfilesDTOS = new ArrayList<>();
        for (Object[] rows : data) {
            ApplicantProfilesDTO applicantProfilesDTO = new ApplicantProfilesDTO();
            applicantProfilesDTO.setFirstName(rows[0] != null ? rows[0].toString() : "");
            applicantProfilesDTO.setLastName(rows[1] != null ? rows[1].toString() : "");
            applicantProfilesDTO.setConsultancyName(rows[2] != null ? rows[2].toString() : "");
            applicantProfilesDTO.setUploadedDate(rows[3] != null ? rows[3].toString() : "");
            applicantProfilesDTO.setJobFunction(rows[4] != null ? rows[4].toString() : "");
            applicantProfilesDTO.setSecondaryJobFunction(rows[5] != null ? rows[5].toString() : "");
            applicantProfilesDTO.setLocation(rows[6] != null ? rows[6].toString() : "");
            applicantProfilesDTO.setProfileMatch(((Integer) rows[7]).intValue());
            applicantProfilesDTO.setApplicantId(((BigInteger) rows[8]).longValue());
            applicantProfilesDTOS.add(applicantProfilesDTO);
        }
        relevantProfilesResponse.setData(applicantProfilesDTOS);
        long totalCount = (Long) results.get("count");
        relevantProfilesResponse.setTotalCount(totalCount);
        relevantProfilesResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return relevantProfilesResponse;
    }

    public PaginationResponse getInterestedProfiles(long jobId, ApplicantProfileFilterDTO applicantProfileFilterDTO, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        Map<String, Object> results = adminRegistrationRepository.getInterestedProfiles(jobId, applicantProfileFilterDTO, name, from, to, pageNumber, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) results.get("data");
        PaginationResponse interestedProfilesResponse = new PaginationResponse();
        List<ApplicantProfilesDTO> applicantProfilesDTOS = new ArrayList<>();
        for (Object[] rows : data) {
            ApplicantProfilesDTO applicantProfilesDTO = new ApplicantProfilesDTO();
            applicantProfilesDTO.setFirstName(rows[0] != null ? rows[0].toString() : "");
            applicantProfilesDTO.setLastName(rows[1] != null ? rows[1].toString() : "");
            applicantProfilesDTO.setConsultancyName(rows[2] != null ? rows[2].toString() : "");
            applicantProfilesDTO.setUploadedDate(rows[3] != null ? rows[3].toString() : "");
            applicantProfilesDTO.setJobFunction(rows[4] != null ? rows[4].toString() : "");
            applicantProfilesDTO.setSecondaryJobFunction(rows[5] != null ? rows[5].toString() : "");
            applicantProfilesDTO.setLocation(rows[6] != null ? rows[6].toString() : "");
            applicantProfilesDTO.setProfileMatch(((Integer) rows[7]).intValue());
            applicantProfilesDTO.setInterestedDate(rows[8] != null ? rows[8].toString() : "");
            applicantProfilesDTO.setApplicantId(((BigInteger) rows[9]).longValue());
            applicantProfilesDTOS.add(applicantProfilesDTO);
        }
        interestedProfilesResponse.setData(applicantProfilesDTOS);
        long totalCount = (Long) results.get("count");
        interestedProfilesResponse.setTotalCount(totalCount);
        interestedProfilesResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return interestedProfilesResponse;
    }

    public List<ApplicantStatusHistoryDTO> getApplicantDataWithStatus(Long jobPostId, Long applicantId) {
        JobMatchConsultancy jobMatchConsultancy = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId, applicantId);

        List<ApplicantStatusHistoryDTO> applicantStatusHistoryDTOS = new ArrayList<>();
        if (jobMatchConsultancy != null) {
            if (jobMatchConsultancy.getInterviewRejectedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.INTERVIEW_REJECTED.toString(), jobMatchConsultancy.getInterviewRejectedDate(), jobMatchConsultancy.getInterviewRejectedUserId()));
            }
            if (jobMatchConsultancy.getInterviewRequestedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.REQUESTED_INTERVIEW.toString(), jobMatchConsultancy.getInterviewRequestedDate(), jobMatchConsultancy.getInterviewRequestedUserId()));
            }
            if (jobMatchConsultancy.getInterviewScheduledDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.INTERVIEW_SCHEDULED.toString(), jobMatchConsultancy.getInterviewScheduledDate(), jobMatchConsultancy.getInterviewScheduledUserId()));
            }
            if (jobMatchConsultancy.getRescheduledRequestDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.RESCHEDULE_REQUESTED.toString(), jobMatchConsultancy.getRescheduledRequestDate(), jobMatchConsultancy.getInterviewRescheduledRequestedUserId()));
            }
            if (jobMatchConsultancy.getRescheduledInterviewDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.RESCHEDULED_INTERVIEW.toString(), jobMatchConsultancy.getRescheduledInterviewDate(), jobMatchConsultancy.getInterviewRescheduledUserId()));
            }
            if (jobMatchConsultancy.getSelectedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.SELECTED.toString(), jobMatchConsultancy.getSelectedDate(), jobMatchConsultancy.getSelectedUserId()));
            }
            if (jobMatchConsultancy.getOnHoldDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.ON_HOLD.toString(), jobMatchConsultancy.getOnHoldDate(), jobMatchConsultancy.getOnholdUpdatedUserId()));
            }
            if (jobMatchConsultancy.getOfferSentDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.OFFER_SENT.toString(), jobMatchConsultancy.getOfferSentDate(), jobMatchConsultancy.getOffersentUserId()));
            }
            if (jobMatchConsultancy.getOfferAcceptedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.OFFER_ACCEPTED.toString(), jobMatchConsultancy.getOfferAcceptedDate(), jobMatchConsultancy.getOfferacceptedUserId()));
            }
            if (jobMatchConsultancy.getRejectedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.REJECTED.toString(), jobMatchConsultancy.getRejectedDate(), jobMatchConsultancy.getRejectedUserId()));
            }
            if (jobMatchConsultancy.getAccepetedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus(InterviewStatus.HIRED.toString(), jobMatchConsultancy.getAccepetedDate(), jobMatchConsultancy.getAcceptedUserId()));
            }
            if (jobMatchConsultancy.getApplicantStatus() == 1 && jobMatchConsultancy.getRecruiterUpdatedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus("Shortlisted", jobMatchConsultancy.getRecruiterUpdatedDate(), jobMatchConsultancy.getUpdatedByRecId()));
            }
            if (jobMatchConsultancy.getApplicantStatus() == 1 && jobMatchConsultancy.getApplicantJobStatus() == 1 && jobMatchConsultancy.getApplicantUpdatedDate() != null) {
                applicantStatusHistoryDTOS.add(fetchApplicantDataWithStatus("Matched", jobMatchConsultancy.getApplicantUpdatedDate(), jobMatchConsultancy.getUpdatedByConsId()));
            }
        }

        return applicantStatusHistoryDTOS.stream().sorted(Comparator.comparing(ApplicantStatusHistoryDTO::getDateOfLastUpdate).reversed())
                .collect(Collectors.toList());
    }

    private ApplicantStatusHistoryDTO fetchApplicantDataWithStatus(String status, Date date, Long interviewRejectedUserId) {
        ApplicantStatusHistoryDTO applicantStatusHistoryDTO = new ApplicantStatusHistoryDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        applicantStatusHistoryDTO.setStatus(status);
        applicantStatusHistoryDTO.setDateOfLastUpdate(sdf.format(date));
        Optional<User> user = interviewRejectedUserId != null ? userRepository.findById(interviewRejectedUserId) : null;
        if (user != null && user.isPresent()) {
            applicantStatusHistoryDTO.setUpdatedBy((user.get().getFirstName() != null ? user.get().getFirstName() : "") + " " + (user.get().getLastName() != null ? user.get().getLastName() : ""));
        }
        return applicantStatusHistoryDTO;
    }
}
