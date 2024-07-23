/**
 *
 */
package com.workruit.us.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.JobApplState;
import com.workruit.us.application.exception.JobNotFoundException;
import com.workruit.us.application.models.*;
import com.workruit.us.application.notification.NotificationException;
import com.workruit.us.application.notification.NotificationScheduler;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mahesh
 */
@Slf4j
@Service
public class TalentViewService {

    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired JobMatchingRepository jobMatchingRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired ApplicantService applicantService;
    private @Autowired WorkExperienceRepository workExperienceRepository;
    private @Autowired ApplicantDetailsService applicantDetailsService;
    private @Autowired UserRepository userRepository;
    private @Autowired AlertService alertService;
    private @Autowired ModelMapper modelMapper;

    private @Autowired CompanyRepository companyRepository;
    private @Autowired NotificationScheduler notificationScheduler;
    private @Autowired JobQuestionAnswersRepository jobQuestionAnswersRepository;
    private @Autowired JobQuestionValuesRepository jobQuestionValuesRepository;
    private @Autowired ImageService imageService;
    @Autowired
    private ConsultancyJobStatusRepository consultancyJobStatusRepository;
    private @Autowired JobPostService jobPostService;
    private @Autowired CompanyService companyService;

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Method to display users that matched for a job from jobMatch table includes
     * both users and consultancy
     *
     * @param jobPostId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public TalentViewResponse getMatchedUsersForJob(long jobPostId, Integer pageNo, Integer pageSize) {

        Pageable intrestedProfilespageable = PageRequest.of(pageNo, pageSize);
        // 1. get users from job matches which no action taken
        TalentViewResponse response = new TalentViewResponse();
        List<TalentViewDTO> talentViewDTO = new ArrayList<>();
        List<Long> consultantIds = new ArrayList<>();

        // List<JobMatchConsultancy> jobMatchList = new ArrayList<>();
        try {
//            Page<JobMatchConsultancy> intrestedJobMatchList = jobMatchConsultancyRepository.findIntrestedJobMatchesByJobPostId(jobPostId, intrestedProfilespageable);
//            jobMatchList.addAll(intrestedJobMatchList.getContent());
//            for (JobMatchConsultancy jobMatchConsultancy : jobMatchList) {
//                consultantIds.add(jobMatchConsultancy.getConsultancyId());
//            }
            Page<JobMatchConsultancy> jobMatchList = null;
//            if (consultantIds.size() > 0) {
//                notIntrestedJobMatchList = jobMatchConsultancyRepository.findJobMatchesByJobPostId(jobPostId, consultantIds, intrestedProfilespageable);
//            } else {
//                notIntrestedJobMatchList = jobMatchConsultancyRepository.findJobMatchesByJobPostId(jobPostId, intrestedProfilespageable);
//
//            }
            jobMatchList = jobMatchConsultancyRepository.findJobMatchesByJobPostId(jobPostId, intrestedProfilespageable);
            talentViewDTO = genrateTalentViewResponseforDashboard(jobMatchList.getContent(), jobPostId);
            List<TalentViewDTO> intrestedUsersList = new ArrayList<>();
            List<TalentViewDTO> notIntrestedUsersList = new ArrayList<>();

            for (int i = 0; i < talentViewDTO.size(); i++) {
                TalentViewDTO talentViewDTO1 = talentViewDTO.get(i);
                if (talentViewDTO1.getApplicantStatus() == 1) {
                    intrestedUsersList.add(talentViewDTO1);
                } else {
                    notIntrestedUsersList.add(talentViewDTO1);
                }
            }
            talentViewDTO.clear();
            intrestedUsersList.sort(Comparator.comparing(TalentViewDTO::getMatchScore).reversed());
            notIntrestedUsersList.sort(Comparator.comparing(TalentViewDTO::getMatchScore).reversed());
            talentViewDTO.addAll(intrestedUsersList);
            talentViewDTO.addAll(notIntrestedUsersList);

            response.setTalentViewDTO(talentViewDTO);
            response.setTotalCount(jobMatchList.getTotalElements());
            response.setTotalPages(jobMatchList.getTotalPages());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }

    public TalentViewConsDetailsDTO getMatchedProfilesForConsultancy(long jobPostId, long consultancyId, int status,
                                                                     Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("applicantId").descending());
        TalentViewConsDetailsDTO talentViewConsDetailsDTO = null;
        try {
            Optional<Consultancy> c = consultancyRepository.findById(consultancyId);
            if (!c.isPresent()) {
                throw new Exception("Consultancy not found");
            }
            Consultancy con = c.get();
            Page<JobMatchConsultancy> jobMatchConsList = null;

            if (status == 0) {
//				jobMatchConsList = jobMatchConsultancyRepository.findByJobPostIdAndConsultancyId(jobPostId,
//						consultancyId, pageable);
                jobMatchConsList = jobMatchConsultancyRepository.getProfilesByJobStatus(jobPostId, consultancyId, pageable);
            } else if (status == 1) {
                jobMatchConsList = jobMatchConsultancyRepository.findByJobPostIdAndRecruiterIdAndIsIntrestedByUser(
                        jobPostId, consultancyId, 1, pageable);
            } else {
                jobMatchConsList = jobMatchConsultancyRepository.findByJobPostIdAndRecruiterIdAndIsIntrestedByUser(
                        jobPostId, consultancyId, 0, pageable);
            }

            List<Long> applicantList = jobMatchConsList.stream().map(JobMatchConsultancy::getApplicantId)
                    .collect(Collectors.toList());

            talentViewConsDetailsDTO = genrateTalentViewConsDetailsDTO(applicantList, con, jobPostId);
            talentViewConsDetailsDTO.setTotalCount(jobMatchConsList.getTotalElements());
            talentViewConsDetailsDTO.setTotalPages(jobMatchConsList.getTotalPages());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return talentViewConsDetailsDTO;
    }

    public ApplicantDTO getMatchedApplicantFromConsultancy(long applicantId) throws Exception {
        ApplicantDTO talentView = getProfileInfoForJob(applicantId);
        // Add id any consultancy specific info required here
        return talentView;
    }

    public ApplicantDTO getProfileInfoForJob(long applicantId) throws Exception {
        ApplicantDTO applicantDTO = new ApplicantDTO();
        try {
            Optional<Applicant> applicant = applicantRepository.findById(applicantId);
            if (!applicant.isPresent()) {
                throw new Exception("Applicant not found");
            }
            applicantDTO = applicantService.profile(applicantId);

            applicantDTO.setJobMatchStatus("NA");

//			JobMatch jobMatch = jobMatchingRepository.findByJobPostIdAndApplicantIdAndIsConsultancy(jobPostId,
//					applicantId,true);
//
//			if (jobMatch.getApplicantJobStatus() == 1 && jobMatch.getApplicantStatus() == 1) {
//				applicantDTO.setJobMatchStatus("Matched");
//			} else if (jobMatch.getApplicantJobStatus() == 0 && jobMatch.getApplicantStatus() == 1) {
//				applicantDTO.setJobMatchStatus("Shortlisted");
//			}

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return applicantDTO;
    }

    public ApplicantDTO getProfileInfoForJob(long applicantId, long jobPostId) throws Exception {
        ApplicantDTO applicantDTO = new ApplicantDTO();
        try {
            Optional<Applicant> applicant = applicantRepository.findById(applicantId);
            if (!applicant.isPresent()) {
                throw new Exception("Applicant not found");
            }
            applicantDTO = applicantService.profile(applicantId);

            JobMatchConsultancy jobMatch = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId,
                    applicantId);
            if (jobMatch != null) {
                if (jobMatch.getApplicantJobStatus() == 1 && jobMatch.getApplicantStatus() == 1) {
                    applicantDTO.setJobMatchStatus("Matched");
                } else if (jobMatch.getApplicantJobStatus() == 0 && jobMatch.getApplicantStatus() == 1) {
                    applicantDTO.setJobMatchStatus("Shortlisted");
                } else if (jobMatch.getInterviewStatus() == 2) {
                    applicantDTO.setJobMatchStatus("Interview Scheduled");
                } else if (jobMatch.getInterviewStatus() == 3) {
                    applicantDTO.setJobMatchStatus("Interview Re-Scheduled");
                } else if (jobMatch.getInterviewStatus() == 4) {
                    applicantDTO.setJobMatchStatus("Interview Re-Schedule Requested");
                } else if (jobMatch.getInterviewStatus() == 5) {
                    applicantDTO.setJobMatchStatus("No Show");
                } else if (jobMatch.getInterviewStatus() == 6) {
                    applicantDTO.setJobMatchStatus("On Hold");
                } else if (jobMatch.getInterviewStatus() == 8) {
                    applicantDTO.setJobMatchStatus("Rejected");
                } else if (jobMatch.getInterviewStatus() == 9) {
                    applicantDTO.setJobMatchStatus("Interview Requested");
                } else if (jobMatch.getInterviewStatus() == 10) {
                    applicantDTO.setJobMatchStatus("Not Fit");
                } else if (jobMatch.getInterviewStatus() == 11) {
                    applicantDTO.setJobMatchStatus("No Show Rejected");
                }
            }

            List<JobQuestionAnswerResultSet> results = jobQuestionAnswersRepository.findByJobPostIdApplicantId(jobPostId, applicantId);
            Map<Long, List<String>> queResults = results.stream()
                    .collect(Collectors.groupingBy(
                            row -> row.getQuestionId(), // Group by jq.question_title
                            Collectors.mapping(row -> row.getQuestionAnsValue(), Collectors.toList()) // Collect jqa.question_ans_value as a list
                    ));
            Set<Long> jobQueIds = new HashSet<>();
            List<JobQuestionAnswerDTO> jobQuestionAnswerDTOS = new ArrayList<>();
            for (JobQuestionAnswerResultSet queAns : results) {
                if (!jobQueIds.contains(queAns.getQuestionId())) {
                    JobQuestionAnswerDTO jobQuestionAnswerDTO = new JobQuestionAnswerDTO();
                    jobQuestionAnswerDTO.setQuestionType(queAns.getQuestionType());
                    jobQuestionAnswerDTO.setQuestionTitle(queAns.getQuestionTitle());
                    jobQuestionAnswerDTO.setQuestionId(queAns.getQuestionId());
                    Set<String> questionAns = new HashSet<>();
                    jobQueIds.add(queAns.getQuestionId());
                    List<String> queAnsValues = queResults.get(queAns.getQuestionId());
                    //if(queAns.getQuestionType().equals(QuestionType.RADIO.toString()) || queAns.getQuestionType().equals(QuestionType.MULTI_SELECT.toString())){
//						for (String ans:queAnsValues) {
//							JobQuestionValues jobQuestionValues= jobQuestionValuesRepository.findById(Long.parseLong(ans)).get();
//							questionAns.add(jobQuestionValues.getQuestionValue());
//						}

                    //} else{
                    questionAns.addAll(queAnsValues);
                    //}
                    jobQuestionAnswerDTO.setQuestionAns(questionAns);
                    jobQuestionAnswerDTOS.add(jobQuestionAnswerDTO);
                }
            }
            applicantDTO.setJobQuestionAnswers(jobQuestionAnswerDTOS);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return applicantDTO;

    }

    /**
     * Method to return user profiles based on the filter criteria set
     *
     * @param jobPostId
     * @param jobPostDTO
     * @param pageNo
     * @param pageSize
     * @return
     * @throws JobNotFoundException
     */
    public TalentViewResponse getMatchedUsersForJobByFilter(long jobPostId, JobPostDTO jobPostDTO, Integer pageNo,
                                                            Integer pageSize) throws JobNotFoundException {

        Optional<JobPost> jobPostOpt = jobPostRepository.findById(jobPostId);
        if (!jobPostOpt.isPresent()) {
            log.error("Job not found for id:" + jobPostId);
            throw new JobNotFoundException("Job not found");
        }
        JobPost jobPost = jobPostOpt.get();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("applicantId").descending());
        // update filter criteria if its passed from UI
        List<Integer> skillList = null;
        if (!jobPostDTO.getJobSkills().isEmpty()) {
            skillList = jobPostDTO.getJobSkills();
        } else {
            skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId).collect(Collectors.toList());
        }
        long expMin = jobPostDTO.getExperienceMin() == null ? jobPost.getExperienceMin()
                : jobPostDTO.getExperienceMin();
        long expMax = jobPostDTO.getExperienceMax() == null ? jobPost.getExperienceMax()
                : jobPostDTO.getExperienceMax();
        String location = jobPostDTO.getLocation() == null ? jobPost.getLocation() : jobPostDTO.getLocation();
        List<Integer> jobFuncList = null;
        if (!jobPostDTO.getJobFunction().isEmpty()) {
            jobFuncList = jobPostDTO.getJobFunction();
        } else {
            jobFuncList = jobPost.getJobFunction().stream().map(JobFunction::getJobFunctionId)
                    .collect(Collectors.toList());
        }
        Integer jobType = jobPostDTO.getJobType() == null ? jobPost.getJobType() : jobPostDTO.getJobType();

        // get matching users for job
        Page<UserInfoResultSet> filterList = jobPostRepository.findUserProfilesBySearchFilter(skillList, jobFuncList,
                expMin, expMax, location, jobType, pageable);
        List<UserInfoResultSet> matchedProfiles = filterList.getContent();
        TalentViewResponse response = new TalentViewResponse();
        try {
            List<Long> consultancyIdList = new ArrayList<>();
            List<Long> applicantIdList = new ArrayList<>();
            List<TalentViewDTO> talentViewDTOs = new ArrayList<>();
            TalentViewDTO talentViewDTO;
            // seperate consultancy with normal appliacnts
            for (UserInfoResultSet jobMatch : matchedProfiles) {
                if (jobMatch.getConsultancyId() != 0) {
                    consultancyIdList.add(jobMatch.getConsultancyId());
                } else {
                    applicantIdList.add(jobMatch.getApplicantId());
                }
            }
            // fetch consultancy info
            Map<Long, Consultancy> consultancyMap = new HashMap<>();
            List<Consultancy> consultancyList = (List<Consultancy>) consultancyRepository
                    .findAllById(consultancyIdList);
            for (Consultancy consultancy : consultancyList) {
                consultancyMap.put(consultancy.getConsultancyId(), consultancy);
            }
            // fetch user info
            Map<Long, Applicant> applicantMap = new HashMap<>();
            List<Applicant> applicantList = (List<Applicant>) applicantRepository.findAllById(applicantIdList);
            for (Applicant applicant : applicantList) {
                applicantMap.put(applicant.getApplicantId(), applicant);
            }
            // prepare response for ui
            for (UserInfoResultSet jobMatch : matchedProfiles) {
                talentViewDTO = new TalentViewDTO();
                talentViewDTO.setConsultant(jobMatch.getConsultancyId() != 0);
                talentViewDTO.setJobFunctionName(null);
                talentViewDTO.setIsintrested(false);
                if (jobMatch.getConsultancyId() != 0) {
                    Consultancy con = consultancyMap.get(jobMatch.getConsultancyId());
                    talentViewDTO.setProfileId(con.getConsultancyId());
                    talentViewDTO.setProfileName(con.getName());
                    String imageUrl = con.getProfileImageUrl();
                    try {
                        talentViewDTO.setProfilePic(imageService.getImage(imageUrl));
                    } catch (IOException e) {
                        talentViewDTO.setProfilePic(null);
                    }
                } else {
                    Applicant appl = applicantMap.get(jobMatch.getApplicantId());
                    talentViewDTO.setProfileId(appl.getApplicantId());
                    talentViewDTO.setProfileName(appl.getFirstName());
                    String imageUrl = appl.getProfileImageUrl();
                    try {
                        talentViewDTO.setProfilePic(imageService.getImage(imageUrl));
                    } catch (IOException e) {
                        talentViewDTO.setProfilePic(null);
                    }
                }
                talentViewDTOs.add(talentViewDTO);
            }
            response.setTotalCount(filterList.getTotalElements());
            response.setTotalPages(filterList.getTotalPages());
            response.setTalentViewDTO(talentViewDTOs);
        } catch (Exception e) {
            log.error("Exception in job search by filter", e);
        }
        return response;
    }

