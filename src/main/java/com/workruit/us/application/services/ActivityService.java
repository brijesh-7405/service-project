package com.workruit.us.application.services;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.enums.JobApplState;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.enums.OfferStatus;
import com.workruit.us.application.exception.OfferNotFoundException;
import com.workruit.us.application.models.*;
import com.workruit.us.application.notification.NotificationService;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ActivityService {
    private @Autowired JobPostService jobPostService;

    private @Autowired JobMatchingRepository jobMatchingRepository;
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired InterviewRepository interviewRepository;
    private @Autowired InterviewFeedbackRepository interviewFeedbackRepository;
    private @Autowired OfferDetailsRepository offerDetailsRepository;
    private @Autowired ApplicantDetailsService applicantDetailsService;
    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired DashboardService dashboardService;
    private @Autowired AlertService alertService;

    private @Autowired CompanyRepository companyRepository;

    private @Autowired FirebaseMessagingService firebaseMessagingService;

    private @Autowired NotificationService notificationService;

    private @Autowired ImageService imageService;

    private @Autowired EmailService emailService;

    @Value("${base.url}")
    private String baseUrl;

    public ActivityViewResponse getJobActivityForRecruiter(long recruiterId, long consultancyId, JobStatus active,
                                                           int filter, String role, Integer pageNo, Integer pageSize) {
        JobViewResponse jobRes = new JobViewResponse();
        ActivityViewResponse actRes = new ActivityViewResponse();

//        if (filter == 0) {
//            jobRes = jobPostService.getAllJobsForUser(recruiterId, active, filter, pageNo, pageSize);
//            actRes.setActivityViewDTO(jobRes.getJobViewDTO());
//            actRes.setTotalCount(jobRes.getTotalCount());
//            actRes.setTotalPages(jobRes.getTotalPages());
//        } else {
        jobRes = jobPostService.getAllTeamPostedJobsForUser(recruiterId, consultancyId, active, filter, pageNo, role, pageSize);
        actRes.setActivityViewDTO(jobRes.getJobViewDTO());
        actRes.setTotalCount(jobRes.getTotalCount());
        actRes.setTotalPages(jobRes.getTotalPages());
        // }
        return actRes;
    }
    // Status 0 Not matched 1 matched, 2 All

    public ActivityShortlistedResponse getShortlistedProfilesForJob(long consultantId, long recruiterId, long jobId, int status,
                                                                    int filter, String role, Integer pageNo, Integer pageSize) {
        if (status < 0 || status > 2) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        int shortlistValue = JobApplState.SHORTLISTED.getValue();
        int statusValue = status == 1 ? shortlistValue : (status == 2 ? 2 : 0);

        return getProfilesForJobByStatus(consultantId, recruiterId, jobId, shortlistValue, statusValue, filter, role, pageNo, pageSize);
    }

    //	public ActivityShortlistedResponse getShortlistedProfilesForRecruiter(long recruiterId, long jobId, Integer pageNo,
//			Integer pageSize) {
//		ActivityShortlistedResponse res = getProfilesForJobByStatus(jobId, JobApplState.SHORTLISTED.getValue(), pageNo,
//				pageSize);
//		return res;
//	}
    private ActivityShortlistedResponse getProfilesForJobByStatus(long consultantId, long recruiterId, long jobId, int applicantStatus,
                                                                  int applicantJobStatus, int filter, String role, Integer pageNo, Integer pageSize) {

        List<Order> orders = new ArrayList<Order>();
        //updatedDate  is referring to Recruiter updated date.
//        Order dateOrder = new Order(Sort.Direction.DESC, "updatedDate");
//        orders.add(dateOrder);
//        Order statusOrder = new Order(Sort.Direction.DESC, "hiredStatus");
//        orders.add(statusOrder);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<JobMatchStateResultSet> jobMatchRs = null;

        List<Long> userIds = new ArrayList<>();
        userIds.addAll(getUserIds(consultantId, recruiterId, jobId, role, filter));

        List<Long> applicantStatusList = new ArrayList<>();
        List<Long> recruiterStatusList = new ArrayList<>();

        if (userIds != null && userIds.size() > 0) {
            if (applicantJobStatus == 2) {
                //jobMatchRs = jobMatchingRepository.getProfilesByApplicantStatusAllWithSort(userIds, jobId, pageable);
                applicantStatusList.add(0l);
                applicantStatusList.add(1l);
                recruiterStatusList.add(1l);
                jobMatchRs = jobMatchingRepository.getProfilesByApplicantStatus(userIds, jobId, pageable);
            } else {

                jobMatchRs = jobMatchingRepository.getProfilesByApplicantStatus(userIds, jobId,
                        applicantStatus, applicantJobStatus, pageable);
            }
        } else {
            return new ActivityShortlistedResponse();
        }

        StatusDTO statusDTO = dashboardService.getStats(jobId, userIds);

        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));
        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ActivityShortlistedResponse response = new ActivityShortlistedResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityShortlistedDTO> activityShortlistedDTOs = new ArrayList<>();
        ActivityShortlistedDTO activityShortlistedDTO = null;
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            activityShortlistedDTO = new ActivityShortlistedDTO();
            activityShortlistedDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityShortlistedDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());
            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
            activityShortlistedDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityShortlistedDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                    ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                    : "");
            activityShortlistedDTO.setExperience(String.valueOf(applicantDetails.getYearsOfExperience()));
            activityShortlistedDTO.setJobFunctionName(String.valueOf(applicantDetails.getJobFunction()));
            activityShortlistedDTO
                    .setSecondaryJobFunctionName(String.valueOf(applicantDetails.getSecondaryJobFunction()));
            activityShortlistedDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityShortlistedDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityShortlistedDTO.setProfilePicUrl(null);
            }
            activityShortlistedDTO
                    .setShortListedBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName() + " "
                            + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
            activityShortlistedDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
            activityShortlistedDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
            activityShortlistedDTOs.add(activityShortlistedDTO);
        }

//        activityShortlistedDTOs.sort(Comparator.comparing(ActivityShortlistedDTO::getUpdatedDate).reversed());
        response.setActivityShortlistedDTO(activityShortlistedDTOs);

        response.setCountsInfo(statusDTO);
        return response;
    }

    public ActivityInterviewResponse getInterviewProfilesForJobData(Long consultantId, long recruiterId, long jobId, String role,
                                                                    int status, int filter, Integer pageNo, Integer pageSize) {
        ActivityInterviewResponse res = getInterviewProfilesForJob(consultantId, recruiterId, jobId, role, status, filter,
                pageNo, pageSize);
        return res;
    }

    private ActivityInterviewResponse getInterviewProfilesForJob(Long consultantId, long recruiterId, long jobId, String role,
                                                                 long interviewState, int filter, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());

        Page<JobMatchStateResultSet> jobMatchRs = null;

        if (interviewState == 1) {
            interviewState = 2L;
        }

        List<Long> interviewStateList = new ArrayList<>();
        if (interviewState == 0) {
            interviewStateList = Arrays.asList(2L, 5L, 6L, 3L, 4L, 9L, 12L, 13L);
        } else if (interviewState == 2) {
            interviewStateList = Arrays.asList(3L, interviewState, 12L, 13L);
        } else {
            interviewStateList.add(interviewState);
        }

        List<Long> userIds = new ArrayList<>();
        userIds.addAll(getUserIds(consultantId, recruiterId, jobId, role, filter));


        if (userIds != null && userIds.size() > 0) {
            jobMatchRs = jobMatchingRepository.getInterviewsListforJobByTeam(jobId, interviewStateList, userIds, pageable);
        } else {
            return new ActivityInterviewResponse();
        }

        List<ActivityInterviewDTO> activityInterviewDTOs = new ArrayList<>();
        ActivityInterviewResponse response = new ActivityInterviewResponse();
        StatusDTO statusDTO = dashboardService.getStats(jobId, userIds);
        response.setCountsInfo(statusDTO);
        if (jobMatchRs != null && jobMatchRs.getNumberOfElements() == 0) {
            return response;
        }


        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());

        ActivityInterviewDTO activityInterviewDTO = null;
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            activityInterviewDTO = new ActivityInterviewDTO();
            activityInterviewDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityInterviewDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                    ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                    : "");
            activityInterviewDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityInterviewDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityInterviewDTO.setProfilePicUrl(null);
            }
            // activityInterviewDTO.setInterviewScheduledBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
