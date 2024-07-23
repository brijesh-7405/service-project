/**
 *
 */
package com.workruit.us.application.services;

import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.enums.OfferStatus;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mahesh
 */

@Slf4j
@Service
public class ConsultancyActivityService {

    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired JobFunctionRepository jobFunctionRepository;
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired InterviewRepository interviewRepository;
    private @Autowired InterviewFeedbackRepository interviewFeedbackRepository;
    private @Autowired OfferDetailsRepository offerDetailsRepository;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired DashboardService dashboardService;

    private @Autowired CompanyRepository companyRepository;

    private @Autowired AlertService alertService;

    private @Autowired ConsultancyJobService consultancyJobService;

    private @Autowired ImageService imageService;


    public ConsActivityViewResponse getUserJobActivityForConsultancy(long consultancyId, Integer pageNo,
                                                                     Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        try {
            // Fetch the job statistics
            Page<JobStatusResultSet> jobStatsPage = jobPostRepository.getUserStatistics(consultancyId, pageable);
            List<JobStatusResultSet> jobStats = jobStatsPage.getContent();
            List<Long> jobPostIdList = new ArrayList<>();

            Map<Long, JobStatisticsDTO> statsMap = new HashMap<>();
            // 1--->Applied
            // 2--->Interviewed
            // 3--->Hired
            // 4--->Rejected
            JobStatisticsDTO jobStatsDTO = null;
            for (JobStatusResultSet jobStatus : jobStats) {
                jobPostIdList.add(jobStatus.getJobPostId());
                jobStatsDTO = statsMap.get(jobStatus.getJobPostId()) == null ? new JobStatisticsDTO()
                        : statsMap.get(jobStatus.getJobPostId());
                jobStatsDTO.setJobPostId(jobStatus.getJobPostId());
                if (jobStatus.getInterviewStatus() == 1) {
                    jobStatsDTO.setShortlisted(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 2) {
                    jobStatsDTO.setInterviewed(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 3) {
                    jobStatsDTO.setHired(jobStatus.getTotalCount());
                } else if (jobStatus.getInterviewStatus() == 4) {
                    jobStatsDTO.setRejected(jobStatus.getTotalCount());
                }
                statsMap.put(jobStatsDTO.getJobPostId(), jobStatsDTO);
            }

            // Fetch jobpost data info for display
            List<JobPost> jobPostData = jobPostRepository.findAllById(jobPostIdList);
            List<ConsultancyActivityDTO> consActivityDTOList = new ArrayList<>();
            ConsultancyActivityDTO consActivityDTO = null;

            for (JobPost jobObj : jobPostData) {
                consActivityDTO = new ConsultancyActivityDTO();
                consActivityDTO.setDescription(jobObj.getDescription());
                consActivityDTO.setJobPostId(jobObj.getJobPostId());
                consActivityDTO.setJobType(jobObj.getJobType());
                consActivityDTO.setAppliedBy(null);
                consActivityDTO.setPostedOn(null);
                consActivityDTO.setTitle(jobObj.getTitle());
                JobStatisticsDTO jobStat = statsMap.get(jobObj.getJobPostId());
                if (jobStat != null) {
                    consActivityDTO.setApplied(jobStat.getShortlisted());
                    consActivityDTO.setInterviewed(jobStat.getInterviewed());
                    consActivityDTO.setHired(jobStat.getHired());
                    consActivityDTO.setRejected(jobStat.getRejected());
                }
                consActivityDTOList.add(consActivityDTO);
            }

            ConsActivityViewResponse res = new ConsActivityViewResponse();
            res.setTotalCount(jobStatsPage.getTotalElements());
            res.setTotalPages(jobStatsPage.getTotalPages());
            res.setActivityViewDTO(consActivityDTOList);
            return res;
        } catch (Exception e) {
            log.error("Exception : ", e);
        }

        return null;
    }

    public ConsActivityAppliedResponse getAppliedProfilesForJob(long consultancyId, long userId, long jobId, int status,
                                                                int filter, String role, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, filter);

        int applicantStaus = 0, applicantJobStatus = 0;
        if (status == 1) {
            applicantStaus = 1;
            applicantJobStatus = 1;
        }
        if (status == 0) {
            applicantStaus = 0;
            applicantJobStatus = 1;
        }
        Page<JobMatchConsultancy> jobMatchRs = null;
        if (status == 2) {
            jobMatchRs = jobMatchConsultancyRepository.getAppliedJobsforConsultantUser(jobId, consultancyId, userIds, pageable);
        } else {
            jobMatchRs = jobMatchConsultancyRepository.getAppliedJobsforConsultantUser(jobId, consultancyId,
                    applicantJobStatus, applicantStaus, userIds, pageable);
        }

        List<Long> applicantIds = new ArrayList<>();
        List<Long> appliedByList = new ArrayList<>();
        for (JobMatchConsultancy jobMatch : jobMatchRs) {
            applicantIds.add(jobMatch.getApplicantId());
            appliedByList.add(jobMatch.getUpdatedByConsUserId());
        }

        // Fetch applicant data, applied by data
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);

        List<User> appliedByObj = userRepository.findAllById(appliedByList);

        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));
        Map<Long, User> appliedByMap = appliedByObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ConsActivityAppliedResponse response = new ConsActivityAppliedResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());

        List<ConsActivityAppliedDTO> activityAppliedDTOs = new ArrayList<>();
        ConsActivityAppliedDTO activityAppliedDTO = null;
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            activityAppliedDTO = new ConsActivityAppliedDTO();
            activityAppliedDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityAppliedDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                    + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityAppliedDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityAppliedDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
            } catch (IOException e) {
                activityAppliedDTO.setProfilePicUrl(null);
            }
            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());

            activityAppliedDTO.setJobFunctionName(applicantDetails.getJobFunction());
            activityAppliedDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

            User consUser = appliedByMap.get(jobMatchStateResultSet.getUpdatedByConsUserId());
            if (consUser != null)
                activityAppliedDTO.setAppliedBy(consUser.getFirstName() + " " + consUser.getLastName());
            activityAppliedDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
            activityAppliedDTOs.add(activityAppliedDTO);
        }
        activityAppliedDTOs.sort(Comparator.comparing(ConsActivityAppliedDTO::getUpdatedDate).reversed());
        response.setActivityAppliedDTO(activityAppliedDTOs);
        response.setConsDashboardStatsDTO(dashboardService.dashboardConsStats(jobId, consultancyId, userIds));

        return response;
    }

    public ConsActivityInterviewResponse getInterviewProfilesForJob(long consultancyId, long userId, long jobId, String role, Integer status, int filter,
                                                                    Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, filter);
        List<Integer> interviewStatuses = new ArrayList<>();
        if (status == 0) {
            interviewStatuses.addAll(Arrays.asList(InterviewStatus.INTERVIEW_SCHEDULED.getValue(), InterviewStatus.REQUESTED_INTERVIEW.getValue(),
                    InterviewStatus.RESCHEDULED_INTERVIEW.getValue(), InterviewStatus.RESCHEDULE_REQUESTED.getValue(), InterviewStatus.NO_SHOW.getValue(), InterviewStatus.ON_HOLD.getValue(), InterviewStatus.INTERVIEW_ACCEPTED.getValue(), InterviewStatus.INTERVIEW_REJECTED.getValue()));
        } else {
            if (status == 2) {
                interviewStatuses.addAll(Arrays.asList(InterviewStatus.INTERVIEW_SCHEDULED.getValue(), InterviewStatus.INTERVIEW_ACCEPTED.getValue(), InterviewStatus.NO_SHOW.getValue(), InterviewStatus.ON_HOLD.getValue(), InterviewStatus.INTERVIEW_REJECTED.getValue()));
            } else {
                interviewStatuses.add(status);
            }
        }
        Page<JobMatchConsultancy> jobMatchRs = null;
        if (userIds != null && userIds.size() > 0) {
            jobMatchRs = jobMatchConsultancyRepository.getProfilesByInterviewStatus(jobId, consultancyId,
                    interviewStatuses, userIds, pageable);
        } else {
            return new ConsActivityInterviewResponse();
        }

        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<ApplicantDetails> applicantDetailsList = applicantDetailsRepository.findByUsersDetails(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);

        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ConsActivityInterviewResponse response = new ConsActivityInterviewResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ConsActivityInterviewDTO> activityInterviewDTOs = new ArrayList<>();
        ConsActivityInterviewDTO activityInterviewDTO = null;
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            activityInterviewDTO = new ConsActivityInterviewDTO();
            activityInterviewDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());
            activityInterviewDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityInterviewDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityInterviewDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityInterviewDTO.setProfilePicUrl(null);
            }