    /**
     * Method to update job applicant state in job match saved or
     * shortlisted/rejected
     *
     * @param jobPostId
     * @param consultancyId
     * @param applicantId
     * @param jobStateObj
     */
    public void updateJobApplicantState(long jobPostId, long consultancyId, long applicantId,
                                        UpdateJobStateDTO jobStateObj, long updatingConsultancyId, long updatingConsultantUserId) throws NotificationException {
        String action = jobStateObj.getAction();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("applicantId").descending());

        Long recruiterId = jobStateObj.getRecruiterId();
        if ("Save".equalsIgnoreCase(action)) {
            // Save applicant state
            saveApplicantForRecruiterInConsultancy(jobPostId, consultancyId, applicantId, recruiterId);
        } else if ("Shortlist".equalsIgnoreCase(action)) {

            // Update applicant_state as shortlist
            saveJobStateForApplicantInConsultancy(jobPostId, consultancyId, applicantId, recruiterId,
                    JobApplState.SHORTLISTED.getValue(), jobStateObj, updatingConsultancyId, updatingConsultantUserId, pageable);

            Page<JobMatchConsultancy> matchedJobMatchConsList = jobMatchConsultancyRepository
                    .findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(jobPostId, consultancyId, 1, 1, pageable);

            JobMatchConsultancy jobMatchCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId, applicantId);
            User updatedConsultancyUser = null;
            if (jobMatchCons.getConsultancyUserId() != null) {
                updatedConsultancyUser = userRepository.findById(jobMatchCons.getUpdatedByConsUserId() != null ? jobMatchCons.getUpdatedByConsUserId() : jobMatchCons.getConsultancyUserId()).get();
            }
            //  User consultancyUser = userRepository.findByConsultancyId(consultancyId);
            User user = userRepository.findById(recruiterId).get();
            Company company = companyRepository.findById(user.getCompanyId()).get();
            Calendar calendar1 = Calendar.getInstance();
            calendar1.add(Calendar.HOUR, 1); // set the reminder time to 1 hour from now
            Date reminderTime1 = calendar1.getTime();
            Applicant applicant = applicantRepository.findById(applicantId).get();
            String notificationMessage = "";
            try {
                if (updatedConsultancyUser != null && updatedConsultancyUser.getNotificationToken() != null) {
                    notificationMessage = applicant.getFirstName() + " " + applicant.getLastName() + " Applicant shortlisted by " + jobStateObj.getRecruiterName() + " from " + company.getName() + " for the " + jobStateObj.getJobTitle() + ".";
                    notificationScheduler.scheduleNotification("Applicants shortlisted.", notificationMessage, updatedConsultancyUser.getNotificationToken(), reminderTime1, updatedConsultancyUser.getUserId(), updatedConsultancyUser.getConsultancyId());
                }
//                if (consultancyUser.getNotificationToken() != null) {
//                    notificationMessage = consultancyUser.getFirstName() + " " + consultancyUser.getLastName() + " has matched with " + company.getName() + " for " + jobStateObj.getJobTitle() + " job. " + matchedJobMatchConsList.getTotalElements() + " profiles matched.";
//                    notificationScheduler.scheduleNotification("New Match.", notificationMessage, consultancyUser.getNotificationToken(), reminderTime1, consultancyUser.getUserId(), consultancyUser.getConsultancyId());
//                }
            } catch (SchedulerException e) {
                throw new NotificationException("Schedule notification failed");
            }

        } else if ("Reject".equalsIgnoreCase(action)) {
            saveJobStateForApplicantInConsultancy(jobPostId, consultancyId, applicantId, recruiterId,
                    JobApplState.REJECTED.getValue(), jobStateObj, updatingConsultancyId, updatingConsultantUserId, pageable);
        }
    }

    private void saveApplicantForRecruiterInConsultancy(long jobPostId, long consultancyId, long applicantId,
                                                        long recruiterId) {
        JobMatchConsultancy jobMatchObj = createJobMatchConObject(jobPostId, consultancyId, applicantId, recruiterId);
        // JobMatch jobMatch = createJobMatchObject(jobPostId, consultancyId,
        // applicantId, recruiterId);
        jobMatchObj.setUpdatedDate(new Date());
        jobMatchObj.setSavedByRecruiter(true);
        jobMatchObj.setSavedRecruiterId(recruiterId);
        jobMatchObj.setSavedRecruiterDate(new Date());
        jobMatchObj.setLastActionPerformedRecruiterId(recruiterId);
        // jobMatch.setSavedByRecruiter(true);

        // 3. updated saved state
        jobMatchConsultancyRepository.save(jobMatchObj);
        // jobMatchingRepository.save(jobMatch);
    }


    private void saveJobStateForApplicantInConsultancy(long jobPostId, long consultancyId, long applicantId,
                                                       long recruiterId, int jobApplState, UpdateJobStateDTO jobStateObj, long updatingConsultancyId, long updatingConsultancyUserId, Pageable pageable) {
        JobMatchConsultancy jobMatchObj = createJobMatchConObject(jobPostId, consultancyId, applicantId, recruiterId);
        // JobMatch jobMatch = createJobMatchObject(jobPostId, consultancyId,
        // applicantId, recruiterId);
        // updated applicant state JobApplState - Shortlist/reject
        jobMatchObj.setSavedByRecruiter(false);
        jobMatchObj.setApplicantStatus(jobApplState);
        jobMatchObj.setUpdatedDate(new Date());
        jobMatchObj.setRecruiterId(recruiterId);
        jobMatchObj.setLastActionPerformedRecruiterId(recruiterId);
        // jobMatchObj.setUpdatedByConsId(updatingConsultancyId);
        jobMatchObj.setRecruiterUpdatedDate(new Date());

        if (jobMatchObj.getApplicantJobStatus() == 0) {
            jobMatchObj.setInterviewStatus(1);
        }
        jobMatchConsultancyRepository.save(jobMatchObj);

        if (jobMatchObj.getApplicantJobStatus() == 1 && jobMatchObj.getApplicantStatus() == 1) {

            String message = jobStateObj.getRecruiterName() + " have matched with " + jobStateObj.getApplicantName()
                    + " from " + jobStateObj.getConsultancyName() + " for " + jobStateObj.getJobTitle() + " job.";
            alertService.saveAlertInfo(recruiterId, message, updatingConsultancyId);

        } else if (jobMatchObj.getApplicantStatus() == 1) {
            Page<JobMatchConsultancy> jobMatchConsList = jobMatchConsultancyRepository
                    .findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(jobPostId, consultancyId, 1, 0, pageable);
            String message = jobMatchConsList.getTotalElements() + " profiles shortlisted for "
                    + jobStateObj.getJobTitle() + " job by " + jobStateObj.getRecruiterName();
            alertService.saveAlertInfo(recruiterId, message, updatingConsultancyId);
        }
    }

    private JobMatchConsultancy createJobMatchConObject(long jobPostId, long consultancyId, long applicantId,
                                                        long recruiterId) {
        // 1. Load Job Match Consultancy object
        JobMatchConsultancy jobMatchObj = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId,
                applicantId);

        // 2. If not found insert one ???
        if (jobMatchObj == null) {
            jobMatchObj = new JobMatchConsultancy();
            jobMatchObj.setApplicantId(applicantId);
            jobMatchObj.setConsultancyId(consultancyId);
            jobMatchObj.setJobPostId(jobPostId);
            jobMatchObj.setRecruiterId(recruiterId);
        }
        jobMatchObj.setUpdatedByRecId(recruiterId);
        jobMatchObj.setRecruiterUpdatedDate(new Date());

        return jobMatchObj;
    }

    private JobMatch createJobMatchObject(long jobPostId, long consultancyId, long applicantId, long recruiterId) {
        // 1. Load Job Match object
        JobMatch jobMatchObj = jobMatchingRepository.findByJobPostIdAndApplicantId(jobPostId, consultancyId);
        // 2. If not found insert one ???
        if (jobMatchObj == null) {
            jobMatchObj = new JobMatch();
            jobMatchObj.setApplicantId(consultancyId);
            jobMatchObj.setJobPostId(jobPostId);
            jobMatchObj.setRecruiterId(recruiterId);
        }
        jobMatchObj.setUpdatedByRecId(recruiterId);
        jobMatchObj.setRecruiterUpdatedDate(new Date());
        return jobMatchObj;
    }

    public void updateJobApplicantStateForApplicant(long jobPostId, long applicantId, UpdateJobStateDTO jobStateObj, long consultantId) {
        String action = jobStateObj.getAction();
        Long recruiterId = jobStateObj.getRecruiterId();
        if ("Save".equalsIgnoreCase(action)) {
            // Save applicant state
            saveApplicantForRecruiter(jobPostId, applicantId, recruiterId);

        } else if ("Shortlist".equalsIgnoreCase(action)) {
            // Update applicant_state as shortlist
            saveJobStateForApplicant(jobPostId, applicantId, recruiterId, JobApplState.SHORTLISTED.getValue());

            User recruiterUser = userRepository.findById(recruiterId).get();
            Long companyId = recruiterUser.getCompanyId();
            Company company = companyRepository.findById(companyId).get();
            JobPost jobPost = jobPostRepository.findById(jobPostId).get();
            JobMatchConsultancy jobMactCons = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId, applicantId);

            String message = jobStateObj.getApplicantName() + " shortlisted by " + jobStateObj.getRecruiterName() + " from " + company.getName() + " for the " + jobPost.getTitle() + ".";
            alertService.saveAlertInfo(jobMactCons.getConsultancyUserId(), message, consultantId);
        } else if ("Reject".equalsIgnoreCase(action)) {
            saveJobStateForApplicant(jobPostId, applicantId, recruiterId, JobApplState.REJECTED.getValue());
        }

    }

    public void updateJobApplicantStateforConsultancy(long jobPostId, long consultancyId, long applicantId,
                                                      UpdateJobStateDTO jobStateObj) {
        String action = jobStateObj.getAction();
        Long recruiterId = jobStateObj.getRecruiterId();
        JobMatchConsultancy jobMatchObj = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId,
                applicantId);
        if ("Save".equalsIgnoreCase(action)) {
            jobMatchObj.setSavedByRecruiter(true);

        } else if ("Reject".equalsIgnoreCase(action)) {

            jobMatchObj.setSavedByRecruiter(false);
            jobMatchObj.setApplicantStatus(JobApplState.REJECTED.getValue());
        }

        jobMatchObj.setUpdatedByConsId(consultancyId);
        jobMatchObj.setUpdatedByConsUserId(recruiterId);
        jobMatchObj.setUpdatedDate(new Date());
        jobMatchConsultancyRepository.save(jobMatchObj);
    }

    private void saveApplicantForRecruiter(long jobPostId, long applicantId, Long recruiterId) {
        JobMatch jobMatchObj = createJobMatchObject(jobPostId, applicantId, recruiterId);
        // updated applicant state JobApplState - Shortlist/reject
        jobMatchObj.setSavedByRecruiter(true);
        jobMatchingRepository.save(jobMatchObj);
    }

    private void saveJobStateForApplicant(long jobPostId, long applicantId, Long recruiterId, int jobApplState) {
        JobMatch jobMatchObj = createJobMatchObject(jobPostId, applicantId, recruiterId);
        // updated applicant state JobApplState - Shortlist/reject
        jobMatchObj.setApplicantStatus(jobApplState);
        jobMatchObj.setSavedByRecruiter(false);
        jobMatchObj.setInterviewStatus(1);
        jobMatchingRepository.save(jobMatchObj);
    }

    private JobMatch createJobMatchObject(long jobPostId, long applicantId, long recruiterId) {
        // 1. Load Job Match Consultancy object
        JobMatch jobMatchObj = jobMatchingRepository.findByJobPostIdAndApplicantIdAndIsConsultancy(jobPostId,
                applicantId, false);
        // 2. If not found insert one ???
        if (jobMatchObj == null) {
            jobMatchObj = new JobMatch();
            jobMatchObj.setApplicantId(applicantId);
            jobMatchObj.setConsultancy(false);
            jobMatchObj.setJobPostId(jobPostId);
            jobMatchObj.setRecruiterId(recruiterId);
        }
        jobMatchObj.setUpdatedByRecId(recruiterId);
        jobMatchObj.setRecruiterUpdatedDate(new Date());
        return jobMatchObj;
    }