//                            + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName())

            Long interviewedRecruiter = jobMatchConsultancyRepository.getLastActionPerfomedRecruiterId(jobMatchStateResultSet.getJobMatchId());
            if (interviewedRecruiter != null) {
                User user = userRepository.getById(interviewedRecruiter);
                activityInterviewDTO.setInterviewScheduledBy(user.getFirstName() + " " + user.getLastName());
            } else {
                activityInterviewDTO.setInterviewScheduledBy("NA");
            }
            activityInterviewDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
            activityInterviewDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
            activityInterviewDTO.setInterviewStatus(jobMatchStateResultSet.getInterviewStatus());
            activityInterviewDTO.setInterviewAcceptStatus(jobMatchStateResultSet.getHiredStatus());

            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setJobFunctionName(String.valueOf(applicantDetails.getJobFunction()));


            // Get interview details object
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setInterviewDetails(interviewObj);
            if (interviewObj != null) {
                InterviewFeedback interviewFeedback = interviewFeedbackRepository
                        .findByInterviewId(interviewObj.getInterviewId());
                if (interviewFeedback != null) {
                    activityInterviewDTO.setInterviewFeedbackStatus(interviewFeedback.getStatus());
                    activityInterviewDTO.setCommunicationLevel(interviewFeedback.getCommunicationLevel());
                    activityInterviewDTO.setRecruiterComments(interviewFeedback.getRecruiterComments());
                    activityInterviewDTO.setKnowledgeLevel(interviewFeedback.getKnowledgeLevel());
                }
            }
            activityInterviewDTOs.add(activityInterviewDTO);
        }
        response.setActivityInterviewDTO(activityInterviewDTOs);

        return response;
    }

    public ActivityInterviewResponse getAllInterviewProfilesForJobData(Long consultantId, long recruiterId, String role, int status,
                                                                       Integer pageNo, Integer pageSize) {
        ActivityInterviewResponse res = getAllInterviewProfilesForJob(consultantId, recruiterId, status, role, pageNo,
                pageSize);
        return res;
    }

    private ActivityInterviewResponse getAllInterviewProfilesForJob(Long consultantId, long recruiterId, int status, String role,
                                                                    Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<JobMatchStateResultSet> jobMatchRs = null;

        List<Long> users = new ArrayList<>();
        List<Long> collabJobIds = new ArrayList<>();

        if (role.equals("COMPANY_ADMIN")) {
            users.addAll(getUserIds(consultantId, recruiterId, 0, role, 2));
            jobMatchRs = jobMatchingRepository.getAllInterviewsListforJobByTeam(users, pageable);
        } else {
            List<JobPost> jobsList = jobPostRepository.findJobByUserIdOrCollaboratorIdAndStatus(recruiterId, String.valueOf(recruiterId));
            for (JobPost job : jobsList) {
                collabJobIds.add(job.getJobPostId());
                String collbrator = job.getCollaboratorId();
                users.add(job.getUserId());
                if (collbrator != null && collbrator.contains(",")) {
                    String[] collabratorArray = Stream
                            .of(collbrator)
                            .map(str -> str.split("\\,"))
                            .findFirst()
                            .get();
                    for (String id : collabratorArray) {
                        users.add(Long.parseLong(id));
                    }
                } else {
                    if (collbrator.trim() != null && !collbrator.isEmpty())
                        users.add(Long.parseLong(collbrator));
                }
            }
            if (jobsList != null && jobsList.size() > 0) {
                users.add(recruiterId);
            }
            jobMatchRs = jobMatchingRepository.getAllInterviewsListforJobByTeam(users, collabJobIds, pageable);
        }


        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        List<Long> jobIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
            jobIds.add(jobMatchStateResultSet.getJobPostId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<ApplicantDetails> applicantDetailsObj = applicantDetailsRepository
                .findByUsersDetails(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);
        List<JobPost> jobIdObj = jobPostRepository.findAllById(jobIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));
        Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsObj.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
        Map<Long, JobPost> jobDetailsMap = jobIdObj.stream().collect(Collectors.toMap(JobPost::getJobPostId, b -> b));

        ActivityInterviewResponse response = new ActivityInterviewResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityInterviewDTO> activityInterviewDTOs = new ArrayList<>();
        ActivityInterviewDTO activityInterviewDTO = null;
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            activityInterviewDTO = new ActivityInterviewDTO();
            activityInterviewDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());

            activityInterviewDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                    ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                    : "");
            activityInterviewDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getCountry());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityInterviewDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityInterviewDTO.setProfilePicUrl(null);
            }

            activityInterviewDTO
                    .setInterviewScheduledBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
                            + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
            activityInterviewDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
            activityInterviewDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
            activityInterviewDTO.setJobFunctionName(
                    applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId()).getJobFunction());
            activityInterviewDTO.setSecondaryJobFunctionName(
                    applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId()).getSecondaryJobFunction());

            String collabrators = jobDetailsMap.get(jobMatchStateResultSet.getJobPostId()).getCollaboratorId();

            activityInterviewDTO.setJobTitle(jobDetailsMap.get(jobMatchStateResultSet.getJobPostId()).getTitle());

            if (collabrators != null && !collabrators.isEmpty() && collabrators.contains(",")) {

                List<Long> ids = Stream.of(collabrators.split(",")).map(Long::parseLong).collect(Collectors.toList());

                List<User> usersInfo = userRepository.findByConsultancyUsers(ids);
                List<UserDTO> usersList = new ArrayList<UserDTO>();

                for (User user : usersInfo) {
                    UserDTO userDto = new UserDTO();
                    userDto.setFirstName(user.getFirstName());
                    userDto.setLastName(user.getLastName());
                    userDto.setEmail(user.getWorkEmail());
                    userDto.setUserId(user.getUserId());
                    userDto.setCompanyId(user.getCompanyId());
                    userDto.setConsultancyId(user.getConsultancyId());
                    userDto.setPhoneNumber(user.getPhoneNumber());
                    usersList.add(userDto);
                }

                activityInterviewDTO.setUsersList(usersList);
            } else if (collabrators != null && !collabrators.isEmpty()) {

                List<Long> ids = new ArrayList<Long>();
                ids.add(Long.valueOf(collabrators));
                List<User> usersInfo = userRepository.findByConsultancyUsers(ids);
                List<UserDTO> usersList = new ArrayList<UserDTO>();

                for (User user : usersInfo) {
                    UserDTO userDto = new UserDTO();
                    userDto.setFirstName(user.getFirstName());
                    userDto.setLastName(user.getLastName());
                    userDto.setEmail(user.getWorkEmail());
                    userDto.setUserId(user.getUserId());
                    userDto.setCompanyId(user.getCompanyId());
                    userDto.setConsultancyId(user.getConsultancyId());
                    userDto.setPhoneNumber(user.getPhoneNumber());
                    usersList.add(userDto);
                }

                activityInterviewDTO.setUsersList(usersList);

            } else {
                List<UserDTO> usersList = new ArrayList<UserDTO>();
                activityInterviewDTO.setUsersList(usersList);
            }

            // Get interview details object
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setInterviewDetails(interviewObj);
            InterviewFeedback interviewFeedback = interviewFeedbackRepository
                    .findByInterviewId(interviewObj.getInterviewId());
            if (interviewFeedback != null) {
                activityInterviewDTO.setInterviewStatus(interviewFeedback.getStatus());
            } else {
                activityInterviewDTO.setInterviewStatus(InterviewStatus.INTERVIEW_SCHEDULED.getValue());
            }

            activityInterviewDTOs.add(activityInterviewDTO);
        }
        response.setActivityInterviewDTO(activityInterviewDTOs);

        return response;
    }

    public ActivityInterviewResponse getInterviewProfilesForRecruiter(long recruiterId, Integer pageNo,
                                                                      Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());
        Page<JobMatchStateResultSet> jobMatchRs = jobMatchingRepository.getUpcomingInterviews(recruiterId,
                InterviewStatus.INTERVIEW_SCHEDULED.getValue(), pageable);

        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        ActivityInterviewResponse response = new ActivityInterviewResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityInterviewDTO> activityInterviewDTOs = new ArrayList<>();
        ActivityInterviewDTO activityInterviewDTO = null;
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            activityInterviewDTO = new ActivityInterviewDTO();
            activityInterviewDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityInterviewDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                    ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                    : "");
            activityInterviewDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getCountry());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityInterviewDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityInterviewDTO.setProfilePicUrl(null);
            }
            activityInterviewDTO
                    .setInterviewScheduledBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
                            + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
            activityInterviewDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
            activityInterviewDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());

            // Get interview details object
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setInterviewDetails(interviewObj);
            InterviewFeedback interviewFeedback = interviewFeedbackRepository
                    .findByInterviewId(interviewObj.getInterviewId());
            if (interviewFeedback != null) {
                activityInterviewDTO.setInterviewStatus(interviewFeedback.getStatus());
            } else {
                activityInterviewDTO.setInterviewStatus(InterviewStatus.INTERVIEW_SCHEDULED.getValue());
            }

            activityInterviewDTOs.add(activityInterviewDTO);
        }
        response.setActivityInterviewDTO(activityInterviewDTOs);

        return response;
    }

    public ActivityHiredResponse getHiredProfilesForJob(long consultantId, long recruiterId, long jobId, String role, int status, int filter,
                                                        Integer pageNo, Integer pageSize) {
        ActivityHiredResponse res = getHiredProfilesForJob(consultantId, recruiterId, jobId, role, InterviewStatus.HIRED.getValue(), status,
                filter, pageNo, pageSize);
        return res;
    }

    // status==1-Hired-7 , 2->offer sent, 3- offer accepted, 4->Applicant Joined
    private ActivityHiredResponse getHiredProfilesForJob(long consultantId, long recruiterId, long jobId, String role, int jobApplState, int status,
                                                         int filter, Integer pageNo, Integer pageSize) {
        // Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<JobMatchStateResultSet> jobMatchRs = null;

        List<Long> userIds = new ArrayList<>();
        userIds.addAll(getUserIds(consultantId, recruiterId, jobId, role, filter));

        ActivityHiredResponse response = new ActivityHiredResponse();

        jobMatchRs = jobMatchingRepository.getHiredProfilesByInterviewStatusforUsers(jobId, jobApplState, userIds,
                pageable);

        StatusDTO statusDTO = dashboardService.getStats(jobId, userIds);
        response.setCountsInfo(statusDTO);

        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);

        List<ApplicantDetails> applicantDetailsObj = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        List<Interview> interviewList = interviewRepository.findByJobPostIdAndApplicantId(jobId, applicantIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsObj.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        Map<Long, Interview> interviewDetailsMap = interviewList.stream()
                .collect(Collectors.toMap(Interview::getApplicantId, b -> b));
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityHiredDTO> activityHiredDTOs = new ArrayList<>();
        ActivityHiredDTO activityHiredDTO = null;
        List<Long> offerCreatedIds = new ArrayList<>();
        if (role.equals("COMPANY_ADMIN")) {
            offerCreatedIds.addAll(userRepository.findUsersIdsByConsultancyIdAndEnabled(consultantId, true, recruiterId, null));
            offerCreatedIds.add(recruiterId);
        } else if (role.equals("HR_MANAGER")) {
            offerCreatedIds.addAll(getUserIdsByFilter(filter, recruiterId, jobId));
        }
        if (status == 1) {
            activityHiredDTOs.addAll(getHiredUsersList(1, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, -1, offerCreatedIds));
            response.setActivityHiredDTO(activityHiredDTOs);
        } else if (status == 0) {
            activityHiredDTOs.addAll(getHiredUsersList(0, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, -1, offerCreatedIds));
            response.setActivityHiredDTO(activityHiredDTOs);
        } else {
            if (status == 4) {
                activityHiredDTOs.addAll(getHiredUsersList(2, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, 7, offerCreatedIds));
            }
            if (status == 2) {
                activityHiredDTOs.addAll(getHiredUsersList(2, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, 2, offerCreatedIds));
            }
            if (status == 3) {
                activityHiredDTOs.addAll(getHiredUsersList(2, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, 4, offerCreatedIds));
                activityHiredDTOs.addAll(getHiredUsersList(2, jobMatchRs, applicantMap, consultancyMap, recruiterMap, applicantDetailsMap, interviewDetailsMap, 8, offerCreatedIds));
            }

            response.setActivityHiredDTO(activityHiredDTOs);
        }

        return response;
    }

    private List<ActivityHiredDTO> getHiredUsersList(int filter, Page<JobMatchStateResultSet> jobMatchRs, Map<Long, Applicant> applicantMap, Map<Long, String> consultancyMap, Map<Long, User> recruiterMap, Map<Long, ApplicantDetails> applicantDetailsMap,
                                                     Map<Long, Interview> interviewDetailsMap, int status, List<Long> userIds) {
        List<ActivityHiredDTO> activityHiredDTOs = new ArrayList<>();


        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            ActivityHiredDTO activityHiredDTO = new ActivityHiredDTO();
            // Get offer details object
            OfferDetails offerDetailsObj = null;
            boolean addData = true;
            activityHiredDTO.setUpdateDate(jobMatchStateResultSet.getUpdatedDate());

            if (filter == 2) {
                offerDetailsObj = offerDetailsRepository.findByJobPostIdAndApplicantId(
                        jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId(), status);
                addData = offerDetailsObj != null;

            } else {
                offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndReceuiterIds(jobMatchStateResultSet.getApplicantId(), jobMatchStateResultSet.getJobPostId(), userIds);
            }
            if (offerDetailsObj != null) {
                activityHiredDTO.setOfferDetails(offerDetailsObj);
                //in Hired we have to exclude offer info
                if (filter == 1) {
                    addData = false;
                }
            }
            if (addData) {
                activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                activityHiredDTO
                        .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                                + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                activityHiredDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                        ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                        : "");
                activityHiredDTO
                        .setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

                String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                try {
                    activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                } catch (IOException e) {
                    activityHiredDTO.setProfilePicUrl(null);
                }

//                activityHiredDTO.setHiredBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
//                        + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());

                Long recruiterId = jobMatchConsultancyRepository.getLastActionPerfomedRecruiterId(jobMatchStateResultSet.getJobMatchId());
                if (recruiterId != null) {
                    User user = userRepository.getById(recruiterId);
                    activityHiredDTO.setHiredBy(user.getFirstName() + " " + user.getLastName());
                } else {
                    activityHiredDTO.setHiredBy("NA");
                }
                activityHiredDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
                activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
                activityHiredDTO.setJobFunctionName(
                        applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId()).getJobFunction());

                Interview interview = interviewDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                InterviewDTO interviewDTO = new InterviewDTO();
                interviewDTO.setInterviewDate(interview.getInterviewDate());
                interviewDTO.setInterviewDescription(interview.getInterviewDescription());
                interviewDTO.setInterviewEndTime(interview.getInterviewEndTime().toLocalTime().toString());
                interviewDTO.setInterviewStartTime(interview.getInterviewStartTime().toLocalTime().toString());
                interviewDTO.setInterviewLocation(interview.getInterviewLocation());
                interviewDTO.setInterviewTitle(interview.getInterviewTitle());
                interviewDTO.setInterviewMode(interview.getInterviewMode());
                interviewDTO.setInterviewVideoLink(interview.getInterviewVideoLink());
                interviewDTO.setInterviewId(interview.getInterviewId());
                activityHiredDTO.setInterviewDetails(interviewDTO);

                activityHiredDTOs.add(activityHiredDTO);
                //activityHiredDTOs.sort(Comparator.comparing(ActivityHiredDTO::getUpdateDate).reversed());
            }
        }

        Collections.sort(activityHiredDTOs, (emp1, emp2) -> emp2.getUpdateDate().compareTo(emp1.getUpdateDate()));

        return activityHiredDTOs;
    }

    public ActivityRejectedResponse getRejectedProfilesForJob(long consultantId, long recruiterId, long jobId, String role, int status, int filter,
                                                              Integer pageNo, Integer pageSize) {
        ActivityRejectedResponse res = getRejectedProfilesListForJob(consultantId, recruiterId, jobId, InterviewStatus.REJECTED.getValue(), status, role, filter, pageNo, pageSize);
        return res;
    }

    // status ==1 offer rejcted, 2 applicant did't join, 3 No-Show, 4 Not-fit and 0
    // all
    private ActivityRejectedResponse getRejectedProfilesListForJob(long consultantId, long recruiterId, long jobId, int jobApplState,
                                                                   int status, String role, int filter, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("updatedDate").descending());

        Page<JobMatchStateResultSet> jobMatchRs = null;
        List<Long> statusIds = new ArrayList<Long>();
        List<Long> userIds = new ArrayList<>();
        userIds.addAll(getUserIds(consultantId, recruiterId, jobId, role, filter));

        if (status == 3) {
            statusIds.add(11L);
        } else if (status == 4) {
            statusIds.add(10L);
        } else if (status == 2) {
            //aplicant did't join
            statusIds.add(15L);
        } else if (status == 0) {
            statusIds.add(11L);
            statusIds.add(10L);
            // statusIds.add(8L);
            statusIds.add(18L);
            statusIds.add(15L);
        } else {
            statusIds.add(18L);
        }

        if (userIds != null && userIds.size() > 0)
            jobMatchRs = jobMatchingRepository.getInterviewsListforJobByTeam(jobId, statusIds, userIds, pageable);
        else
            return new ActivityRejectedResponse();
        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);
        List<ApplicantDetails> applicantDetailsObj = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsObj.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ActivityRejectedResponse response = new ActivityRejectedResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityRejectedDTO> activityRejectedDTOs = new ArrayList<>();
        ActivityRejectedDTO activityRejectedDTO = null;
        // status ==1 offer rejcted, 2 applicant did't join, 3 No-Show, 4 Not-fit and 0
        // all

        StatusDTO statusDTO = dashboardService.getStats(jobId, userIds);
        response.setCountsInfo(statusDTO);


        if (status == 1) {
            for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {

                OfferDetails offerDetailsObj = offerDetailsRepository.findByJobPostIdAndApplicantId(jobId,
                        jobMatchStateResultSet.getApplicantId(), 5);
                if (offerDetailsObj != null) {
                    activityRejectedDTO = getRejectedApplicantsInfo(jobMatchStateResultSet, consultancyMap, recruiterMap, applicantMap, applicantDetailsMap);
                    activityRejectedDTO.setOfferDetails(offerDetailsObj);
                    activityRejectedDTOs.add(activityRejectedDTO);
                }
            }
            response.setActivityRejectedDTO(activityRejectedDTOs);
        } else if (status == 2) {
            for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {

                OfferDetails offerDetailsObj = offerDetailsRepository.findByJobPostIdAndApplicantId(jobId,
                        jobMatchStateResultSet.getApplicantId(), 6);
                if (offerDetailsObj != null) {
                    activityRejectedDTO = getRejectedApplicantsInfo(jobMatchStateResultSet, consultancyMap, recruiterMap, applicantMap, applicantDetailsMap);
                    activityRejectedDTO.setOfferDetails(offerDetailsObj);
                    activityRejectedDTOs.add(activityRejectedDTO);
                }
            }
            response.setActivityRejectedDTO(activityRejectedDTOs);
        } else if (status == 3 || status == 4) {
            for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
                activityRejectedDTO = getRejectedApplicantsInfo(jobMatchStateResultSet, consultancyMap, recruiterMap, applicantMap, applicantDetailsMap);
                activityRejectedDTOs.add(activityRejectedDTO);
            }
            response.setActivityRejectedDTO(activityRejectedDTOs);
        } else if (status == 0) {
            for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
                activityRejectedDTO = getRejectedApplicantsInfo(jobMatchStateResultSet, consultancyMap, recruiterMap, applicantMap, applicantDetailsMap);
                OfferDetails offerDetailsObj = offerDetailsRepository.findByJobPostIdAndApplicantId(jobId,
                        jobMatchStateResultSet.getApplicantId());
                if (offerDetailsObj != null) {
                    activityRejectedDTO.setOfferDetails(offerDetailsObj);
                }
                activityRejectedDTOs.add(activityRejectedDTO);
            }

            response.setActivityRejectedDTO(activityRejectedDTOs);
        }

        return response;
    }

    private ActivityRejectedDTO getRejectedApplicantsInfo(JobMatchStateResultSet jobMatchStateResultSet, Map<Long, String> consultancyMap,
                                                          Map<Long, User> recruiterMap, Map<Long, Applicant> applicantMap, Map<Long, ApplicantDetails> applicantDetailsMap) {

        ActivityRejectedDTO activityRejectedDTO = new ActivityRejectedDTO();
        activityRejectedDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
        activityRejectedDTO
                .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                        + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
        activityRejectedDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                : "");
        activityRejectedDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getCountry());

        String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
        try {
            activityRejectedDTO.setProfilePicUrl(imageService.getImage(imageUrl));
        } catch (IOException e) {
            activityRejectedDTO.setProfilePicUrl(null);
        }