//            activityInterviewDTO.setInterviewScheduledBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
//                    + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());

            Long recruiterId = jobMatchConsultancyRepository.getLastActionPerfomedConsultantUserId(jobMatchStateResultSet.getJobMatchConId());
            if (recruiterId != null) {
                User user = userRepository.getById(recruiterId);
                activityInterviewDTO.setInterviewScheduledBy(user.getFirstName() + " " + user.getLastName());
            } else {
                activityInterviewDTO.setInterviewScheduledBy("NA");
            }

            if (jobMatchStateResultSet.getInterviewRequestedUserId() != null) {
                User user = userRepository.getById(jobMatchStateResultSet.getInterviewRequestedUserId());
                activityInterviewDTO.setInterviewRequestedBy(user.getFirstName() + " " + user.getLastName());
            } else if (jobMatchStateResultSet.getInterviewRescheduledRequestedUserId() != null) {
                User user = userRepository.getById(jobMatchStateResultSet.getInterviewRescheduledRequestedUserId());
                activityInterviewDTO.setInterviewRequestedBy(user.getFirstName() + " " + user.getLastName());
            } else {
                activityInterviewDTO.setInterviewRequestedBy(null);
            }

            activityInterviewDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
            activityInterviewDTO.setInterviewAcceptStatus(jobMatchStateResultSet.getHiredStatus());
            // Get interview details object
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setInterviewDetails(interviewObj);
            if (interviewObj != null) {
                InterviewFeedback interviewFeedback = interviewFeedbackRepository
                        .findByInterviewId(interviewObj.getInterviewId());
                if (interviewFeedback != null) {
                    activityInterviewDTO.setInterviewFeedbackStatus(interviewFeedback.getStatus());
                    activityInterviewDTO.setConComments(interviewFeedback.getConsComments());
                    activityInterviewDTO.setConOptions(interviewFeedback.getConsOption());
                }
            }
            activityInterviewDTO.setInterviewStatus(jobMatchStateResultSet.getInterviewStatus());
            activityInterviewDTOs.add(activityInterviewDTO);
        }
        activityInterviewDTOs.sort(Comparator.comparing(ConsActivityInterviewDTO::getUpdatedDate).reversed());

        response.setActivityInterviewDTO(activityInterviewDTOs);
        response.setConsDashboardStatsDTO(dashboardService.dashboardConsStats(jobId, consultancyId, userIds));


        return response;
    }

    public ConsActivityInterviewResponse getInterviewProfilesForDashboard(long userId, long consultancyId, String role,
                                                                          Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, 2);
        List<Integer> interviewStatuses = new ArrayList<>();
        interviewStatuses.addAll(Arrays.asList(InterviewStatus.INTERVIEW_SCHEDULED.getValue(), InterviewStatus.RESCHEDULED_INTERVIEW.getValue(), InterviewStatus.INTERVIEW_ACCEPTED.getValue()));
        Page<JobMatchConsultancy> jobMatchRs = jobMatchConsultancyRepository.getProfilesByInterviewStatus(interviewStatuses, userIds, pageable);

        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<ApplicantDetails> applicantDetailsList = applicantDetailsRepository.findByUsersDetails(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);


        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ConsActivityInterviewResponse response = new ConsActivityInterviewResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ConsActivityInterviewDTO> activityInterviewDTOs = new ArrayList<>();
        ConsActivityInterviewDTO activityInterviewDTO = null;
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            activityInterviewDTO = new ConsActivityInterviewDTO();
            activityInterviewDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityInterviewDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityInterviewDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityInterviewDTO.setProfilePicUrl(null);
            }
            activityInterviewDTO
                    .setInterviewScheduledBy(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName()
                            + " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName());
            activityInterviewDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
            activityInterviewDTO.setInterviewAcceptStatus(jobMatchStateResultSet.getHiredStatus());
            Optional<Company> company = companyRepository.findById(recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getCompanyId());
            activityInterviewDTO.setCompanyName(company.isPresent() ? company.get().getName() : null);
            // Get interview details object
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            activityInterviewDTO.setInterviewDetails(interviewObj);
            if (interviewObj != null) {
                InterviewFeedback interviewFeedback = interviewFeedbackRepository
                        .findByInterviewId(interviewObj.getInterviewId());
                if (interviewFeedback != null) {
                    activityInterviewDTO.setInterviewStatus(interviewFeedback.getStatus());
                    activityInterviewDTO.setConComments(interviewFeedback.getConsComments());
                    activityInterviewDTO.setConOptions(interviewFeedback.getConsOption());
                }
            }
            activityInterviewDTO.setInterviewStatus(jobMatchStateResultSet.getInterviewStatus());
            activityInterviewDTOs.add(activityInterviewDTO);
        }
        response.setActivityInterviewDTO(activityInterviewDTOs);
        //response.setConsDashboardStatsDTO(dashboardService.dashboardConsStats(consultancyId,userIds));


        return response;
    }


    public long submitFeedback(long interviewId, @Valid InterviewFeedbackDTO feedbackDTO) {
        InterviewFeedback feedbackModel = interviewFeedbackRepository.findByInterviewId(interviewId);
        if (feedbackModel == null) {
            feedbackModel = new InterviewFeedback();
            feedbackModel.setCreatedDate(new Date());
        }
        feedbackModel.setInterviewId(interviewId);
        feedbackModel.setRecruiterId(feedbackDTO.getRecruiterId());
        feedbackModel.setApplicantId(feedbackDTO.getApplicantId());
        feedbackModel.setJobId(feedbackDTO.getJobId());
        feedbackModel.setUpdatedDate(new Date());
        feedbackModel.setConsComments(feedbackDTO.getConsComments());
        feedbackModel.setConsOption(feedbackDTO.getConsOption());
        interviewFeedbackRepository.save(feedbackModel);
        return feedbackModel.getFeedbackId();
    }

    public ConsActivityHiredResponse getHiredProfilesForJob(long consultancyId, long userId, long jobId, int status, int filter, String role,
                                                            Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, filter);

        List<Integer> hireStatus = new ArrayList<>();
        hireStatus.add(7);
        Page<JobMatchConsultancy> jobMatchRs = jobMatchConsultancyRepository.getHiredJobsforConsultantUser(
                jobId, consultancyId, hireStatus, userIds, pageable);

        List<Long> applicantIds = new ArrayList<>();
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ConsActivityHiredResponse response = new ConsActivityHiredResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ConsActivityHiredDTO> activityHiredDTOs = new ArrayList<>();
        ConsActivityHiredDTO activityHiredDTO = null;
        if (status == 0) {
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                        + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
                String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                try {
                    activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                } catch (IOException e) {
                    activityHiredDTO.setProfilePicUrl(null);
                }
                activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
                activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());
                // Get interview details object
                Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                        jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                activityHiredDTO.setInterviewDetails(interviewObj);
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantIds(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                if (offerDetailsObj != null) {
                    activityHiredDTO.setOfferDetails(offerDetailsObj);
                }

                if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                    User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                    activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                } else {
                    activityHiredDTO.setOfferAccepted(null);
                }

                ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                activityHiredDTOs.add(activityHiredDTO);
            }
        } else if (status == 1) {
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantIds(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                if (offerDetailsObj == null) {
                    activityHiredDTO.setOfferDetails(null);
                    activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                            + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                    activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
                    String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                    try {
                        activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                    } catch (IOException e) {
                        activityHiredDTO.setProfilePicUrl(null);
                    }
                    activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
                    activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

                    if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                        User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                        activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                    } else {
                        activityHiredDTO.setOfferAccepted(null);
                    }

                    // Get interview details object
                    Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                            jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setInterviewDetails(interviewObj);

                    ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                    activityHiredDTOs.add(activityHiredDTO);

                }

            }
        } else if (status == 2) {
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantId(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId(), OfferStatus.OFFER_SENT.getValue());
                if (offerDetailsObj != null) {
                    activityHiredDTO.setOfferDetails(offerDetailsObj);
                    activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                            + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                    activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
                    activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

                    String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                    try {
                        activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                    } catch (IOException e) {
                        activityHiredDTO.setProfilePicUrl(null);
                    }
                    activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());


                    if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                        User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                        activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                    } else {
                        activityHiredDTO.setOfferAccepted(null);
                    }

                    // Get interview details object
                    Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                            jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setInterviewDetails(interviewObj);

                    ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                    activityHiredDTOs.add(activityHiredDTO);

                }

            }
        } else if (status == 3) {
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantId(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId(), OfferStatus.OFFER_SIGNED.getValue());
                if (offerDetailsObj != null) {
                    activityHiredDTO.setOfferDetails(offerDetailsObj);
                    activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                            + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                    activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

                    activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
                    String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                    try {
                        activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                    } catch (IOException e) {
                        activityHiredDTO.setProfilePicUrl(null);
                    }
                    activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());


                    if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                        User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                        activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                    } else {
                        activityHiredDTO.setOfferAccepted(null);
                    }

                    // Get interview details object
                    Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                            jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setInterviewDetails(interviewObj);

                    ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                    activityHiredDTOs.add(activityHiredDTO);
                }
            }
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantId(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId(), OfferStatus.OFFER_ACCEPTED.getValue());
                if (offerDetailsObj != null) {
                    activityHiredDTO.setOfferDetails(offerDetailsObj);
                    activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

                    activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                            + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                    activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

                    String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                    try {
                        activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                    } catch (IOException e) {
                        activityHiredDTO.setProfilePicUrl(null);
                    }
                    activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());


                    if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                        User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                        activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                    } else {
                        activityHiredDTO.setOfferAccepted(null);
                    }

                    // Get interview details object
                    Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                            jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setInterviewDetails(interviewObj);

                    ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                    activityHiredDTOs.add(activityHiredDTO);
                }
            }
        } else if (status == 4) {
            for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
                activityHiredDTO = new ConsActivityHiredDTO();
                OfferDetails offerDetailsObj = offerDetailsRepository
                        .findByJobPostIdAndApplicantId(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId(), OfferStatus.APPLICANT_JOINED.getValue());
                if (offerDetailsObj != null) {
                    activityHiredDTO.setOfferDetails(offerDetailsObj);
                    activityHiredDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());

                    activityHiredDTO.setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName()
                            + " " + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
                    activityHiredDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());
                    String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
                    try {
                        activityHiredDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                    } catch (IOException e) {
                        activityHiredDTO.setProfilePicUrl(null);
                    }
                    activityHiredDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());

                    // Get interview details object
                    Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                            jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setInterviewDetails(interviewObj);


                    if (jobMatchStateResultSet.getOfferacceptedUserId() != null) {
                        User user = userRepository.getById(jobMatchStateResultSet.getOfferacceptedUserId());
                        activityHiredDTO.setOfferAccepted(user.getFirstName() + " " + user.getLastName());
                    } else {
                        activityHiredDTO.setOfferAccepted(null);
                    }

                    ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
                    activityHiredDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);
                    activityHiredDTOs.add(activityHiredDTO);

                }

            }
        }

        activityHiredDTOs.sort(Comparator.comparing(ConsActivityHiredDTO::getUpdatedDate).reversed());
        response.setActivityHiredDTO(activityHiredDTOs);
        response.setConsDashboardStatsDTO(dashboardService.dashboardConsStats(jobId, consultancyId, userIds));


        return response;
    }

    public ConsActivityRejectedResponse getRejectedProfileForJob(long consultancyId, long userId, long jobId,
                                                                 int status, int filter, String role, Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<JobMatchConsultancy> jobMatchRs = null;
        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, filter);
        List<Integer> interviewStatus = new ArrayList<>();
        List<Integer> offerStatus = new ArrayList<>();
        if (status == 3 || status == 4) {
            interviewStatus.add(status == 3 ? InterviewStatus.NO_SHOW_REJECTED.getValue() : InterviewStatus.NOT_FIT.getValue());
            jobMatchRs = jobMatchConsultancyRepository.getRejectedJobsforConsultantUserByInterviewStatus(jobId, consultancyId, interviewStatus, userIds, pageable);
        } else if (status == 1 || status == 2) {
            offerStatus.add(status == 1 ? OfferStatus.OFFER_REJECTED.getValue() : OfferStatus.APPLICANT_NOT_JOINED.getValue());
            jobMatchRs = jobMatchConsultancyRepository.getRejectedJobsforConsultantUserByOfferStatus(jobId, consultancyId, offerStatus, userIds, pageable);
        } else {
            interviewStatus.addAll(Arrays.asList(InterviewStatus.NOT_FIT.getValue(), InterviewStatus.NO_SHOW_REJECTED.getValue(), InterviewStatus.APPLICANT_NOT_JOINED.getValue()));
            offerStatus.addAll(Arrays.asList(OfferStatus.OFFER_REJECTED.getValue(), OfferStatus.APPLICANT_NOT_JOINED.getValue()));
            jobMatchRs = jobMatchConsultancyRepository.getRejectedJobsforConsultantUserByByInterviewStatusAndOfferStatus(jobId, consultancyId, offerStatus, interviewStatus, userIds, pageable);
        }

        List<Long> applicantIds = new ArrayList<>();
        List<Long> recruiterIds = new ArrayList<>();
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            applicantIds.add(jobMatchStateResultSet.getApplicantId());
            recruiterIds.add(jobMatchStateResultSet.getRecruiterId());
        }

        // Fetch applicant data, recruiter data
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<User> recruiterObj = userRepository.findAllById(recruiterIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);
        Map<Long, User> recruiterMap = recruiterObj.stream().collect(Collectors.toMap(User::getUserId, b -> b));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));
        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        ConsActivityRejectedResponse response = new ConsActivityRejectedResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<ConsActivityRejectedDTO> activityRejectedDTOs = new ArrayList<>();
        ConsActivityRejectedDTO activityRejectedDTO = null;
        for (JobMatchConsultancy jobMatchStateResultSet : jobMatchRs) {
            activityRejectedDTO = new ConsActivityRejectedDTO();
            activityRejectedDTO.setApplicantId(jobMatchStateResultSet.getApplicantId());
            activityRejectedDTO.setUpdatedDate(jobMatchStateResultSet.getUpdatedDate());
            activityRejectedDTO
                    .setApplicantName(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getFirstName() + " "
                            + applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLastName());
            activityRejectedDTO.setLocation(applicantMap.get(jobMatchStateResultSet.getApplicantId()).getLocation());

            String imageUrl = applicantMap.get(jobMatchStateResultSet.getApplicantId()).getProfileImageUrl();
            try {
                activityRejectedDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                activityRejectedDTO.setProfilePicUrl(null);
            }

            String name = "";
            if (recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName() != null)
                name = recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getFirstName();
            if (recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName() != null)
                name += " " + recruiterMap.get(jobMatchStateResultSet.getRecruiterId()).getLastName();
            activityRejectedDTO.setRejectedBy(name);
            activityRejectedDTO.setInterviewSatus(jobMatchStateResultSet.getInterviewStatus());
            activityRejectedDTO.setJobMatchId(jobMatchStateResultSet.getJobMatchConId());
            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchStateResultSet.getApplicantId());
            activityRejectedDTO.setJobFunctionName(applicantDetails != null ? applicantDetails.getJobFunction() : null);


            if (jobMatchStateResultSet.getOfferrejectedUserId() != null) {
                User user = userRepository.getById(jobMatchStateResultSet.getOfferrejectedUserId());
                activityRejectedDTO.setOfferRejectedBy(user.getFirstName() + " " + user.getLastName());
            } else {
                activityRejectedDTO.setOfferRejectedBy(null);
            }

            OfferDetails offerDetailsObj = offerDetailsRepository
                    .findByJobPostIdAndApplicantId(jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            if (offerDetailsObj != null) {
                activityRejectedDTO.setOfferStatus(offerDetailsObj.getOfferStatus());
            }
            Interview interviewObj = interviewRepository.findByJobPostIdAndApplicantId(
                    jobMatchStateResultSet.getJobPostId(), jobMatchStateResultSet.getApplicantId());
            if (interviewObj != null) {
                InterviewFeedback interviewFeedback = interviewFeedbackRepository
                        .findByInterviewId(interviewObj.getInterviewId());
                activityRejectedDTO.setInterviewFeedbackStatus(interviewFeedback != null ? interviewFeedback.getStatus() : null);
            }
            activityRejectedDTOs.add(activityRejectedDTO);

        }
        response.setActivityRejectedDTO(activityRejectedDTOs);
        activityRejectedDTOs.sort(Comparator.comparing(ConsActivityRejectedDTO::getUpdatedDate).reversed());

        response.setConsDashboardStatsDTO(dashboardService.dashboardConsStats(jobId, consultancyId, userIds));

        return response;
    }

    public ConsActivityJobResponse getConsActivityJobList(Long recruiterId, Long consultancyId, int filter, String role,
                                                          Integer pageNo, Integer pageSize) {

        //TODO need to update queries
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Long totalCount = null;
        List<ConsActivityJobResultSet> consActivityJobResultSets = null;
        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, recruiterId, role, filter);
        consActivityJobResultSets = jobMatchConsultancyRepository.countConsJobStatus(userIds, pageable);
        totalCount = jobMatchConsultancyRepository.totalCountConsJobStatus(userIds);
        List<ConsActivityJobDTO> consActivityJobDTOList = new ArrayList<>();

        ConsActivityJobResponse consActivityJobResponse = new ConsActivityJobResponse();
        if (consActivityJobResultSets != null) {
            Set<Long> jobIds = consActivityJobResultSets.stream().filter(c -> c.getJobPostId() != null)
                    .map(c -> c.getJobPostId()).collect(Collectors.toSet());

            List<JobPost> jobPostList = jobPostRepository.findAllById(jobIds);
            Map<Long, JobPost> jobPostMap = new HashMap<>();
            for (JobPost jobPost : jobPostList) {
                jobPostMap.put(jobPost.getJobPostId(), jobPost);
            }
            for (ConsActivityJobResultSet consActivityJobResultSet : consActivityJobResultSets) {
                JobPost jobPost = jobPostMap.get(consActivityJobResultSet.getJobPostId());
                ConsActivityJobDTO consActivityJobDTO = new ConsActivityJobDTO();

                ConsDashboardStatsDTO consDashboardStatsDTO = dashboardService.dashboardConsStats(jobPost.getJobPostId(), consultancyId, userIds);
                // consActivityJobDTO.setUpdatedDate(consActivityJobResultSet.getUpdatedDate());
                consActivityJobDTO.setAppliedCount(consDashboardStatsDTO.getAppliedCount());
                consActivityJobDTO.setHiredCount(consDashboardStatsDTO.getHiredCount());
                consActivityJobDTO.setInterviewedCount(consDashboardStatsDTO.getInterviewCount());
                consActivityJobDTO.setRejectedCount(consDashboardStatsDTO.getRejectedCount());
                consActivityJobDTO.setUpdatedDate(consActivityJobResultSet.getUpdatedDate());
                consActivityJobDTO.setUploadedCount(consDashboardStatsDTO.getProfilesUploaded());
                consActivityJobDTO
                        .setTotalCount(consDashboardStatsDTO.getAppliedCount() + consDashboardStatsDTO.getHiredCount()
                                + consDashboardStatsDTO.getInterviewCount() + consDashboardStatsDTO.getRejectedCount());
                if (jobPost != null) {
                    consActivityJobDTO.setJobId(jobPost.getJobPostId());
                    consActivityJobDTO.setJobFunction(jobPost.getJobFunction().stream().map(j -> j.getJobFunctionName())
                            .collect(Collectors.joining(",")));
                    consActivityJobDTO.setJobTitle(jobPost.getTitle());

                    Optional<User> user = userRepository.findById(jobPost.getUserId());
                    if (user.isPresent()) {
                        Optional<Company> company = companyRepository.findById(user.get().getCompanyId());
                        consActivityJobDTO.setCompanyName(company.isPresent() ? company.get().getName() : null);
                        try {
                            consActivityJobDTO.setProfilePic(company.get().getProfileImageUrl() != null ? imageService.getImage(company.get().getProfileImageUrl()) : CommonConstants.company_default_image);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                //11, 13, 14
//                if (consActivityJobResultSet.getUpdatedByConsUserId() != null) {
//                    Long userId = jobMatchConsultancyRepository.findRecentUpdatedUserId(consActivityJobResultSet.getJobPostId());
//                    if (userId != null) {
//                        Optional<User> user = userRepository.findById(userId);
//                        if (user.isPresent()) {
//                            String name = "";
//                            if (user.get().getFirstName() != null) {
//                                name = user.get().getFirstName();
//                            }
//                            if (user.get().getLastName() != null) {
//                                name += " " + user.get().getLastName();
//                            }
//                            consActivityJobDTO.setAppliedBy(name);
//                            consActivityJobDTO.setUserId(user.get().getUserId());
//                            consActivityJobDTO.setName(user.get().getFirstName() + " " + user.get().getLastName());
//                        }
//                    }
//                }

                if (consActivityJobResultSet.getUpdatedByConsUserId() != null) {
                    Optional<User> user = userRepository.findById(consActivityJobResultSet.getUpdatedByConsUserId());
                    if (user.isPresent()) {
                        String name = "";
                        if (user.get().getFirstName() != null) {
                            name = user.get().getFirstName();
                        }
                        if (user.get().getLastName() != null) {
                            name += " " + user.get().getLastName();
                        }
                        consActivityJobDTO.setAppliedBy(name);
                        consActivityJobDTO.setUserId(user.get().getUserId());
                        consActivityJobDTO.setName(user.get().getFirstName() + " " + user.get().getLastName());
                    }
                }
                consActivityJobDTOList.add(consActivityJobDTO);
            }
            //Map<String, List<ConsActivityJobDTO>> usersGroup = consActivityJobDTOList.stream().collect(Collectors.groupingBy(ConsActivityJobDTO -> ConsActivityJobDTO.getName()));
            consActivityJobDTOList.sort(Comparator.comparing(ConsActivityJobDTO::getUpdatedDate).reversed());
            consActivityJobResponse.setConsActivityJobDTOS(consActivityJobDTOList);
            consActivityJobResponse.setTotalCount(totalCount);
            consActivityJobResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        }
        return consActivityJobResponse;
    }

    public ConsActivityDashboardJobResponse getConsDashboardActivityJobList(Long recruiterId, Long consultancyId, long filter, String role,
                                                                            Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Long totalCount = null;
        List<ConsActivityJobResultSet> consActivityJobResultSets = null;

        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, recruiterId, role, 2);
//        List<Long> userIds = new ArrayList<Long>();
//        if (filter == 2 || filter == 1) {
//            userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, recruiterId, null);
//        }
//        if (filter == 0 || filter == 2) {
//            userIds.add(recruiterId);
//        }
        consActivityJobResultSets = jobMatchConsultancyRepository.countConsJobStatus(userIds, pageable);
        totalCount = jobMatchConsultancyRepository.totalCountConsJobStatus(userIds);
        List<ConsActivityJobDTO> consActivityJobDTOList = new ArrayList<>();

        ConsActivityDashboardJobResponse consActivityJobResponse = new ConsActivityDashboardJobResponse();
        if (consActivityJobResultSets != null) {
            Set<Long> jobIds = consActivityJobResultSets.stream().filter(c -> c.getJobPostId() != null)
                    .map(c -> c.getJobPostId()).collect(Collectors.toSet());

            List<JobPost> jobPostList = jobPostRepository.findAllById(jobIds);
            Map<Long, JobPost> jobPostMap = new HashMap<>();
            for (JobPost jobPost : jobPostList) {
                jobPostMap.put(jobPost.getJobPostId(), jobPost);
            }
            for (ConsActivityJobResultSet consActivityJobResultSet : consActivityJobResultSets) {
                JobPost jobPost = jobPostMap.get(consActivityJobResultSet.getJobPostId());
                ConsActivityJobDTO consActivityJobDTO = new ConsActivityJobDTO();

                ConsDashboardStatsDTO consDashboardStatsDTO = dashboardService.dashboardConsStats(jobPost.getJobPostId(), consultancyId, userIds);
                consActivityJobDTO.setAppliedCount(consDashboardStatsDTO.getAppliedCount());
                consActivityJobDTO.setHiredCount(consDashboardStatsDTO.getHiredCount());
                consActivityJobDTO.setInterviewedCount(consDashboardStatsDTO.getInterviewCount());
                consActivityJobDTO.setRejectedCount(consDashboardStatsDTO.getRejectedCount());
                consActivityJobDTO.setUpdatedDate(consActivityJobResultSet.getUpdatedDate());

                if (jobPost != null) {
                    consActivityJobDTO.setJobId(jobPost.getJobPostId());
                    consActivityJobDTO.setJobFunction(jobPost.getJobFunction().stream().map(j -> j.getJobFunctionName())
                            .collect(Collectors.joining(",")));
                    consActivityJobDTO.setJobTitle(jobPost.getTitle());

                    Optional<User> user = userRepository.findById(jobPost.getUserId());
                    if (user.isPresent()) {
                        Optional<Company> company = companyRepository.findById(user.get().getCompanyId());
                        consActivityJobDTO.setCompanyName(company.isPresent() ? company.get().getName() : null);
                    }
                }
                if (consActivityJobResultSet.getUpdatedByConsUserId() != null) {
                    Optional<User> user = userRepository.findById(consActivityJobResultSet.getUpdatedByConsUserId());
                    if (user.isPresent()) {
                        String name = "";
                        if (user.get().getFirstName() != null) {
                            name = user.get().getFirstName();
                        }
                        if (user.get().getLastName() != null) {
                            name += " " + user.get().getLastName();
                        }
                        consActivityJobDTO.setAppliedBy(name);
                        consActivityJobDTO.setUserId(user.get().getUserId());
                        consActivityJobDTO.setName(user.get().getFirstName() + " " + user.get().getLastName());
                        List<Long> userList = new ArrayList<>();
                        userList.add(user.get().getUserId());
                        consActivityJobDTO.setTotalCount(applicantRepository.countByConsultancyId(userList));
                    }
                }
                consActivityJobDTOList.add(consActivityJobDTO);
            }
            consActivityJobDTOList.sort(Comparator.comparing(ConsActivityJobDTO::getUpdatedDate).reversed());
            Map<String, List<ConsActivityJobDTO>> usersGroup = consActivityJobDTOList.stream().collect(Collectors.groupingBy(ConsActivityJobDTO -> ConsActivityJobDTO.getName()));

            consActivityJobResponse.setConsActivityJobDTOS(usersGroup);
            consActivityJobResponse.setTotalCount(totalCount);
            consActivityJobResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));


        }
        return consActivityJobResponse;
    }