//    public TalentViewResponse getUsersByFilter(TalentFilterDTO talentFilterDTO, Integer pageNo, Integer pageSize)
//            throws WorkruitException {
//
//        String selectQuery = "SELECT DISTINCT c";
//        String selectQuery2 = "SELECT DISTINCT a.consultancyId,a.applicantId";
//        String countSql = "SELECT COUNT(DISTINCT a.consultancyId) ";
//        StringBuilder sqlBuilder = new StringBuilder(
//                " FROM Applicant a " + "LEFT JOIN ApplicantDetails ad ON a.applicantId = ad.applicantId "
//                        + "LEFT JOIN ApplicantJobFunction ajf ON a.applicantId = ajf.applicantId "
//                        + "LEFT JOIN ApplicantSecondaryJobFunction asjf ON a.applicantId = asjf.applicantId "
//                        + "LEFT JOIN ApplicantJobSkill ajs ON a.applicantId = ajs.applicantId "
//                        + "LEFT JOIN EducationHistory eh ON eh.applicantId = a.applicantId  " +
//                        " INNER JOIN Consultancy c on c.consultancyId = a.consultancyId WHERE a.correctionRequired=0 ");
//        Map<String, Object> params = new HashMap<>();
//        if (talentFilterDTO.getJobFunction() != null) {
//            sqlBuilder.append(" AND (ajf.jobFunctionId = :jobFunction OR asjf.jobFunctionId = :jobFunction)");
//            params.put("jobFunction", talentFilterDTO.getJobFunction());
//        }
//        if (talentFilterDTO.getLocation() != null && !talentFilterDTO.getLocation().equals("")) {
//            sqlBuilder.append(" AND a.location = :location");
//            params.put("location", talentFilterDTO.getLocation());
//        }
//        if (talentFilterDTO.getJobTypes() != null && !talentFilterDTO.getJobTypes().isEmpty()) {
//            sqlBuilder.append(" AND ad.jobType IN (:jobTypes)");
//            params.put("jobTypes", talentFilterDTO.getJobTypes());
//        }
//        if (talentFilterDTO.getWorkMode() != null && !talentFilterDTO.getWorkMode().isEmpty()) {
//            sqlBuilder.append(" AND ad.preferredWorkMode IN (:preferredWorkMode)");
//            params.put("preferredWorkMode", talentFilterDTO.getWorkMode());
//        }
//        if (talentFilterDTO.getExpMin() != null && talentFilterDTO.getExpMax() != null) {
//            sqlBuilder.append(" AND ad.yearsOfExperience >= :yearsOfExpMin AND ad.yearsOfExperience <= :yearsOfExpMax");
//            params.put("yearsOfExpMin", talentFilterDTO.getExpMin());
//            params.put("yearsOfExpMax", talentFilterDTO.getExpMax());
//        }
//        if (talentFilterDTO.getCitizenship() != null && !talentFilterDTO.getCitizenship().isEmpty()) {
//            sqlBuilder.append(" AND ad.citizenship IN (:citizenshipList)");
//            params.put("citizenshipList", talentFilterDTO.getCitizenship());
//        }
//        if (talentFilterDTO.getEduQualification() != null && !talentFilterDTO.getEduQualification().isEmpty()) {
//            sqlBuilder.append(" AND eh.degree IN (:eduQualification)");
//            params.put("eduQualification", talentFilterDTO.getEduQualification());
//        }
//        if (talentFilterDTO.getNoticePeriod() != null && !talentFilterDTO.getNoticePeriod().isEmpty()) {
//            sqlBuilder.append(" AND ad.noticePeriod IN (:noticePeriod)");
//            params.put("noticePeriod", talentFilterDTO.getNoticePeriod());
//        }
//        if (talentFilterDTO.getJobSkills() != null && !talentFilterDTO.getJobSkills().isEmpty()) {
//            sqlBuilder.append(" AND ajs.jobSkillId IN (:jobSkillId)");
//            params.put("jobSkillId", talentFilterDTO.getJobSkills());
//        }
//        //sqlBuilder.append(" order by a.consultancyId ");
//        Query countQuery = entityManager.createQuery(countSql + sqlBuilder.toString());
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            countQuery.setParameter(entry.getKey(), entry.getValue());
//        }
//        long totalCount = (long) countQuery.getSingleResult();
//        Query query = entityManager.createQuery(selectQuery + sqlBuilder.toString()).setFirstResult(pageNo * pageSize)
//                .setMaxResults(pageSize);
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            query.setParameter(entry.getKey(), entry.getValue());
//        }
//        Query applicantsQuery = entityManager.createQuery(selectQuery2 + sqlBuilder.toString());
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            applicantsQuery.setParameter(entry.getKey(), entry.getValue());
//        }
//        List<Consultancy> results = query.getResultList();
//        if (results.size() == 0) {
//            return null;
//        }
//        TalentViewResponse talentViewResponse = genrateTalentViewResponse(results, applicantsQuery.getResultList());
//        talentViewResponse.setTotalCount(totalCount);
//        talentViewResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
//        return talentViewResponse;
//    }

    public TalentViewResponse getUsersByFilter(TalentFilterDTO talentFilterDTO, Long consultancyId, String role, Integer pageNo, Integer pageSize)
            throws WorkruitException {

        String selectQuery = "SELECT DISTINCT c.consultancy_id,MAX(subquery.match_score) AS maxMatchScore,MAX(subquery.applicant_job_status) ";
        String selectQuery2 = "SELECT DISTINCT a.consultancy_id,a.applicant_id ";
        String countSql = "SELECT COUNT(DISTINCT a.consultancy_id) ";
        StringBuilder sqlBuilder = new StringBuilder(
                "  from applicant a\n" +
                        "left join applicant_details ad on ad.applicant_id=a.applicant_id\n" +
                        "inner join consultancy c on c.consultancy_id=a.consultancy_id\n" +
                        "left JOIN (\n" +
                        "    SELECT jc.applicant_id, jc.applicant_job_status, jc.match_score, jc.saved_recruiter, jc.job_post_id\n" +
                        "    FROM job_match_consultancy jc where jc.applicant_status=0 and jc.saved_recruiter=0  and  jc.job_post_id=:jobPostId" +
                        "    ORDER BY jc.applicant_job_status DESC, jc.match_score DESC\n" +
                        ") AS subquery ON subquery.applicant_id = a.applicant_id WHERE a.correction_required=0  " +
                        " and a.consultancy_id not in (select cj.consultancy_id from consultancy_job_status cj where cj.status in (2) and a.consultancy_id=cj.consultancy_id)" +
//                        "AND NOT EXISTS (" +
//                        " SELECT 1 " +
//                        "   FROM job_match_consultancy jmc" +
//                        "   WHERE jmc.applicant_id = a.applicant_id" +
//                        "   AND jmc.job_post_id = :jobPostId" +
//                        "  AND jmc.applicant_status > 0 ) " +
//                        " AND NOT exists ( select j.job_post_id from job_match_consultancy j \n" +
//                        " where j.job_post_id=:jobPostId \n" +
//                        " and j.job_post_id in (select a.job_post_id from consultancy_job_status a where a.status =2 and j.job_post_id=a.job_post_id) group by j.job_post_id)" +
//                        "  and subquery.saved_recruiter = 0" +
                        " ");
        Map<String, Object> params = new HashMap<>();
        params.put("jobPostId", talentFilterDTO.getJobId());

        if (role.equals("COMPANY_ADMIN") || role.equals("CONSULTANCY_ADMIN")) {
            sqlBuilder.append(" and a.consultancy_id != :consultancyId ");
            params.put("consultancyId", consultancyId);
        }

        if (talentFilterDTO.getJobFunction() != null) {
            sqlBuilder.append(" AND (ad.job_function_id = :jobFunction OR FIND_IN_SET(:jobFunction, ad.secondary_job_function_id) > 0) ");

            params.put("jobFunction", talentFilterDTO.getJobFunction());
        }
        if (talentFilterDTO.getLocation() != null && !talentFilterDTO.getLocation().equals("")) {
            sqlBuilder.append(" AND a.location = :location");
            params.put("location", talentFilterDTO.getLocation());
        }

        if (talentFilterDTO.getJobTypes() != null && !talentFilterDTO.getJobTypes().isEmpty()) {
            sqlBuilder.append(" AND ad.job_type IN (:jobTypes)");
            params.put("jobTypes", talentFilterDTO.getJobTypes());
        }
        if (talentFilterDTO.getWorkMode() != null && !talentFilterDTO.getWorkMode().isEmpty()) {
            sqlBuilder.append(" AND ad.preferred_work_mode IN (:preferredWorkMode)");
            params.put("preferredWorkMode", talentFilterDTO.getWorkMode());
        }
        if (talentFilterDTO.getExpMin() != null && talentFilterDTO.getExpMax() != null) {
            sqlBuilder.append(" AND ad.years_of_exp >= :yearsOfExpMin AND ad.years_of_exp <= :yearsOfExpMax");
            params.put("yearsOfExpMin", talentFilterDTO.getExpMin());
            params.put("yearsOfExpMax", talentFilterDTO.getExpMax());
        }
        if (talentFilterDTO.getCitizenship() != null && !talentFilterDTO.getCitizenship().isEmpty()) {
            sqlBuilder.append(" AND ad.citizenship IN (:citizenshipList)");
            params.put("citizenshipList", talentFilterDTO.getCitizenship());
        }
        if (talentFilterDTO.getEduQualification() != null && !talentFilterDTO.getEduQualification().isEmpty()) {
            sqlBuilder.append(" AND ad.degree_id IN (:eduQualification) ");

            params.put("eduQualification", talentFilterDTO.getEduQualification());
        }
        if (talentFilterDTO.getNoticePeriod() != null && !talentFilterDTO.getNoticePeriod().isEmpty()) {
            sqlBuilder.append(" AND ad.notice_period IN (:noticePeriod)");
            params.put("noticePeriod", talentFilterDTO.getNoticePeriod());
        }
        if (talentFilterDTO.getJobSkills() != null && !talentFilterDTO.getJobSkills().isEmpty()) {
//            sqlBuilder.append(" AND ajs.job_skill_id IN (:jobSkillId)");
//            params.put("jobSkillId", talentFilterDTO.getJobSkills());
            sqlBuilder.append(" AND ( ");
            for (Integer skillId : talentFilterDTO.getJobSkills()) {
                sqlBuilder.append("FIND_IN_SET('").append(skillId).append("', ad.skill_id) > 0");
                if (skillId != talentFilterDTO.getJobSkills().get(talentFilterDTO.getJobSkills().size() - 1)) {
                    sqlBuilder.append(" OR ");
                }
            }
            sqlBuilder.append(") ");
        }
        //sqlBuilder.append(" order by a.consultancyId ");
        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();
        Query applicantsQuery = entityManager.createNativeQuery(selectQuery2 + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            applicantsQuery.setParameter(entry.getKey(), entry.getValue());
        }
        sqlBuilder.append(" group by c.consultancy_id  ORDER BY MAX(subquery.applicant_job_status) DESC, MAX(subquery.match_score) DESC,c.consultancy_id desc ");
        Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder).setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> results = query.getResultList();
        if (results.size() == 0) {
            return new TalentViewResponse();
        }
        TalentViewResponse talentViewResponse = genrateTalentViewResponse(results, talentFilterDTO, applicantsQuery.getResultList());
        talentViewResponse.setTotalCount(totalCount);
        talentViewResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return talentViewResponse;
    }

    /**
     * Method to genrate TalentViewConsDetailsDTO
     *
     * @param applicantList
     * @param consultancy
     * @param jobPostId
     */
    private TalentViewConsDetailsDTO genrateTalentViewConsDetailsDTO(List<Long> applicantList, Consultancy consultancy,
                                                                     Long jobPostId) throws WorkruitException, JsonProcessingException {
        TalentViewConsDetailsDTO talentViewConsDetailsDTO = new TalentViewConsDetailsDTO();
        List<AppledProfilesDTO> appledProfilesDTOs = new ArrayList<>();
        AppledProfilesDTO appledProfilesDTO;

        List<Applicant> applicants = (List<Applicant>) applicantRepository.findAllById(applicantList);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantList);

        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
        for (Applicant applicant : applicants) {
            appledProfilesDTO = new AppledProfilesDTO();
            appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());

            ApplicantDetails applicantDetails = applicantDetailsMap.get(applicant.getApplicantId());

            JobMatchConsultancy jobMatchDetails = jobMatchConsultancyRepository.findByJobPostIdAndApplicantId(jobPostId,
                    applicant.getApplicantId());

            WorkExperience workExperiences = workExperienceRepository
                    .findByWorkExperienceIdAndApplicantId(applicant.getApplicantId());

            appledProfilesDTO.setApplicantTitle(workExperiences == null ? "Not Found"
                    : workExperiences.getJobTitle() + " : " + workExperiences.getCompanyName());

            appledProfilesDTO.setApplicantStatus(jobMatchDetails != null ? jobMatchDetails.getApplicantJobStatus() : 0);
            if (applicantDetails != null) {
                appledProfilesDTO.setExperience(String.valueOf(applicantDetails.getYearsOfExperience()));
                appledProfilesDTO.setJobFunc(applicantDetails.getJobFunction());
                appledProfilesDTO.setSecondaryJobFunc(applicantDetails.getSecondaryJobFunction());
            }
            appledProfilesDTO.setLocation(applicant.getLocation());
            appledProfilesDTO.setApplicantId(applicant.getApplicantId());
            appledProfilesDTO.setRecommended(jobMatchDetails.getMatchScore() > 95);
            appledProfilesDTO.setMatchScore(jobMatchDetails.getMatchScore());
            String imageUrl = applicant.getProfileImageUrl();
            try {
                appledProfilesDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
            } catch (IOException e) {
                appledProfilesDTO.setProfilePic(null);
            }
            appledProfilesDTOs.add(appledProfilesDTO);
        }
        //appledProfilesDTOs.sort(Comparator.comparing(a -> a.getApplicantStatus()));
        // Collections.reverse(appledProfilesDTOs);
        appledProfilesDTOs.sort(Comparator.comparing(AppledProfilesDTO::getApplicantStatus).thenComparing(AppledProfilesDTO::getMatchScore).reversed());