//        activityRejectedDTO
//                .setRejectedBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName() + " "
//                        + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
        Long recruiterId = jobMatchConsultancyRepository.getLastActionPerfomedRecruiterId(jobMatchStateResultSet.getJobMatchId());
        if (recruiterId != null) {
            User user = userRepository.getById(recruiterId);
            activityRejectedDTO.setRejectedBy(user.getFirstName() + " " + user.getLastName());
        } else {
            activityRejectedDTO.setRejectedBy("NA");
        }
        activityRejectedDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
        activityRejectedDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
        activityRejectedDTO.setInterviewStatus(jobMatchStateResultSet.getInterviewStatus());
        activityRejectedDTO.setJobFunctionName(
                applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId()).getJobFunction());
        activityRejectedDTO
                .setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

        return activityRejectedDTO;
    }

    private ActivityRejectedResponse getRejectedProfileForJob(long recruiterId, long jobId, int jobApplState,
                                                              int status, int filter, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<JobMatchStateResultSet> jobMatchRs = null;

        List<Long> userIds = getUserIdsByFilter(filter, recruiterId, jobId);

        jobMatchRs = jobMatchingRepository.getRejectedProfilesByInterviewStatusforUsers(jobId, jobApplState, userIds,
                pageable);

        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            if (jobMatchStateResultSet.getConsultancyId() != null)
                consultancyIds.add(jobMatchStateResultSet.getConsultancyId());
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        ActivityRejectedResponse response = new ActivityRejectedResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ActivityRejectedDTO> activityRejectedDTOs = new ArrayList<>();
        ActivityRejectedDTO activityRejectedDTO = null;
        for (JobMatchStateResultSet jobMatchStateResultSet : jobMatchRs) {
            activityRejectedDTO = new ActivityRejectedDTO();
            activityRejectedDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityRejectedDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityRejectedDTO.setConsultancyName(jobMatchStateResultSet.getConsultancyId() != null
                    ? consultancyMap.get(jobMatchStateResultSet.getConsultancyId())
                    : "");
            activityRejectedDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getCountry());
            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityRejectedDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityRejectedDTO.setProfilePicUrl(null);
            }
            activityRejectedDTO.setRejectedBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
                    + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
            activityRejectedDTO.setConsultancy(jobMatchStateResultSet.getConsultancyId() != null);
            activityRejectedDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchId());
            activityRejectedDTOs.add(activityRejectedDTO);
        }
        response.setActivityRejectedDTO(activityRejectedDTOs);

        return response;
    }

    public Long scheduleInterviewForApplicant(long recruiterId, long jobId, long applicantId,
                                              @Valid InterviewDTO interviewDTO, long consultantId) throws FirebaseMessagingException {
        Interview interviewModel = new Interview();
        interviewModel.setApplicantId(applicantId);
        interviewModel.setCreatedDate(new Date());
        interviewModel.setUpdatedDate(new Date());

        interviewModel.setInterviewDate(interviewDTO.getInterviewDate());
        interviewModel.setInterviewDescription(interviewDTO.getInterviewDescription());
        interviewModel.setInterviewStartTime(Time.valueOf(interviewDTO.getInterviewStartTime()));
        interviewModel.setInterviewEndTime(Time.valueOf(interviewDTO.getInterviewEndTime()));
        interviewModel.setInterviewTitle(interviewDTO.getInterviewTitle());
        interviewModel.setInterviewLocation(interviewDTO.getInterviewLocation());
        // 1 - F2F, 2- Skype, 3- audio, 4-video
        interviewModel.setInterviewMode(interviewDTO.getInterviewMode());
        interviewModel.setInterviewVideoLink(interviewDTO.getInterviewVideoLink());
        interviewModel.setJobPostId(jobId);
        interviewModel.setRecruiterId(recruiterId);
        interviewRepository.save(interviewModel);
        // Update status in jobMatchTable
        if (interviewDTO.isConsultancy()) {
            JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId,
                    applicantId);
            jobMactCons.setUpdatedByRecId(recruiterId);
            jobMactCons.setInterviewStatus(InterviewStatus.INTERVIEW_SCHEDULED.getValue());
            jobMactCons.setUpdatedDate(new Date());
            jobMactCons.setInterviewScheduledDate(new Date());
            jobMactCons.setInterviewScheduledUserId(recruiterId);
            jobMactCons.setLastActionPerformedRecruiterId(recruiterId);
            jobMatchConsultancyRepository.save(jobMactCons);

            String message = "Interview scheduled for " + interviewDTO.getJobTitle() + " role with "
                    + interviewDTO.getApplicantName() + " from " + interviewDTO.getConsultantName() + " by "
                    + interviewDTO.getRecruiterName();
            alertService.saveAlertInfo(recruiterId, message, consultantId);

            User user = userRepository.findById(recruiterId).get();
            User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();
            Long companyId = user.getCompanyId();
            Company company = companyRepository.findById(companyId).get();
            message = "Interview has been scheduled with " + interviewDTO.getApplicantName() + " for "
                    + interviewDTO.getJobTitle() + " job of " + company.getName() + ".";
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, consultantId);

            String notificationMessage = "Interview has been scheduled with " + interviewDTO.getApplicantName() + " for " + interviewDTO.getJobTitle() + " job of " + company.getName() + ".";
            if (updatedConsultancyUser.getNotificationToken() != null && !updatedConsultancyUser.getNotificationToken().equals("")) {
                firebaseMessagingService.prepareNotifObject("Interview scheduled", notificationMessage, updatedConsultancyUser.getNotificationToken());
                notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Interview scheduled", notificationMessage);
            }