//    private List<Long> getUserIdByFilter(int filter, long consultancyId, long userId) {
//        List<Long> userIds = new ArrayList<Long>();
//        if (filter == 2 || filter == 1) {
//            User user = userRepository.getById(userId);
//            if (user.getDepartmentId() == null) {
//                userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId, null);
//            } else {
//                userIds = userRepository.findSameDepartmentUsersIdsByConsultancyIdAndEnabled(consultancyId, true, user.getDepartmentId(), null);
//            }
//        }
//        if (filter == 0 || filter == 2) {
//            userIds.add(userId);
//        }
//        return userIds;
//    }

    public void updateApplicantInterviewStatus(Long consultancyId, Long userId, Long jobPostId, Long applicantId, int status) throws WorkruitException {
        //TODO need to update admin and manage roles

        //Admin
        JobMatchConsultancy jobMatchConsultancy = jobMatchConsultancyRepository.findByApplicantIdAndConsultancyIdAndConsultancyUserIdAndJobPostId(applicantId, jobPostId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant job match is not found with job id: %s", jobPostId)));
        //Manager
        //JobMatchConsultancy jobMatchConsultancy = jobMatchConsultancyRepository.findByApplicantIdAndConsultancyIdAndConsultancyUserIdAndJobPostId(applicantId, consultancyId, userId, jobPostId)
        //.orElseThrow(() -> new WorkruitException(String.format("Applicant job match is not found with job id: %s", jobPostId)));


        User user = userRepository.findById(userId).get();
        User recruiter = userRepository.findById(jobMatchConsultancy.getRecruiterId()).get();
        Consultancy mainConsultancy = consultancyRepository.findById(consultancyId).get();
        Applicant applicant = applicantRepository.findById(applicantId).get();
        JobPost jobPost = jobPostRepository.findById(jobPostId).get();
        StringBuilder consultancyMessage = new StringBuilder();
        StringBuilder recruiterMessage = new StringBuilder();
        String consultancyUsername = user.getFirstName() + " " + user.getLastName();
        String applicantName = applicant.getFirstName() + " " + applicant.getLastName();

        if (InterviewStatus.REQUESTED_INTERVIEW.getValue() == status) {
            consultancyMessage.append("Interview has been requested for ");
            recruiterMessage.append("Interview has been requested by ");
            jobMatchConsultancy.setInterviewRequestedUserId(userId);

            jobMatchConsultancy.setInterviewRequestedDate(new Date());
            jobMatchConsultancy.setInterviewStatus(status);
        } else if (InterviewStatus.RESCHEDULE_REQUESTED.getValue() == status) {
            consultancyMessage.append("Reschedule interview has been requested for ");
            recruiterMessage.append("Reschedule interview has been requested by ");
            jobMatchConsultancy.setRescheduledRequestDate(new Date());
            jobMatchConsultancy.setInterviewRescheduledRequestedUserId(userId);
            jobMatchConsultancy.setInterviewStatus(status);
        } else if (InterviewStatus.INTERVIEW_ACCEPTED.getValue() == status) {
            consultancyMessage.append("Interview has been accepted for ");
            recruiterMessage.append("Interview has been accepted by ");
            jobMatchConsultancy.setAccepetedDate(new Date());
            jobMatchConsultancy.setAcceptedUserId(userId);
            jobMatchConsultancy.setHiredStatus(status);
            jobMatchConsultancy.setInterviewStatus(status);
        } else if (InterviewStatus.INTERVIEW_REJECTED.getValue() == status) {
            consultancyMessage.append("Interview has been rejected for ");
            recruiterMessage.append("Interview has been rejected by ");
            jobMatchConsultancy.setHiredStatus(status);
            jobMatchConsultancy.setInterviewRejectedDate(new Date());
            jobMatchConsultancy.setInterviewRejectedUserId(userId);
        }
        jobMatchConsultancy.setLastactionPerformedConsultantUserId(userId);
        jobMatchConsultancy.setUpdatedDate(new Date());
        jobMatchConsultancyRepository.save(jobMatchConsultancy);


        if (consultancyMessage.length() > 0 && recruiterMessage.length() > 0) {
            consultancyMessage.append(applicantName).append(" for ")
                    .append(jobPost.getTitle()).append(" job by ")
                    .append(consultancyUsername).append(".");
            alertService.saveAlertInfo(userId, consultancyMessage.toString(), consultancyId);
        }
        if (consultancyMessage.length() > 0 && recruiterMessage.length() > 0) {
            recruiterMessage.append(consultancyUsername).append(" from ")
                    .append(mainConsultancy.getName()).append(" for ")
                    .append(applicantName).append(" for ")
                    .append(jobPost.getTitle()).append(" job.");
            alertService.saveAlertInfo(jobMatchConsultancy.getRecruiterId(), recruiterMessage.toString(), recruiter.getConsultancyId());
        }
    }
}