//        appledProfilesDTOs.stream().sorted((p1, p2) -> Integer
//                        .compare(p2.getApplicantStatus(), p1.getApplicantStatus()))
//                .collect(Collectors.toList());

        talentViewConsDetailsDTO.setAppliedProfiles(appledProfilesDTOs);
        talentViewConsDetailsDTO.setConsultancyId(consultancy.getConsultancyId());

        List<Long> idList = new ArrayList<>();
        idList.add(consultancy.getConsultancyId());


        talentViewConsDetailsDTO.setConsultancyName(consultancy.getName());
        talentViewConsDetailsDTO.setFoundedIn(consultancy.getFoundedDate());
        talentViewConsDetailsDTO.setAbout(consultancy.getAbout());
        talentViewConsDetailsDTO.setDomainSpec(consultancy.getDomains());

        talentViewConsDetailsDTO.setIndustryType(consultancy.getIndustryTypes());
        talentViewConsDetailsDTO.setLocation(consultancy.getLocation());

        String imageUrl = consultancy.getProfileImageUrl();
        try {
            talentViewConsDetailsDTO.setProfilePicUrl(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
        } catch (IOException e) {
            talentViewConsDetailsDTO.setProfilePicUrl(null);
        }

        talentViewConsDetailsDTO.setWebsite(consultancy.getWebsite());

        talentViewConsDetailsDTO.setSize(consultancy.getNumberOfEmployees());
//        if (consultancy.getClients() != null)
//            talentViewConsDetailsDTO.setClients(consultancy.getClients().stream()
//                    .map(cl -> modelMapper.map(cl, ClientDTO.class)).collect(Collectors.toSet()));
        List<User> usersList = userRepository.findByConsultancyUsersIds(idList);

        CompanyDetailsDTO companyDetailsDTO = companyService.getCompany(usersList.get(0).getCompanyId());
        if (companyDetailsDTO.getClients() != null)
            talentViewConsDetailsDTO.setClients(companyDetailsDTO.getClients());
        if (companyDetailsDTO.getDomains() != null)
            talentViewConsDetailsDTO.setDomainSpec(companyDetailsDTO.getDomains().stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));
        talentViewConsDetailsDTO.setOverallTalent(consultancy.getOverallTalentPool());
        talentViewConsDetailsDTO.setFbLink(consultancy.getFacebookLink());
        talentViewConsDetailsDTO.setTwLink(consultancy.getTwitterLink());
        talentViewConsDetailsDTO.setLiLink(consultancy.getLinkedinLink());

        long intrestedCount = jobMatchConsultancyRepository.getProfilesByInterviewStatus(jobPostId,
                consultancy.getConsultancyId(), 1);

        talentViewConsDetailsDTO.setIntrestedCount(intrestedCount);

        return talentViewConsDetailsDTO;
    }

    private List<TalentViewDTO> genrateTalentViewResponseforDashboard(List<JobMatchConsultancy> jobMatchList, Long jobId) {
        TalentViewResponse talentViewResponse = new TalentViewResponse();

        List<Long> consultancyIdList = new ArrayList<>();
        // 2. seperate consultancy with normal appliacnts
        for (JobMatchConsultancy jobMatch : jobMatchList) {
            consultancyIdList.add(jobMatch.getConsultancyId());
        }
        // List<Consultancy> consultancyList = (List<Consultancy>) consultancyRepository.findAllById(consultancyIdList);

        List<TalentViewDTO> talentViewDTOs = new ArrayList<>();
        TalentViewDTO talentViewDTO;
        for (JobMatchConsultancy jobMatch : jobMatchList) {
            talentViewDTO = new TalentViewDTO();
            talentViewDTO.setConsultant(true);
            talentViewDTO.setJobFunctionName(null);
            Consultancy consultancy = consultancyRepository.findById(jobMatch.getConsultancyId()).get();
            long applicantsCount = jobMatchConsultancyRepository.findCountofConsultants(jobId,
                    consultancy.getConsultancyId());
            long intrestedCount = jobMatchConsultancyRepository.getProfilesByInterviewStatus(jobId,
                    consultancy.getConsultancyId(), 1);
            long recommendedCount = jobMatchConsultancyRepository.getProfilesByRecommendedStatus(jobId,
                    consultancy.getConsultancyId());
            talentViewDTO.setIsintrested(intrestedCount >= 1);
            talentViewDTO.setRecommended(recommendedCount > 0);
            talentViewDTO.setMatchScore(recommendedCount > 0 ? 100 : jobMatch.getMatchScore());
            talentViewDTO.setApplicantStatus(intrestedCount >= 1 ? 1 : 0);
            talentViewDTO.setProfileId(consultancy.getConsultancyId());
            talentViewDTO.setProfileName(consultancy.getName());

            String imageUrl = consultancy.getProfileImageUrl();
            try {
                talentViewDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                talentViewDTO.setProfilePic(null);
            }

            talentViewDTO.setJobFunctionName(consultancy.getDomains());
            talentViewDTO.setLocation(consultancy.getLocation());
            talentViewDTO.setCount(applicantsCount);
            talentViewDTO.setIntrestedCount(intrestedCount);
            talentViewDTOs.add(talentViewDTO);
        }

        return talentViewDTOs;
    }