//            String content = baseUrl + "/redirect/?to=" + "/signup/applicant/";
//            try {
//                List<MandrillMessage.MergeVar> globalMergeVars = new ArrayList();
//                MandrillMessage.MergeVar mergeVar = new MandrillMessage.MergeVar();
//                mergeVar.setName("Applicant Name");
//                mergeVar.setContent(interviewDTO.getApplicantName());
//                globalMergeVars.add(mergeVar);
//                mergeVar = new MandrillMessage.MergeVar();
//                mergeVar.setName("Company Name");
//                mergeVar.setContent(company.getName());
//                globalMergeVars.add(mergeVar);
//                emailService.sendMail(interviewDTO.getApplicantName(), "sudheer7786@gmail.com",
//                        "WUS-Consultancy-Interview-Scheduled", null, content);
//            } catch (MandrillApiError e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        return interviewModel.getInterviewId();
    }

    public Long updateInterviewForApplicant(long recruiterId, Long interviewId, InterviewDTO interviewDTO, long consultantId) throws Exception {
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        boolean isRescheduled = false;

        if (!interviewOpt.isPresent()) {
            throw new Exception("Interview details not found");
        }
        Interview interviewModel = interviewOpt.get();
        if (interviewDTO.getInterviewTitle() != null) {
            interviewModel.setInterviewTitle(interviewDTO.getInterviewTitle());
        }
        if (interviewDTO.getInterviewDate() != null) {
            interviewModel.setInterviewDate(interviewDTO.getInterviewDate());
            isRescheduled = true;
        }
        if (interviewDTO.getInterviewDescription() != null)
            interviewModel.setInterviewDescription(interviewDTO.getInterviewDescription());
        if (interviewDTO.getInterviewStartTime() != null) {
            interviewModel.setInterviewStartTime(Time.valueOf(interviewDTO.getInterviewStartTime()));
            isRescheduled = true;
        }
        if (interviewDTO.getInterviewEndTime() != null) {
            interviewModel.setInterviewEndTime(Time.valueOf(interviewDTO.getInterviewEndTime()));
            isRescheduled = true;
        }
        if (interviewDTO.getInterviewLocation() != null)
            interviewModel.setInterviewLocation(interviewDTO.getInterviewLocation());
        if (interviewDTO.getInterviewMode() != 0)
            // 1 - F2F, 2- Skype, 3- audio, 4-video
            interviewModel.setInterviewMode(interviewDTO.getInterviewMode());
        if (interviewDTO.getInterviewVideoLink() != null)
            interviewModel.setInterviewVideoLink(interviewDTO.getInterviewVideoLink());
//
//		if (interviewDTO.getInterviewStatus() == 3) {
//			updateInterviewStatus(interviewDTO.getRecruiterId(), interviewDTO.getJobId(), interviewDTO.getApplicantId(),
//					interviewDTO.getInterviewStatus());
//		}

        interviewModel.setUpdatedDate(new Date());
        interviewRepository.save(interviewModel);
        //if (interviewDTO.isConsultancy()) {
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(interviewModel.getJobPostId(),
                interviewModel.getApplicantId());
        jobMactCons.setUpdatedByRecId(recruiterId);
        jobMactCons.setLastActionPerformedRecruiterId(recruiterId);
        jobMactCons.setUpdatedDate(new Date());
        if (isRescheduled) {
            jobMactCons.setInterviewStatus(InterviewStatus.RESCHEDULED_INTERVIEW.getValue());
            jobMactCons.setRescheduledInterviewDate(new Date());
            jobMactCons.setInterviewRescheduledRequestedUserId(recruiterId);
            jobMatchConsultancyRepository.save(jobMactCons);
            User user = userRepository.findById(interviewModel.getRecruiterId()).get();
            User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();
            Long companyId = user.getCompanyId();
            Company company = companyRepository.findById(companyId).get();

            String message = "Interview has been rescheduled with " + interviewDTO.getApplicantName() + " for "
                    + interviewDTO.getJobTitle() + " job of " + company.getName() + ".";
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, consultantId);

            String notificationMessage = "Interview has been rescheduled with " + interviewDTO.getApplicantName() + " for " + interviewDTO.getJobTitle() + " job of " + company.getName() + ".";
            if (updatedConsultancyUser.getNotificationToken() != null && !updatedConsultancyUser.getNotificationToken().equals("")) {
                firebaseMessagingService.prepareNotifObject("Interview rescheduled", notificationMessage, updatedConsultancyUser.getNotificationToken());
                notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Interview rescheduled", notificationMessage);
            }
        }
        jobMatchConsultancyRepository.save(jobMactCons);

        //}
        return interviewModel.getInterviewId();
    }

    // Updating User status in matching table as well.
    public long submitFeedback(long interviewId, InterviewFeedbackDTO feedbackDTO, long consultantId) throws FirebaseMessagingException {
        InterviewFeedback feedbackModel = interviewFeedbackRepository.findByInterviewId(interviewId);
        if (feedbackModel == null) {
            feedbackModel = new InterviewFeedback();
            feedbackModel.setCreatedDate(new Date());

        }
        feedbackModel.setCommunicationLevel(feedbackDTO.getCommunicationLevel());
        feedbackModel.setInterviewId(interviewId);
        feedbackModel.setKnowledgeLevel(feedbackDTO.getKnowledgeLevel());
        feedbackModel.setUpdatedDate(new Date());
        feedbackModel.setStatus(feedbackDTO.getStatus());
        feedbackModel.setRecruiterComments(feedbackDTO.getRecruiterComments());
        feedbackModel.setRecruiterId(feedbackDTO.getRecruiterId());
        feedbackModel.setApplicantId(feedbackDTO.getApplicantId());
        feedbackModel.setJobId(feedbackDTO.getJobId());

        interviewFeedbackRepository.save(feedbackModel);
        // if (feedbackDTO.getStatus() == 7 || feedbackDTO.getStatus() == 8) {
        updateInterviewStatus(feedbackDTO.getRecruiterId(), feedbackDTO.getJobId(), feedbackDTO.getApplicantId(),
                feedbackDTO.getStatus());
        // }

        String message = "Interview with " + feedbackDTO.getApplicantName() + " from " + feedbackDTO.getConsultantName()
                + " for " + feedbackDTO.getJobTitle() + " role has been completed by " + feedbackDTO.getRecruiterName();
        alertService.saveAlertInfo(feedbackDTO.getRecruiterId(), message, consultantId);

        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(feedbackDTO.getJobId(),
                feedbackDTO.getApplicantId());
        User user = userRepository.findById(feedbackDTO.getRecruiterId()).get();
        Long companyId = user.getCompanyId();
        User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();

        Company company = companyRepository.findById(companyId).get();
        message = "Feedback provided for " + feedbackDTO.getApplicantName() + " for " + feedbackDTO.getJobTitle() + " job interview of " + company.getName() + ".";
        alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, jobMactCons.getConsultancyId());

        updateFeedbackAlerts(feedbackDTO, consultantId, user, company, jobMactCons, updatedConsultancyUser);


        return feedbackModel.getFeedbackId();
    }

    // Updating User status in matching table as well.
    public InterviewFeedbackDTO getFeedback(long interviewId) {
        InterviewFeedback feedbackModel = interviewFeedbackRepository.findByInterviewId(interviewId);
        if (feedbackModel == null) {
            feedbackModel = new InterviewFeedback();
        }

        InterviewFeedbackDTO feedbackDTO = new InterviewFeedbackDTO();
        feedbackDTO.setApplicantId(feedbackModel.getApplicantId());
        feedbackDTO.setCommunicationLevel(feedbackModel.getCommunicationLevel());
        feedbackDTO.setConsComments(feedbackModel.getConsComments());
        feedbackDTO.setJobId(feedbackModel.getJobId());
        feedbackDTO.setKnowledgeLevel(feedbackModel.getKnowledgeLevel());
        feedbackDTO.setRecruiterComments(feedbackModel.getRecruiterComments());
        feedbackDTO.setRecruiterId(feedbackModel.getRecruiterId());
        feedbackDTO.setStatus(feedbackModel.getStatus());
        feedbackDTO.setConsOption(feedbackModel.getConsOption());

        return feedbackDTO;
    }

    // Updating User status in matching table as well.
    public long updateFeedback(long interviewId, InterviewFeedbackDTO feedbackDTO, long consultantId) throws OfferNotFoundException, FirebaseMessagingException {
        InterviewFeedback feedbackModel = interviewFeedbackRepository.findByInterviewId(interviewId);
        if (feedbackModel == null) {
            throw new OfferNotFoundException("No Offer details found with given id");
        }
        feedbackModel.setUpdatedDate(new Date());
        feedbackModel.setStatus(feedbackDTO.getStatus());
        feedbackModel.setConsOption(feedbackDTO.getConsOption() != null ? feedbackDTO.getConsOption() : "");
        interviewFeedbackRepository.save(feedbackModel);
        // if (feedbackDTO.getStatus() == 7 || feedbackDTO.getStatus() == 8) {
        updateInterviewStatus(feedbackDTO.getRecruiterId(), feedbackDTO.getJobId(), feedbackDTO.getApplicantId(),
                feedbackDTO.getStatus());
        // }

        User recruiterUser = userRepository.findById(feedbackDTO.getRecruiterId()).get();
        Long companyId = recruiterUser.getCompanyId();
        Company company = companyRepository.findById(companyId).get();
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(feedbackDTO.getJobId(), feedbackDTO.getApplicantId());
        User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();

        updateFeedbackAlerts(feedbackDTO, consultantId, recruiterUser, company, jobMactCons, updatedConsultancyUser);
        // 10 ,11

        return feedbackModel.getFeedbackId();
    }

    public void updateFeedbackAlerts(InterviewFeedbackDTO feedbackDTO, long consultantId, User recruiterUser, Company company, JobMatchConsultancy jobMactCons,
                                     User updatedConsultancyUser) throws FirebaseMessagingException {
        if (feedbackDTO.getStatus() == 7) {

            String message = feedbackDTO.getApplicantName() + " from " + feedbackDTO.getConsultantName()
                    + " is hired with current status as selected for " + feedbackDTO.getJobTitle() + " job by "
                    + feedbackDTO.getRecruiterName();
            alertService.saveAlertInfo(recruiterUser.getUserId(), message, recruiterUser.getConsultancyId());

            message = feedbackDTO.getApplicantName() + " selected by " + feedbackDTO.getRecruiterName() + " from " + company.getName() + " for " + feedbackDTO.getJobTitle() + " job.";
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, jobMactCons.getConsultancyId());

            String notificationMessage = feedbackDTO.getApplicantName() + " selected by " + feedbackDTO.getRecruiterName() + " from " + company.getName() + " for " + feedbackDTO.getJobTitle() + " job.";
            if (updatedConsultancyUser.getNotificationToken() != null && !updatedConsultancyUser.getNotificationToken().equals("")) {
                firebaseMessagingService.prepareNotifObject("Applicant selected", notificationMessage, updatedConsultancyUser.getNotificationToken());
                notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Applicant selected", notificationMessage);
            }
        }
        if (feedbackDTO.getStatus() == 10 || feedbackDTO.getStatus() == 11) {

            String message = feedbackDTO.getApplicantName() + " rejected for  " + feedbackDTO.getJobTitle() + " job by "
                    + feedbackDTO.getRecruiterName();
            alertService.saveAlertInfo(feedbackDTO.getRecruiterId(), message, consultantId);

            message = feedbackDTO.getApplicantName() + " rejected by " + feedbackDTO.getRecruiterName() + " from " + company.getName() + " for " + feedbackDTO.getJobTitle() + " job.";
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, jobMactCons.getConsultancyId());

            String notificationMessage = feedbackDTO.getApplicantName() + " rejected by " + feedbackDTO.getRecruiterName() + " from " + company.getName() + " for " + feedbackDTO.getJobTitle() + " job.";
            if (updatedConsultancyUser.getNotificationToken() != null && !updatedConsultancyUser.getNotificationToken().equals("")) {
                firebaseMessagingService.prepareNotifObject("Applicant rejected", notificationMessage, updatedConsultancyUser.getNotificationToken());
                notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Applicant rejected", notificationMessage);
            }
        }
    }

    public Long updateInterviewAcceptStatus(long recruiterId, long jobId, long applicantId, long consultantId, Integer applStatus, long userId) {
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId,
                applicantId);
        jobMactCons.setHiredStatus(applStatus);
        jobMactCons.setUpdatedDate(new Date());
        jobMactCons.setOfferAcceptedDate(new Date());
        jobMactCons.setOfferacceptedUserId(userId);

        jobMatchConsultancyRepository.save(jobMactCons);
        return jobMactCons.getJobMatchConId();
    }

    public Long updateInterviewJoinedStatus(long recruiterId, long jobId, long applicantId, Integer applStatus, long userId) {
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId,
                applicantId);
        jobMactCons.setHiredStatus(applStatus);
        jobMactCons.setUpdatedDate(new Date());
        jobMactCons.setApplicantAoinedStatusUpdatedDate(new Date());
        jobMactCons.setApplicantAoinedStatusUpdatedUserId(userId);
        jobMactCons.setLastActionPerformedRecruiterId(userId);
        jobMatchConsultancyRepository.save(jobMactCons);
        return jobMactCons.getJobMatchConId();
    }

    public Long updateInterviewStatus(long recruiterId, long jobId, long applicantId, Integer applStatus) {
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId,
                applicantId);
        jobMactCons.setUpdatedByRecId(recruiterId);
        jobMactCons.setLastActionPerformedRecruiterId(recruiterId);
        jobMactCons.setUpdatedDate(new Date());
        if (InterviewStatus.HIRED.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.HIRED.getValue());
            jobMactCons.setSelectedDate(new Date());
            jobMactCons.setSelectedUserId(recruiterId);
        } else if (InterviewStatus.REJECTED.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.REJECTED.getValue());
            jobMactCons.setRejectedDate(new Date());
            jobMactCons.setRejectedUserId(recruiterId);
        } else if (InterviewStatus.ON_HOLD.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.ON_HOLD.getValue());
            jobMactCons.setOnHoldDate(new Date());
            jobMactCons.setOnholdUpdatedUserId(recruiterId);
        } else if (InterviewStatus.NO_SHOW.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.NO_SHOW.getValue());
            jobMactCons.setRejectedDate(new Date());
            jobMactCons.setRejectedUserId(recruiterId);
        } else if (InterviewStatus.NOT_FIT.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.NOT_FIT.getValue());
            jobMactCons.setRejectedDate(new Date());
            jobMactCons.setRejectedUserId(recruiterId);
        } else if (InterviewStatus.NO_SHOW_REJECTED.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.NO_SHOW_REJECTED.getValue());
            jobMactCons.setRejectedDate(new Date());
            jobMactCons.setRejectedUserId(recruiterId);
        } else if (OfferStatus.APPLICANT_NOT_JOINED.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.APPLICANT_NOT_JOINED.getValue());
            jobMactCons.setRejectedDate(new Date());
            jobMactCons.setRejectedUserId(recruiterId);
        } else if (InterviewStatus.RESCHEDULED_INTERVIEW.getValue() == applStatus) {
            jobMactCons.setInterviewStatus(InterviewStatus.RESCHEDULED_INTERVIEW.getValue());
            jobMactCons.setRescheduledInterviewDate(new Date());
            jobMactCons.setInterviewRescheduledUserId(recruiterId);
        }


        jobMatchConsultancyRepository.save(jobMactCons);
        return jobMactCons.getJobMatchConId();
    }

    public Long uploadOffer(long recruiterId, long jobId, long applicantId, OfferDetailsDTO offerDetails, long consultantId, long userId) throws FirebaseMessagingException {
        OfferDetails offerModel = new OfferDetails();
        offerModel.setApplicantId(applicantId);
        offerModel.setJobPostId(jobId);
        offerModel.setRecruiterId(recruiterId);
        offerModel.setConsultancyId(offerDetails.getConsultancyId());
        offerModel.setOfferStatus(OfferStatus.OFFER_SENT.getValue());
        offerModel.setCreatedDate(new Date());
        offerModel.setUpdatedDate(new Date());
        offerModel.setJoiningDate(offerDetails.getJoiningDate());
        offerModel.setOfferUrl(offerDetails.getOfferUrl());
        offerDetailsRepository.save(offerModel);

        JobMatchConsultancy jobMactConsUpdate = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId,
                applicantId);
        jobMactConsUpdate.setHiredStatus(offerDetails.getOfferStatus());
        jobMactConsUpdate.setUpdatedDate(new Date());
        jobMactConsUpdate.setOfferSentDate(new Date());
        jobMactConsUpdate.setOffersentUserId(recruiterId);
        jobMactConsUpdate.setLastActionPerformedRecruiterId(recruiterId);
        jobMatchConsultancyRepository.save(jobMactConsUpdate);

        String message = "Offer letter has been sent to " + offerDetails.getApplicantName() + " from "
                + offerDetails.getConsultantName() + " for " + offerDetails.getJobTitle() + " job by "
                + offerDetails.getRecruiterName();

        alertService.saveAlertInfo(recruiterId, message, consultantId);


        User recruiterUser = userRepository.findById(recruiterId).get();
        Company company = companyRepository.findById(recruiterUser.getCompanyId()).get();
        message = "Offer letter recieved for " + offerDetails.getApplicantName() + " by " + offerDetails.getRecruiterName() + " from " + company.getName() + " for the " + offerDetails.getJobTitle() + " job.";
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobId, applicantId);

        alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, consultantId);

        String notificationMessage = "Offer letter recieved for " + offerDetails.getApplicantName() + " by " + offerDetails.getRecruiterName() + " from " + company.getName() + " for the " + offerDetails.getJobTitle() + " job.";
        User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();
        if (updatedConsultancyUser.getNotificationToken() != null && !updatedConsultancyUser.getNotificationToken().equals("")) {
            firebaseMessagingService.prepareNotifObject("Offer letter recieved", notificationMessage, updatedConsultancyUser.getNotificationToken());
            notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Offer letter recieved", notificationMessage);
        }
        return offerModel.getOfferDetailsId();
    }

    public Long updateOfferDetails(long offerDetailsId, OfferDetailsDTO offerDetails, long consultantId, long updatedUserId) throws OfferNotFoundException, FirebaseMessagingException {
        Optional<OfferDetails> offerOpt = offerDetailsRepository.findById(offerDetailsId);
        if (!offerOpt.isPresent()) {
            throw new OfferNotFoundException("No Offer details found with given id");
        }
        OfferDetails offerModel = offerOpt.get();
        offerModel.setOfferStatus(offerDetails.getOfferStatus());
        offerModel.setUpdatedDate(new Date());
        offerModel.setJoiningDate(offerDetails.getJoiningDate());
        offerModel.setOfferUrl(offerDetails.getOfferUrl() != null ? offerDetails.getOfferUrl() : offerModel.getOfferUrl());
        offerModel.setOfferSignedUrl(offerDetails.getOfferSignedUrl() != null ? offerDetails.getOfferSignedUrl() : offerModel.getOfferSignedUrl());
        if (offerModel.getOfferSignedUrl() != null && offerModel.getOfferSignedDate() == null) {
            offerModel.setOfferSignedDate(new Date());
        }
        offerDetailsRepository.save(offerModel);

        User recruiterUser = userRepository.findById(offerModel.getRecruiterId()).get();
        Long companyId = recruiterUser.getCompanyId();
        Company company = companyRepository.findById(companyId).get();
        JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(offerModel.getJobPostId(), offerModel.getApplicantId());
        String consultancyMessage = null;
        String recruiterMeassae = null;

// only consultant will update 8 status
        if (offerDetails.getOfferStatus() == 8) {
            updateInterviewAcceptStatus(offerModel.getRecruiterId(), offerModel.getJobPostId(), offerModel.getApplicantId(), offerModel.getConsultancyId(),
                    offerDetails.getOfferStatus(), updatedUserId);
        }
        // 7 status company will update
        if (offerDetails.getOfferStatus() == 7) {
            updateInterviewJoinedStatus(offerModel.getRecruiterId(), offerModel.getJobPostId(), offerModel.getApplicantId(),
                    offerDetails.getOfferStatus(), updatedUserId);
        }
// 6 status company will update
        if (offerDetails.getOfferStatus() == 6 || offerDetails.getOfferStatus() == 5) {
            if (offerDetails.getOfferStatus() == 5) {
                jobMactCons.setInterviewStatus(InterviewStatus.OFFER_REJECTED.getValue());
                jobMactCons.setRejectedDate(new Date());
                jobMactCons.setOfferrejectedUserId(updatedUserId);
            } else if (offerDetails.getOfferStatus() == 6) {
                jobMactCons.setInterviewStatus(InterviewStatus.APPLICANT_NOT_JOINED.getValue());
                jobMactCons.setRejectedDate(new Date());
                jobMactCons.setRejectedUserId(updatedUserId);
                jobMactCons.setLastActionPerformedRecruiterId(updatedUserId);
            } else {
                updateInterviewStatus(updatedUserId, offerModel.getJobPostId(), offerModel.getApplicantId(),
                        InterviewStatus.REJECTED.getValue());
            }
            recruiterMeassae = offerDetails.getApplicantName() + " rejected for  " + offerDetails.getJobTitle()
                    + " job by " + offerDetails.getRecruiterName();
            consultancyMessage = offerDetails.getApplicantName() + " rejected by " + offerDetails.getRecruiterName() + " from " + company.getName() + " for " + offerDetails.getJobTitle() + " job.";
        }
        jobMactCons.setUpdatedDate(new Date());
        jobMatchConsultancyRepository.save(jobMactCons);
        if (offerDetails.getOfferStatus() == 8) {
            recruiterMeassae = "Offer letter accepted by " + offerDetails.getApplicantName() + " from "
                    + offerDetails.getConsultantName() + " for " + offerDetails.getJobTitle() + " job.";
            consultancyMessage = "Offer letter accepted for " + offerDetails.getApplicantName() + " for " + offerDetails.getJobTitle() + " job, offered by " + company.getName() + ".";

        } else if (offerDetails.getOfferStatus() == 7) {
            recruiterMeassae = offerDetails.getApplicantName() + " from " + offerDetails.getConsultantName()
                    + " joined for " + offerDetails.getJobTitle() + " job.";
            consultancyMessage = offerDetails.getApplicantName() + " joined " + company.getName() + " for " + offerDetails.getJobTitle() + " job.";

            String notificationMessage = offerDetails.getApplicantName() + " joined " + company.getName() + " for " + offerDetails.getJobTitle() + " job..";
            User updatedConsultancyUser = userRepository.findById(jobMactCons.getUpdatedByConsUserId()).get();
            if (updatedConsultancyUser.getNotificationToken() != null) {
                firebaseMessagingService.prepareNotifObject("Applicant joined", notificationMessage, updatedConsultancyUser.getNotificationToken());
                notificationService.saveNotification(updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId(), "Applicant joined", notificationMessage);
            }
        }

        if (consultancyMessage != null && recruiterMeassae != null) {
            alertService.saveAlertInfo(offerModel.getRecruiterId(), recruiterMeassae, consultantId);
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), consultancyMessage, consultantId);
        }
        return offerModel.getOfferDetailsId();
    }

    public List<Long> getUserIds(long consultantId, long userId, long jobId, String role, int filter) {

        List<Long> userIds = new ArrayList<>();
        if (role.equals("COMPANY_ADMIN")) {
            userIds.addAll(getRegisteredUserIdByFilter(filter, consultantId, userId));
        } else if (role.equals("HR_MANAGER")) {
            userIds.addAll(getUserIdsByFilter(filter, userId, jobId));
        }
        return userIds;
    }

    private List<Long> getRegisteredUserIdByFilter(int filter, long consultancyId, long userId) {
        List<Long> userIds = new ArrayList<Long>();
        if (filter == 2 || filter == 1) {
            userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId, null);
        }
        if (filter == 0 || filter == 2) {
            userIds.add(userId);
        }
        return userIds;
    }

    private List<Long> getUserIdsByFilter(int filter, long recruiterId, long jobId) {
        List<Long> userIds = new ArrayList<>();
        if (filter == 0) {
            userIds.add(recruiterId);
        } else if (filter == 1 || filter == 2) {
            Optional<JobPost> jobPost = jobPostRepository.findById(jobId);
            if (jobPost.isPresent()) {
                String collaboratorId = jobPost.get().getCollaboratorId();
                if (collaboratorId != null && !collaboratorId.isEmpty()) {
                    userIds.addAll(Arrays.stream(collaboratorId.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList()));
                }
                userIds.remove(recruiterId);
                if (recruiterId != jobPost.get().getUserId()) {
                    userIds.add(jobPost.get().getUserId());
                }
                User user = userRepository.getById(recruiterId);
                if (user.getCreatedBy() != null) {
                    userIds.add(user.getCreatedBy());
                }
            }
            if (filter == 2) {
                userIds.add(recruiterId);

            }
        }

        return userIds;
    }

    public List<Long> getJobsCollbarators(List<JobPost> jobPostList) {
        List<Long> userIds = new ArrayList<>();

        for (JobPost jobPostObj : jobPostList) {
            Optional<JobPost> jobPost = jobPostRepository.findById(jobPostObj.getJobPostId());
            if (jobPost.isPresent()) {
                String collaboratorId = jobPost.get().getCollaboratorId();
                if (collaboratorId != null && !collaboratorId.isEmpty()) {
                    userIds.addAll(Arrays.stream(collaboratorId.split(","))
                            .map(Long::parseLong)
                            .collect(Collectors.toList()));
                }
                userIds.add(jobPost.get().getUserId());
            }
        }

        return userIds;
    }

}
