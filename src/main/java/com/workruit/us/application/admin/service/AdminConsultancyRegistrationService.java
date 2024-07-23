package com.workruit.us.application.admin.service;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantJobs;
import com.workruit.us.application.admin.enums.ApplicantProfileStatus;
import com.workruit.us.application.admin.enums.TimePeriod;
import com.workruit.us.application.admin.repository.AdminConsultancyRegistrationRepositoryImpl;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.enums.JobType;
import com.workruit.us.application.models.Consultancy;
import com.workruit.us.application.models.User;
import com.workruit.us.application.repositories.ConsultancyRepository;
import com.workruit.us.application.repositories.DepartmentRepository;
import com.workruit.us.application.repositories.UserRepository;
import com.workruit.us.application.services.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AdminConsultancyRegistrationService {

    @Autowired
    private AdminConsultancyRegistrationRepositoryImpl adminConsultancyRegistrationRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired ApplicantService applicantService;
    private @Autowired DepartmentRepository departmentRepository;

    public PaginationResponse getIncompleteConsultancyRegistration(IncompleteRegistrationFilter incompleteRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) {

        Map<String, Object> result = adminConsultancyRegistrationRepository.getIncompleteConsultancyRegistration(incompleteRegistrationFilter, name, from, to, pageNumber, pageSize, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<IncompleteRegistrationDTO> incompleteRegistrationDTOs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse incompleteRegistrationResponse = new PaginationResponse();
        for (Object[] row : data) {
            IncompleteRegistrationDTO incompleteRegistrationDTO = new IncompleteRegistrationDTO();
            incompleteRegistrationDTO.setId(((BigInteger) row[0]).longValue());
            incompleteRegistrationDTO.setName(row[1] != null ? (row[1]).toString() : "");
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

    public PaginationResponse getCompleteConsultancyRegistration(CompleteRegistrationFilter completeRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        Map<String, Object> result = adminConsultancyRegistrationRepository.getCompleteConsultancyRegistration(completeRegistrationFilter, name, from, to, pageNumber, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<CompleteConsultancyRegistrationDTO> completeRegistrationDTOs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse completeRegistrationResponse = new PaginationResponse();
        for (Object[] row : data) {
            CompleteConsultancyRegistrationDTO completeRegistrationDTO = new CompleteConsultancyRegistrationDTO();
            completeRegistrationDTO.setConsultancyId(((BigInteger) row[0]).longValue());
            completeRegistrationDTO.setConsultancyName((String) row[1]);
            completeRegistrationDTO.setRegistrationDate(row[2] != null ? sdf.format(((Date) row[2])) : "");
            completeRegistrationDTO.setLocation((String) row[3]);
            completeRegistrationDTO.setIndustryType((String) row[4]);
            completeRegistrationDTO.setDomains((String) row[5]);
            completeRegistrationDTO.setAccountStatus("ACTIVE");
            completeRegistrationDTO.setShortlisted(((BigInteger) row[6]).longValue());
            completeRegistrationDTO.setUnderInterview(((BigInteger) row[7]).longValue());
            completeRegistrationDTO.setUnderHire(((BigInteger) row[8]).longValue());
            completeRegistrationDTO.setUnderReject(((BigInteger) row[9]).longValue());
            completeRegistrationDTO.setRegisteredConsultancy(((BigInteger) row[10]).longValue());
            completeRegistrationDTO.setPendingRegistrationConsultancy(((BigInteger) row[11]).longValue());
            completeRegistrationDTO.setUploadedProfiles(row[12] != null ? ((BigInteger) row[12]).longValue() : 0);
            completeRegistrationDTO.setLastActive(row[13] != null ? sdf.format(((Date) row[13])) : "");
            completeRegistrationDTOs.add(completeRegistrationDTO);
        }
        completeRegistrationResponse.setData(completeRegistrationDTOs);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        completeRegistrationResponse.setTotalCount(totalCount);
        completeRegistrationResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return completeRegistrationResponse;
    }

    public PaginationResponse getConsultancyConsManager(Long userId, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) throws WorkruitException {

        Map<String, Object> result = adminConsultancyRegistrationRepository.getConsManagerOfConsultancy(userId, name, from, to, pageNumber, pageSize, period);
        List<UserDTO> consultancyUserDTOS = new ArrayList<>();
        PaginationResponse consultancyManagerResponse = new PaginationResponse();
        List<User> data = (List<User>) result.get("data");
        for (User user : data) {
            consultancyUserDTOS.add(generateConsutancyUserDTO(user));
        }
        consultancyManagerResponse.setData(consultancyUserDTOS);
        long totalCount = (long) result.get("count");
        consultancyManagerResponse.setTotalCount(totalCount);
        consultancyManagerResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return consultancyManagerResponse;
    }

    public UserDTO getConsultancyMainCons(Long consultancyId)
            throws WorkruitException {
        Optional<Consultancy> optionalConsultancy = consultancyRepository.findById(consultancyId);
        if (optionalConsultancy.isPresent()) {
            List<Long> roleIds = Arrays.asList((long) 1, (long) 2);
            return generateConsutancyUserDTO(userRepository.findMainConsultancyUser(consultancyId, roleIds));
        } else {
            throw new WorkruitException("Consultancy not found for the given id:" + consultancyId);
        }
    }

    public UserDTO getConsultantManagerDetails(Long userId)
            throws WorkruitException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            return generateConsutancyUserDTO(optionalUser.get());
        } else {
            throw new WorkruitException("User not found for the given id:" + userId);
        }
    }

    private UserDTO generateConsutancyUserDTO(User user) throws WorkruitException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        UserDTO userDTO = new UserDTO();
        if (user != null) {
            userDTO.setUserId(user.getUserId());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setLastName(user.getLastName());
            userDTO.setCreatedDate(sdf.format(user.getCreatedDate()));
            if (user.getUpdatedDate() != null && user.getCreatedDate() != null) {
                userDTO.setUpdatedProfile(user.getUpdatedDate().equals(user.getCreatedDate()) ? "No" : "Yes");
            }
            userDTO.setIsActivated(user.isEnabled() ? "Yes" : "No");
            userDTO.setIsClickedOnLink(user.isEnabled() ? "Yes" : "No");
            if (user.getEnabledDate() != null) {
                userDTO.setEnabledAccountDate(sdf.format(user.getEnabledDate()));
            }
            userDTO.setWorkEmail(user.getWorkEmail());
            userDTO.setContactNumber(user.getPhoneNumber());
            userDTO.setRole(user.getRoleName());
            if (user.getDepartmentId() != null && departmentRepository.findById(user.getDepartmentId()).isPresent()) {
                userDTO.setDepartment(departmentRepository.findById(user.getDepartmentId()).get().getName());
            }
        } else {
            throw new WorkruitException("User not found");
        }
        return userDTO;
    }

    public PaginationResponse getApplicantsOfCons(long consultancyId, String name, int pageNumber, int pageSize) {
        Map<String, Object> result = adminConsultancyRegistrationRepository.getApplicantsOfCons(consultancyId, name, pageNumber, pageSize);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<ApplicantsOfConsDTO> applicantsOfConsDTOS = new ArrayList<>();
        PaginationResponse applicantsOfConsResponse = new PaginationResponse();
        for (Object[] row : data) {
            ApplicantsOfConsDTO applicantsOfCons = new ApplicantsOfConsDTO();
            applicantsOfCons.setUserId(((BigInteger) row[0]).longValue());
            applicantsOfCons.setFirstName(row[1] != null ? (String) row[1] : "");
            applicantsOfCons.setLastName(row[2] != null ? (String) row[2] : "");
            applicantsOfCons.setProfilesUploaded(row[3] != null ? ((BigInteger) row[3]).longValue() : 0);
            applicantsOfConsDTOS.add(applicantsOfCons);
        }
        applicantsOfConsResponse.setData(applicantsOfConsDTOS);
        long totalCount = ((long) result.get("count"));
        applicantsOfConsResponse.setTotalCount(totalCount);
        applicantsOfConsResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return applicantsOfConsResponse;
    }

    public PaginationResponse getUploadedApplicants(Long consultancyId, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period, UploadedApplicantFilterDTO uploadedApplicantFilterDTO, Long userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Map<String, Object> result = adminConsultancyRegistrationRepository.getUploadedApplicants(consultancyId, name, from, to, pageNumber, pageSize, sortBy, period, uploadedApplicantFilterDTO, userId);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<UploadedProfilesDTO> uploadedProfilesDTOS = new ArrayList<>();
        PaginationResponse uploadedProfilesResponse = new PaginationResponse();
        for (Object[] row : data) {
            UploadedProfilesDTO uploadedProfilesDTO = new UploadedProfilesDTO();
            uploadedProfilesDTO.setApplicantId(((BigInteger) row[0]).longValue());
            uploadedProfilesDTO.setFirstName(row[1] != null ? (String) row[1] : "");
            uploadedProfilesDTO.setLastName(row[2] != null ? (String) row[2] : "");
            uploadedProfilesDTO.setUploadedDate(row[3] != null ? sdf.format(((Date) row[3])) : "");
            uploadedProfilesDTO.setLocation(row[4] != null ? (String) row[4] : "");
            uploadedProfilesDTO.setJobFunction(row[5] != null ? (String) row[5] : "");
            uploadedProfilesDTO.setSecondaryJobFunction(row[6] != null ? (String) row[6] : "");
            uploadedProfilesDTO.setGender(row[7] != null ? (String) row[7] : "");
            uploadedProfilesDTO.setEthnicity(row[8] != null ? (String) row[8] : "");
            uploadedProfilesDTO.setCitizenship(row[9] != null ? (String) row[9] : "");
            uploadedProfilesDTO.setYearsOfExperience(row[10] != null ? (Float) row[10] : 0);
            uploadedProfilesDTO.setCareerLevel(row[11] != null ? (String) row[11] : "");
            uploadedProfilesDTO.setWorkMode(row[12] != null ? (String) row[12] : "");
            uploadedProfilesDTO.setJobType(row[13] != null ? (String) row[13] : "");
            uploadedProfilesDTO.setNoticePeriod(row[14] != null ? (String) row[14] : "");
            uploadedProfilesDTO.setCurrentWorkStatus(row[15] != null ? (String) row[15] : "");
            uploadedProfilesDTO.setJobsAppliedFor(row[17] != null ? ((BigInteger) row[17]).longValue() : 0);
            uploadedProfilesDTO.setRelevantJobs(row[16] != null ? ((BigInteger) row[16]).longValue() : 0);
            uploadedProfilesDTO.setProfileCompletion(applicantService.profileCompletionStatus(uploadedProfilesDTO.getApplicantId()));
            uploadedProfilesDTO.setUploadedBy((row[18] != null ? ((String) row[18]) : "") + " " + (row[19] != null ? ((String) row[19]) : ""));
            uploadedProfilesDTOS.add(uploadedProfilesDTO);
        }
        uploadedProfilesResponse.setData(uploadedProfilesDTOS);
        long totalCount = ((long) result.get("count"));
        uploadedProfilesResponse.setTotalCount(totalCount);
        uploadedProfilesResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return uploadedProfilesResponse;
    }

    public PaginationResponse getApplicantJobs(Long consultancyId, Long applicantId, String title, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period, JobAppliedFilterDTO jobAppliedFilterDTO, ApplicantJobs applicantJobs) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        Map<String, Object> result = adminConsultancyRegistrationRepository.getAppliedJobs(consultancyId, applicantId, title, from, to, pageNumber, pageSize, sortBy, period, jobAppliedFilterDTO, applicantJobs);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<JobAppliedDTO> jobAppliedDTOS = new ArrayList<>();
        PaginationResponse jobDetailsResponse = new PaginationResponse();
        for (Object[] row : data) {
            JobAppliedDTO jobAppliedDTO = new JobAppliedDTO();
            jobAppliedDTO.setJobId(((BigInteger) row[0]).longValue());
            jobAppliedDTO.setTitle(row[1] != null ? (String) row[1] : "");
            jobAppliedDTO.setJobFunction(row[2] != null ? (String) row[2] : "");
            jobAppliedDTO.setLocation(row[3] != null ? (String) row[3] : "");
            jobAppliedDTO.setWorkMode(row[4] != null ? (String) row[4] : "");
            jobAppliedDTO.setJobType(row[5] != null ? JobType.getByValue(((Integer) row[5]).intValue()).toString() : "");
            jobAppliedDTO.setCompanyName(row[6] != null ? (String) row[6] : "");
            if (applicantJobs.equals(ApplicantJobs.APPLIED_JOBS) && row[7] != null) {
                int status = ((BigInteger) row[7]).intValue();
                if (status != 0) {
                    jobAppliedDTO.setApplicantStatus(InterviewStatus.getByValue(status).toString());
                } else if (row[8] != null && ((Integer) row[8]).intValue() == 1) {
                    jobAppliedDTO.setApplicantStatus("Matched");
                } else {
                    jobAppliedDTO.setApplicantStatus("Applied");
                }
                jobAppliedDTO.setLastUpdated(row[9] != null ? sdf.format(((Date) row[9])) : "");
            }
            jobAppliedDTO.setMatchScore(row[10] != null ? ((Integer) row[10]).intValue() : 0);
            jobAppliedDTOS.add(jobAppliedDTO);
        }
        jobDetailsResponse.setData(jobAppliedDTOS);
        long totalCount = ((long) result.get("count"));
        jobDetailsResponse.setTotalCount(totalCount);
        jobDetailsResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return jobDetailsResponse;
    }

    public PaginationResponse getJobsActivity(Long consultancyId, String title, int pageNumber, int pageSize, ConsActivityFilterDTO consActivityFilterDTO) {
        Map<String, Object> result = adminConsultancyRegistrationRepository.getJobsActivity(consultancyId, title, pageNumber, pageSize, consActivityFilterDTO);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<ConsActivityJobsDTO> consActivityJobsDTOS = new ArrayList<>();
        PaginationResponse consActivityResponse = new PaginationResponse();
        for (Object[] row : data) {
            ConsActivityJobsDTO consActivityJobsDTO = new ConsActivityJobsDTO();
            consActivityJobsDTO.setJobId(((BigInteger) row[0]).longValue());
            consActivityJobsDTO.setTitle(row[1] != null ? (String) row[1] : "");
            consActivityJobsDTO.setCompanyName(row[2] != null ? (String) row[2] : "");
            consActivityJobsDTO.setUnderApplied(row[3] != null ? ((BigInteger) row[3]).longValue() : 0);
            consActivityJobsDTO.setUnderInterview(row[4] != null ? ((BigInteger) row[4]).longValue() : 0);
            consActivityJobsDTO.setUnderHired(row[5] != null ? ((BigInteger) row[5]).longValue() : 0);
            consActivityJobsDTO.setUnderRejected(row[6] != null ? ((BigInteger) row[6]).longValue() : 0);
            consActivityJobsDTO.setAppliedBy((row[7] != null ? (String) row[7] : "") + " " + (row[8] != null ? (String) row[8] : ""));
            consActivityJobsDTOS.add(consActivityJobsDTO);
        }
        consActivityResponse.setData(consActivityJobsDTOS);
        long totalCount = ((long) result.get("count"));
        consActivityResponse.setTotalCount(totalCount);
        consActivityResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return consActivityResponse;
    }

    public PaginationResponse getApplicantData(Long consultancyId, Long jobPostId, JobApplicantFilter jobApplicantFilter, String name, ApplicantProfileStatus applicantStatus, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) throws WorkruitException {
        Map<String, Object> result = adminConsultancyRegistrationRepository.getApplicantData(consultancyId, jobPostId, jobApplicantFilter, name, applicantStatus, from, to, pageNo, pageSize, sortBy, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        PaginationResponse jobApplicantDataResponse = new PaginationResponse();
        List<JobApplicantProfilesDTO> jobApplicantDataDTOs = new ArrayList<>();
        for (Object[] row : data) {
            JobApplicantProfilesDTO jobApplicantDataDTO = new JobApplicantProfilesDTO();
            jobApplicantDataDTO.setApplicantId(((BigInteger) row[0]).longValue());
            jobApplicantDataDTO.setFirstName(row[1] != null ? row[1].toString() : "");
            jobApplicantDataDTO.setLastName(row[2] != null ? row[2].toString() : "");
            jobApplicantDataDTO.setUploadedDate(row[3] != null ? sdf.format((Date) row[3]) : "");
            jobApplicantDataDTO.setPrimaryJobFunction(row[4] != null ? row[4].toString() : "");
            jobApplicantDataDTO.setSecondaryJobFunction(row[5] != null ? row[5].toString() : "");
            jobApplicantDataDTO.setLocation(row[6] != null ? row[6].toString() : "");
            jobApplicantDataDTO.setDateOfLastUpdate(row[7] != null ? sdf.format((Date) row[7]) : "");
            jobApplicantDataDTO.setProfileScore(row[8] != null ? ((Integer) row[8]).intValue() : 0);

            if (applicantStatus == ApplicantProfileStatus.APPLIED) {
                if (row[9] != null && ((Integer) row[9]).intValue() == 1) {
                    jobApplicantDataDTO.setApplicantStatus("Matched");
                } else {
                    jobApplicantDataDTO.setApplicantStatus("Applied");
                }
            } else {
                jobApplicantDataDTO.setApplicantStatus(InterviewStatus.getByValue(((BigInteger) row[10]).intValue()).toString());
            }
            jobApplicantDataDTOs.add(jobApplicantDataDTO);
        }
        jobApplicantDataResponse.setData(jobApplicantDataDTOs);
        long totalCount = (Long) result.get("count");
        jobApplicantDataResponse.setTotalCount(totalCount);
        jobApplicantDataResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return jobApplicantDataResponse;
    }

    public PaginationResponse getConsultancyAlertData(Long consultancyId, List<Long> userId, int pageNumber, int pageSize, Date from, Date to, TimePeriod period) {
        Map<String, Object> result = adminConsultancyRegistrationRepository.getConsultancyAlertData(consultancyId, userId, pageNumber, pageSize, from, to, period);
        List<Object[]> data = (List<Object[]>) result.get("data");
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a | dd MMM yyyy");
        PaginationResponse overallAlertResponse = new PaginationResponse();
        List<OverallAlertDTO> overallAlertDTOS = new ArrayList<>();
        for (Object[] row : data) {
            OverallAlertDTO overallAlertDTO = new OverallAlertDTO();
            overallAlertDTO.setMsg((row[0]).toString());
            overallAlertDTO.setAlertDate(sdf.format(((Date) row[1])));
            overallAlertDTOS.add(overallAlertDTO);
        }
        overallAlertResponse.setData(overallAlertDTOS);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        overallAlertResponse.setTotalCount(totalCount);
        overallAlertResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return overallAlertResponse;
    }
}