//    private TalentViewResponse genrateTalentViewResponse(List<Consultancy> consultancyList, List<Object[]> results) {
//
//        TalentViewResponse talentViewResponse = new TalentViewResponse();
//        Map<Long, List<Long>> consAppMap = new HashMap<>();
//        for (Object[] row : results) {
//            if (consAppMap.containsKey((Long) row[0])) {
//                List<Long> applicantIds = consAppMap.get((Long) row[0]);
//                applicantIds.add((Long) row[1]);
//            } else {
//                List<Long> applicantIds = new ArrayList<>();
//                applicantIds.add((Long) row[1]);
//                consAppMap.put((Long) row[0], applicantIds);
//            }
//        }
//        //   List<Consultancy> consultancyList = (List<Consultancy>) consultancyRepository.findAllById(consultancyIdList);
//        List<TalentViewDTO> talentViewDTOs = new ArrayList<>();
//        TalentViewDTO talentViewDTO;
//        for (Consultancy consultancy : consultancyList) {
//            talentViewDTO = new TalentViewDTO();
//            talentViewDTO.setConsultant(true);
//            long applicantsCount = consAppMap.get(consultancy.getConsultancyId()).size();
//            List<ApplicantRecommendedResultSet> applicantsWithMatchScoreAndStatus = jobMatchConsultancyRepository.getApplicantsMatchScore(consAppMap.get(consultancy.getConsultancyId()))
//                    .stream().sorted(Comparator.comparing(ApplicantRecommendedResultSet::getApplicantStatus).thenComparing(ApplicantRecommendedResultSet::getMatchScore).reversed()).collect(Collectors.toList());
////            Map<Long, Map<Long, Integer>> applicantJobMap = applicantsWithMatchScoreAndStatus.stream()
////                    .collect(Collectors.groupingBy(ApplicantRecommendedResultSet::getId, LinkedHashMap::new,
////                            Collectors.toMap(ApplicantRecommendedResultSet::getJobPostId, ApplicantRecommendedResultSet::getMatchScore, (score1, score2) -> score1, LinkedHashMap::new)));
//            if(applicantsWithMatchScoreAndStatus != null && !applicantsWithMatchScoreAndStatus.isEmpty()){
//                if(applicantsWithMatchScoreAndStatus.get(0).getMatchScore()>=95){
//                    talentViewDTO.setRecommended(true);
//                }else {
//                    talentViewDTO.setRecommended(false);
//                }
//                talentViewDTO.setMatchScore(applicantsWithMatchScoreAndStatus.get(0).getMatchScore());
//            }
//            long intrestedCount = 0;
//            // long intrestedCount = jobMatchConsultancyRepository.getProfilesByInterviewStatusForCons(consultancy.getConsultancyId(), 1, consAppMap.get(consultancy.getConsultancyId()));
//            talentViewDTO.setIsintrested(intrestedCount != 0 ? true : false);
//            talentViewDTO.setProfileId(consultancy.getConsultancyId());
//            talentViewDTO.setProfileName(consultancy.getName());
//            String imageUrl = consultancy.getProfileImageUrl();
//            try {
//                talentViewDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : null);
//            } catch (IOException e) {
//                talentViewDTO.setProfilePic(null);
//            }
//            talentViewDTO.setJobFunctionName(consultancy.getDomains());
//            talentViewDTO.setLocation(consultancy.getLocation());
//            talentViewDTO.setCount(applicantsCount);
//            talentViewDTOs.add(talentViewDTO);
//        }
//        talentViewResponse.setTalentViewDTO(talentViewDTOs.stream().sorted(Comparator.comparing(TalentViewDTO::isRecommended).thenComparing(TalentViewDTO::getMatchScore).reversed()).collect(Collectors.toList()));
//        return talentViewResponse;
//    }

    private TalentViewResponse genrateTalentViewResponse(List<Object[]> consultancyResults, TalentFilterDTO talentFilterDTO, List<Object[]> results) {

        TalentViewResponse talentViewResponse = new TalentViewResponse();
        Map<Long, List<Long>> consAppMap = new HashMap<>();
        for (Object[] row : results) {
            long key = ((BigInteger) row[0]).longValue();
            long value = ((BigInteger) row[1]).longValue();
            consAppMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        List<Long> consultancyIdList = new ArrayList<>();
        Map<Long, Integer> consultancyMatchScore = new HashMap<>();
        Map<Long, Integer> consultancyAppStatus = new HashMap<>();
        for (Object[] row : consultancyResults) {
            Long consultancyId = ((BigInteger) row[0]).longValue();
            consultancyMatchScore.put(consultancyId, row[1] != null ? ConsultancyJobService.getIntegerValue(row[1]) : null);
            consultancyAppStatus.put(consultancyId, row[2] != null ? ConsultancyJobService.getIntegerValue(row[2]) : null);

            consultancyIdList.add(consultancyId);
        }
        List<Consultancy> consultancyList = (List<Consultancy>) consultancyRepository.findAllById(consultancyIdList);
        List<TalentViewDTO> talentViewDTOs = new ArrayList<>();
        TalentViewDTO talentViewDTO;
        for (Consultancy consultancy : consultancyList) {
            Long consultancyId = consultancy.getConsultancyId();
            List<Long> applicantIds = consAppMap.get(consultancyId);
            talentViewDTO = new TalentViewDTO();
            talentViewDTO.setConsultant(true);
            long applicantsCount = applicantIds.size();
            long interestedCount = 0;
            Integer matchScore = consultancyMatchScore.get(consultancyId);
            Integer status;
            if (matchScore != null) {
                interestedCount = jobMatchConsultancyRepository.interestedCountForConsultancy(consultancyId, applicantIds, talentFilterDTO.getJobId());
                status = consultancyAppStatus.get(consultancyId);
            } else {
                List<Integer> scores = applicantIds.stream().map(id -> {

                    try {
                        return jobPostService.calculateAppFilterScore(id, null, talentFilterDTO);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
                matchScore = scores != null && !scores.isEmpty() ? Collections.max(scores) : 0;
                status = 0;

            }
            talentViewDTO.setRecommended(matchScore >= 95);
            talentViewDTO.setMatchScore(matchScore);
            talentViewDTO.setApplicantStatus(status);
            talentViewDTO.setIsintrested(interestedCount != 0);
            talentViewDTO.setProfileId(consultancyId);
            talentViewDTO.setProfileName(consultancy.getName());
            String imageUrl = consultancy.getProfileImageUrl();
            try {
                talentViewDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                talentViewDTO.setProfilePic(CommonConstants.company_default_image);
            }
            // talentViewDTO.setProfilePic(null);
            talentViewDTO.setJobFunctionName(consultancy.getDomains());
            talentViewDTO.setLocation(consultancy.getLocation());
            talentViewDTO.setCount(applicantsCount);
            talentViewDTOs.add(talentViewDTO);
        }
        talentViewResponse.setTalentViewDTO(talentViewDTOs.stream()
                .sorted(Comparator.comparing(TalentViewDTO::isRecommended)
                        .thenComparing(TalentViewDTO::isIsintrested).thenComparing(TalentViewDTO::getMatchScore).reversed()).collect(Collectors.toList()));
        return talentViewResponse;
    }

//    public TalentViewConsDetailsDTO getApplicantsProfilesForConsultancy(TalentFilterDTO talentFilterDTO, long consultancyId, Integer pageNo, Integer pageSize)
//            throws Exception {
//        String selectQuery = "SELECT distinct a";
//        String countSql = "SELECT COUNT(DISTINCT a.applicantId) ";
//        StringBuilder sqlBuilder = new StringBuilder(
//                " FROM Applicant a " + "LEFT JOIN ApplicantDetails ad ON a.applicantId = ad.applicantId "
//                        + "LEFT JOIN ApplicantJobFunction ajf ON a.applicantId = ajf.applicantId "
//                        + "LEFT JOIN ApplicantSecondaryJobFunction asjf ON a.applicantId = asjf.applicantId "
//                        + "LEFT JOIN ApplicantJobSkill ajs ON a.applicantId = ajs.applicantId "
//                        + "LEFT JOIN EducationHistory eh ON eh.applicantId = a.applicantId " + "WHERE  a.consultancyId= :consultancyId and a.correctionRequired=0 ");
//        Map<String, Object> params = new HashMap<>();
//        params.put("consultancyId", consultancyId);
//        if (talentFilterDTO.getJobFunction() != null) {
//            sqlBuilder.append(" AND (ajf.jobFunctionId = :jobFunction OR asjf.jobFunctionId = :jobFunction)");
//            params.put("jobFunction", talentFilterDTO.getJobFunction());
//        }
//        if (talentFilterDTO.getLocation() != null && !talentFilterDTO.getLocation().equals("")) {
//            sqlBuilder.append(" AND a.location = :location");
//            params.put("location", talentFilterDTO.getLocation());
//        }
//        if (talentFilterDTO.getJobTypes() != null && !talentFilterDTO.getJobTypes().isEmpty()) {
//            sqlBuilder.append(" AND ad.jobType IN (:jobTypes)");
//            params.put("jobTypes", talentFilterDTO.getJobTypes());
//        }
//        if (talentFilterDTO.getWorkMode() != null && !talentFilterDTO.getWorkMode().isEmpty()) {
//            sqlBuilder.append(" AND ad.preferredWorkMode IN (:preferredWorkMode)");
//            params.put("preferredWorkMode", talentFilterDTO.getWorkMode());
//        }
//        if (talentFilterDTO.getExpMin() != null && talentFilterDTO.getExpMax() != null) {
//            sqlBuilder.append(" AND ad.yearsOfExperience >= :yearsOfExpMin AND ad.yearsOfExperience <= :yearsOfExpMax");
//            params.put("yearsOfExpMin", talentFilterDTO.getExpMin());
//            params.put("yearsOfExpMax", talentFilterDTO.getExpMax());
//        }
//        if (talentFilterDTO.getCitizenship() != null && !talentFilterDTO.getCitizenship().isEmpty()) {
//            sqlBuilder.append(" AND ad.citizenship IN (:citizenshipList)");
//            params.put("citizenshipList", talentFilterDTO.getCitizenship());
//        }
//        if (talentFilterDTO.getEduQualification() != null && !talentFilterDTO.getEduQualification().isEmpty()) {
//            sqlBuilder.append(" AND eh.degree IN (:eduQualification)");
//            params.put("eduQualification", talentFilterDTO.getEduQualification());
//        }
//        if (talentFilterDTO.getNoticePeriod() != null && !talentFilterDTO.getNoticePeriod().isEmpty()) {
//            sqlBuilder.append(" AND ad.noticePeriod IN (:noticePeriod)");
//            params.put("noticePeriod", talentFilterDTO.getNoticePeriod());
//        }
//        if (talentFilterDTO.getJobSkills() != null && !talentFilterDTO.getJobSkills().isEmpty()) {
//            sqlBuilder.append(" AND ajs.jobSkillId IN (:jobSkillId)");
//            params.put("jobSkillId", talentFilterDTO.getJobSkills());
//        }
//        sqlBuilder.append(" order by a.consultancyId ");
//        Query countQuery = entityManager.createQuery(countSql + sqlBuilder.toString());
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            countQuery.setParameter(entry.getKey(), entry.getValue());
//        }
//        long totalCount = (long) countQuery.getSingleResult();
//        Query query = entityManager.createQuery(selectQuery + sqlBuilder.toString()).setFirstResult(pageNo * pageSize)
//                .setMaxResults(pageSize);
//        for (Map.Entry<String, Object> entry : params.entrySet()) {
//            query.setParameter(entry.getKey(), entry.getValue());
//        }
//        List<Applicant> results = query.getResultList();
//        TalentViewConsDetailsDTO talentViewResponse = genrateTalentViewAppDetailsDTO(results, consultancyId);
//        if (talentViewResponse != null) {
//            talentViewResponse.setTotalCount(totalCount);
//            talentViewResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
//        }
//        return talentViewResponse;
//    }
//
//    private TalentViewConsDetailsDTO genrateTalentViewAppDetailsDTO(List<Applicant> applicants, long consultancyId) throws Exception {
//
//        TalentViewConsDetailsDTO talentViewConsDetailsDTO = new TalentViewConsDetailsDTO();
//        if (applicants.isEmpty()) {
//            return null;
//        }
//        List<AppledProfilesDTO> appledProfilesDTOs = new ArrayList<>();
//        AppledProfilesDTO appledProfilesDTO;
//
//        List<Long> applicantIds = applicants.stream().map(Applicant::getApplicantId).collect(Collectors.toList()) ;
////        List<ApplicantRecommendedResultSet> applicantsWithMatchScoreAndStatus = jobMatchConsultancyRepository.getApplicantsMatchScore(applicantIds)
////                .stream().sorted(Comparator.comparing(ApplicantRecommendedResultSet::getApplicantStatus).thenComparing(ApplicantRecommendedResultSet::getMatchScore).reversed()).collect(Collectors.toList());
////
////        Map<Long, Map<Long, Integer>> applicantJobMap = applicantsWithMatchScoreAndStatus.stream()
////                .collect(Collectors.groupingBy(ApplicantRecommendedResultSet::getId, LinkedHashMap::new,
////                        Collectors.toMap(ApplicantRecommendedResultSet::getJobPostId, ApplicantRecommendedResultSet::getMatchScore, (score1, score2) -> score1, LinkedHashMap::new)));
//
//        List<ApplicantDetails> applicatDetailsList = (List<ApplicantDetails>) applicantDetailsRepository
//                .findByUsersDetails(applicantIds);
//        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
//                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
//        for (Applicant applicant : applicants) {
//            appledProfilesDTO = new AppledProfilesDTO();
//            appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
//            ApplicantDetails applicantDetails = applicantDetailsMap.get(applicant.getApplicantId());
//            WorkExperience workExperiences = workExperienceRepository
//                    .findByWorkExperienceIdAndApplicantId(applicant.getApplicantId());
//            appledProfilesDTO.setApplicantTitle(workExperiences == null ? "Not Found"
//                    : workExperiences.getJobTitle() + " : " + workExperiences.getCompanyName());
//            if (applicantDetails != null) {
//                appledProfilesDTO.setExperience(String.valueOf(applicantDetails.getYearsOfExperience()));
//                appledProfilesDTO.setJobFunc(applicantDetails.getJobFunction());
//                appledProfilesDTO.setSecondaryJobFunc(applicantDetails.getSecondaryJobFunction());
//            }
//            appledProfilesDTO.setLocation(applicant.getLocation());
//            String imageUrl = applicant.getProfileImageUrl();
//            appledProfilesDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : null);
//            appledProfilesDTO.setApplicantId(applicant.getApplicantId());
//            List<ApplicantRecommendedResultSet> applicantsWithMatchScore = jobMatchConsultancyRepository.getApplicantMatchScore(applicant.getApplicantId())
//                    .stream().sorted(Comparator.comparing(ApplicantRecommendedResultSet::getApplicantStatus).thenComparing(ApplicantRecommendedResultSet::getMatchScore).reversed()).collect(Collectors.toList());
////                Map<Long, Map<Long, Integer>> applicantJobMap = applicantsWithMatchScore.stream()
////                        .collect(Collectors.groupingBy(ApplicantRecommendedResultSet::getId, LinkedHashMap::new,
////                                Collectors.toMap(ApplicantRecommendedResultSet::getJobPostId, ApplicantRecommendedResultSet::getMatchScore, (score1, score2) -> score1, LinkedHashMap::new)));
//            if(applicantsWithMatchScore != null && !applicantsWithMatchScore.isEmpty()){
//                if(applicantsWithMatchScore.get(0).getMatchScore()>=95){
//                    appledProfilesDTO.setRecommended(true);
//                }else {
//                    appledProfilesDTO.setRecommended(false);
//                }
//                appledProfilesDTO.setMatchScore(applicantsWithMatchScore.get(0).getMatchScore());
//            }
//            appledProfilesDTOs.add(appledProfilesDTO);
//        }
//        talentViewConsDetailsDTO.setAppliedProfiles(appledProfilesDTOs.stream().sorted(Comparator.comparing(AppledProfilesDTO::isRecommended)
//                .thenComparing(AppledProfilesDTO::getMatchScore).reversed()).collect(Collectors.toList()));
//        Optional<Consultancy> c = consultancyRepository.findById(consultancyId);
//        if (!c.isPresent()) {
//            throw new Exception("Consultancy not found");
//        }
//        Consultancy consultancy = c.get();
//        talentViewConsDetailsDTO.setConsultancyId(consultancy.getConsultancyId());
//        talentViewConsDetailsDTO.setConsultancyName(consultancy.getName());
//        talentViewConsDetailsDTO.setFoundedIn(consultancy.getFoundedDate());
//        talentViewConsDetailsDTO.setAbout(consultancy.getAbout());
//        talentViewConsDetailsDTO.setDomainSpec(consultancy.getDomains());
//        talentViewConsDetailsDTO.setIndustryType(consultancy.getIndustryTypes());
//        talentViewConsDetailsDTO.setLocation(consultancy.getLocation());
//
//
//        String imageUrl = consultancy.getProfileImageUrl();
//        try {
//            talentViewConsDetailsDTO.setProfilePicUrl(imageService.getImage(imageUrl));
//        } catch (IOException e) {
//            talentViewConsDetailsDTO.setProfilePicUrl(null);
//        }
//
//        talentViewConsDetailsDTO.setWebsite(consultancy.getWebsite());
//
//        talentViewConsDetailsDTO.setSize(consultancy.getNumberOfEmployees().toString());
//        if (consultancy.getClients() != null)
//            talentViewConsDetailsDTO.setClients(consultancy.getClients().stream()
//                    .map(cl -> modelMapper.map(cl, ClientDTO.class)).collect(Collectors.toSet()));
//        talentViewConsDetailsDTO.setOverallTalent(consultancy.getOverallTalentPool());
//        talentViewConsDetailsDTO.setFbLink(consultancy.getFacebookLink());
//        talentViewConsDetailsDTO.setTwLink(consultancy.getTwitterLink());
//        talentViewConsDetailsDTO.setLiLink(consultancy.getLinkedinLink());
////        long intrestedCount = jobMatchConsultancyRepository.getProfilesByInterviewStatus(jobPostId,
////                consultancy.getConsultancyId(), 1);
////        talentViewConsDetailsDTO.setIntrestedCount(intrestedCount);
//        return talentViewConsDetailsDTO;
//    }

    public TalentViewConsDetailsDTO getApplicantsProfilesForConsultancy(TalentFilterDTO talentFilterDTO, long consultancyId, Integer pageNo, Integer pageSize)
            throws Exception {

        String selectQuery = "SELECT DISTINCT a.applicant_id,MAX(subquery.match_score) AS maxMatchScore,MAX(subquery.applicant_job_status) ";
        String countSql = "SELECT COUNT(DISTINCT a.applicant_id) ";
        String allApplicantQuery = "SELECT DISTINCT a.applicant_id ";
        StringBuilder sqlBuilder = new StringBuilder(
                " from applicant a\n" +
                        "left join applicant_details ad on ad.applicant_id=a.applicant_id\n" +
//                        "left join applicant_job_function ajf on ajf.applicant_id=a.applicant_id\n" +
//                        "left join applicant_secondary_job_function asjf on asjf.applicant_id=a.applicant_id\n" +
//                        "left join education_history eh on eh.applicant_id=a.applicant_id\n" +
//                        "left join applicant_job_skill ajs on ajs.applicant_id=a.applicant_id\n" +
                        "left JOIN (\n" +
                        "    SELECT jc.applicant_id, jc.applicant_job_status, jc.match_score,jc.saved_recruiter\n" +
                        "    FROM job_match_consultancy jc where jc.job_post_id=:jobPostId and jc.applicant_status=0 and jc.applicant_job_status in(0,1)  and jc.job_post_id not in (select a.job_post_id from consultancy_job_status a where a.status =2 and jc.consultancy_id=a.consultancy_id) \n" +
                        "    ORDER BY jc.applicant_job_status DESC, jc.match_score DESC\n" +
                        ") AS subquery ON subquery.applicant_id = a.applicant_id where a.consultancy_id=:consultancyId and  a.correction_required=0 AND NOT EXISTS (" +
                        " SELECT 1 " +
                        "   FROM job_match_consultancy jmc" +
                        "   WHERE jmc.applicant_id = a.applicant_id" +
                        "   AND jmc.job_post_id = :jobPostId" +
                        "  AND jmc.applicant_status > 0 " +
                        " )" +
                        " and subquery.saved_recruiter = 0");
        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);
        params.put("jobPostId", talentFilterDTO.getJobId());
        if (talentFilterDTO.getJobFunction() != null) {
            sqlBuilder.append(" AND (ad.job_function_id = :jobFunction OR FIND_IN_SET(:jobFunction, ad.secondary_job_function_id) > 0) ");
            params.put("jobFunction", talentFilterDTO.getJobFunction());
        }
        if (talentFilterDTO.getLocation() != null && !talentFilterDTO.getLocation().equals("")) {
            sqlBuilder.append(" AND a.location = :location");
            params.put("location", talentFilterDTO.getLocation());
        }
        if (talentFilterDTO.getJobTypes() != null && !talentFilterDTO.getJobTypes().isEmpty()) {
            sqlBuilder.append(" AND ad.job_type IN (:jobTypes)");
            params.put("jobTypes", talentFilterDTO.getJobTypes());
        }
        if (talentFilterDTO.getWorkMode() != null && !talentFilterDTO.getWorkMode().isEmpty()) {
            sqlBuilder.append(" AND ad.preferred_work_mode IN (:preferredWorkMode)");
            params.put("preferredWorkMode", talentFilterDTO.getWorkMode());
        }
        if (talentFilterDTO.getExpMin() != null && talentFilterDTO.getExpMax() != null) {
            sqlBuilder.append(" AND ad.years_of_exp >= :yearsOfExpMin AND ad.years_of_exp <= :yearsOfExpMax");
            params.put("yearsOfExpMin", talentFilterDTO.getExpMin());
            params.put("yearsOfExpMax", talentFilterDTO.getExpMax());
        }

        if (talentFilterDTO.getCitizenship() != null && !talentFilterDTO.getCitizenship().isEmpty()) {
            sqlBuilder.append(" AND ad.citizenship IN (:citizenshipList)");
            params.put("citizenshipList", talentFilterDTO.getCitizenship());
        }
        if (talentFilterDTO.getEduQualification() != null && !talentFilterDTO.getEduQualification().isEmpty()) {
            sqlBuilder.append(" AND ad.degree_id IN (:eduQualification) ");
            params.put("eduQualification", talentFilterDTO.getEduQualification());
        }
        if (talentFilterDTO.getNoticePeriod() != null && !talentFilterDTO.getNoticePeriod().isEmpty()) {
            sqlBuilder.append(" AND ad.notice_period IN (:noticePeriod)");
            params.put("noticePeriod", talentFilterDTO.getNoticePeriod());
        }
        if (talentFilterDTO.getJobSkills() != null && !talentFilterDTO.getJobSkills().isEmpty()) {
//            sqlBuilder.append(" AND ajs.job_skill_id IN (:jobSkillId)");
//            params.put("jobSkillId", talentFilterDTO.getJobSkills());
            sqlBuilder.append(" AND ( ");
            for (Integer skillId : talentFilterDTO.getJobSkills()) {
                sqlBuilder.append("FIND_IN_SET('").append(skillId).append("', ad.skill_id) > 0");
                if (skillId != talentFilterDTO.getJobSkills().get(talentFilterDTO.getJobSkills().size() - 1)) {
                    sqlBuilder.append(" OR ");
                }
            }
            sqlBuilder.append(") ");
        }
        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();
        sqlBuilder.append(" group by a.applicant_id ORDER BY MAX(subquery.applicant_job_status) DESC, MAX(subquery.match_score) DESC,a.applicant_id  DESC ");
        Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder).setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> results = query.getResultList();
        Query applicantsQuery = entityManager.createNativeQuery(allApplicantQuery + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            applicantsQuery.setParameter(entry.getKey(), entry.getValue());
        }
        List<Long> applicanIds = applicantsQuery.getResultList();
        TalentViewConsDetailsDTO talentViewResponse = genrateTalentViewAppDetailsDTO(results, consultancyId, talentFilterDTO, applicanIds);
        if (talentViewResponse != null) {
            talentViewResponse.setTotalCount(totalCount);
            talentViewResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        }
        return talentViewResponse;
    }

    private TalentViewConsDetailsDTO genrateTalentViewAppDetailsDTO(List<Object[]> results, long consultancyId, TalentFilterDTO talentFilterDTO, List<Long> allApplicanIds) throws Exception {

        TalentViewConsDetailsDTO talentViewConsDetailsDTO = new TalentViewConsDetailsDTO();
//        if (results.isEmpty()) {
//            return null;
//        }
        List<AppledProfilesDTO> appledProfilesDTOs = new ArrayList<>();
        AppledProfilesDTO appledProfilesDTO;

//        List<Long> applicantIds = applicants.stream().map(Applicant::getApplicantId).collect(Collectors.toList()) ;
        List<Long> applicantIds = new ArrayList<>();
        Map<Long, Integer> applicantsMatchScore = new LinkedHashMap<>();
        Map<Long, Integer> applicantsAppStatus = new LinkedHashMap<>();
//        for (Object[] row : results) {
//            Long applicantId = ((BigInteger) row[0]).longValue();
//            applicantsMatchScore.put(applicantId, row[1] != null ? ((BigInteger) row[1]).intValue() : null);
//            applicantsAppStatus.put(applicantId, row[2] != null ? ((BigInteger) row[2]).intValue() : null);
//            applicantIds.add(applicantId);
//        }

        for (Object[] row : results) {
            Long applicantId = ((BigInteger) row[0]).longValue();
            applicantsMatchScore.put(applicantId, row[1] != null ? ConsultancyJobService.getIntegerValue(row[1]) : null);
            applicantsAppStatus.put(applicantId, row[2] != null ? ConsultancyJobService.getIntegerValue(row[2]) : null);

            applicantIds.add(applicantId);
        }


        List<Applicant> applicants = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository.findByUsersDetails(applicantIds);
        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
        for (Applicant applicant : applicants) {
            appledProfilesDTO = new AppledProfilesDTO();
            appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
            ApplicantDetails applicantDetails = applicantDetailsMap.get(applicant.getApplicantId());
            WorkExperience workExperiences = workExperienceRepository
                    .findByWorkExperienceIdAndApplicantId(applicant.getApplicantId());
            appledProfilesDTO.setApplicantTitle(workExperiences == null ? "Not Found"
                    : workExperiences.getJobTitle() + " : " + workExperiences.getCompanyName());
            if (applicantDetails != null) {
                appledProfilesDTO.setExperience(String.valueOf(applicantDetails.getYearsOfExperience()));
                appledProfilesDTO.setJobFunc(applicantDetails.getJobFunction());
                appledProfilesDTO.setSecondaryJobFunc(applicantDetails.getSecondaryJobFunction());
            }
            appledProfilesDTO.setLocation(applicant.getLocation());
            String imageUrl = applicant.getProfileImageUrl();
            try {
                appledProfilesDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
            } catch (IOException e) {
                appledProfilesDTO.setProfilePic(null);

            }
            Long applicantId = applicant.getApplicantId();
            appledProfilesDTO.setApplicantId(applicantId);

            Integer matchScore = applicantsMatchScore.get(applicantId);
            if (matchScore == null) {
                matchScore = jobPostService.calculateAppFilterScore(applicant.getApplicantId(), null, talentFilterDTO);
            }
            appledProfilesDTO.setRecommended(matchScore >= 95);
            appledProfilesDTO.setMatchScore(matchScore);

            appledProfilesDTO.setApplicantStatus(applicantsAppStatus.get(applicantId) != null ? applicantsAppStatus.get(applicantId) : 0);

            // Integer matchScore = applicantsMatchScore.getOrDefault(applicantId, jobPostService.calculateAppFilterScore(applicant.getApplicantId(), null, talentFilterDTO));
            appledProfilesDTOs.add(appledProfilesDTO);
        }
        talentViewConsDetailsDTO.setAppliedProfiles(appledProfilesDTOs.stream().sorted(Comparator.comparing(AppledProfilesDTO::getApplicantStatus)
                .thenComparing(AppledProfilesDTO::isRecommended).thenComparing(AppledProfilesDTO::getMatchScore).reversed()).collect(Collectors.toList()));

        Consultancy consultancy = consultancyRepository.findById(consultancyId).orElseThrow(() -> new Exception("Consultancy not found"));
        talentViewConsDetailsDTO.setConsultancyId(consultancy.getConsultancyId());
        talentViewConsDetailsDTO.setConsultancyName(consultancy.getName());
        talentViewConsDetailsDTO.setFoundedIn(consultancy.getFoundedDate());
        talentViewConsDetailsDTO.setAbout(consultancy.getAbout());
        talentViewConsDetailsDTO.setDomainSpec(consultancy.getDomains());
        talentViewConsDetailsDTO.setIndustryType(consultancy.getIndustryTypes());
        talentViewConsDetailsDTO.setLocation(consultancy.getLocation());

        String imageUrl = consultancy.getProfileImageUrl();
        try {
            talentViewConsDetailsDTO.setProfilePicUrl(imageUrl != null && !imageUrl.isEmpty() ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
        } catch (IOException e) {
            talentViewConsDetailsDTO.setProfilePicUrl(null);
        }

        talentViewConsDetailsDTO.setWebsite(consultancy.getWebsite());
        talentViewConsDetailsDTO.setSize(consultancy.getNumberOfEmployees());
//        if (consultancy.getClients() != null)
//            talentViewConsDetailsDTO.setClients(consultancy.getClients().stream()
//                    .map(cl -> modelMapper.map(cl, ClientDTO.class)).collect(Collectors.toSet()));
        List<Long> idList = new ArrayList<>();
        idList.add(consultancy.getConsultancyId());
        List<User> usersList = userRepository.findByConsultancyUsersIds(idList);

        CompanyDetailsDTO companyDetailsDTO = companyService.getCompany(usersList.get(0).getCompanyId());
        if (companyDetailsDTO.getClients() != null)
            talentViewConsDetailsDTO.setClients(companyDetailsDTO.getClients());
        if (companyDetailsDTO.getDomains() != null)
            talentViewConsDetailsDTO.setDomainSpec(companyDetailsDTO.getDomains().stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));
        talentViewConsDetailsDTO.setOverallTalent(consultancy.getOverallTalentPool());
        talentViewConsDetailsDTO.setFbLink(consultancy.getFacebookLink());
        talentViewConsDetailsDTO.setTwLink(consultancy.getTwitterLink());
        talentViewConsDetailsDTO.setLiLink(consultancy.getLinkedinLink());
        long interestedCount = jobMatchConsultancyRepository.interestedCountForConsultancy(consultancyId, allApplicanIds, talentFilterDTO.getJobId());

        talentViewConsDetailsDTO.setIntrestedCount(interestedCount);
        talentViewConsDetailsDTO.setIntrested(interestedCount != 0);
        return talentViewConsDetailsDTO;
    }
}
