/**
 *
 */
package com.workruit.us.application.services;

import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.ApplJobState;
import com.workruit.us.application.enums.Citizenship;
import com.workruit.us.application.enums.JobType;
import com.workruit.us.application.enums.NoticePeriod;
import com.workruit.us.application.models.*;
import com.workruit.us.application.notification.NotificationException;
import com.workruit.us.application.notification.NotificationScheduler;
import com.workruit.us.application.repositories.*;
import lombok.extern.slf4j.Slf4j;
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
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mahesh
 */
@Slf4j
@Service
public class ConsultancyJobService {

    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired JobMatchingRepository jobMatchingRepository;
    private @Autowired CompanyRepository companyRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired JobQuestionAnswersRepository jobQuestionAnswersRepository;
    private @Autowired NotificationScheduler notificationScheduler;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired ApplicantService applicantService;
    private @Autowired ImageService imageService;
    private @Autowired EducationHistoryRepository educationHistoryRepository;
    private @Autowired JobPostService jobPostService;
    private @Autowired ApplicantSecondaryJobFunctionRepository applicantSecondaryJobFunctionRepository;
    private @Autowired ApplicantJobFunctionRepository applicantJobFunctionRepository;
    private @Autowired DegreesRepository degreesRepository;
    private @Autowired ApplicantJobSkillRepository applicantJobSkillRepository;


    @Autowired
    private ConsultancyJobStatusRepository consultancyJobStatusRepository;

    @Autowired
    private AlertService alertService;

    @PersistenceContext
    private EntityManager entityManager;

    public static Integer getIntegerValue(Object obj) {
        if (obj instanceof BigInteger) {
            return ((BigInteger) obj).intValue();
        } else if (obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return null;
        }
    }

    public List<Long> getUserIds(Long consultancyId, Long userId, String role, int filter) {
        List<Long> userIds = new ArrayList<>();
        if (role.equals("CONSULTANCY_ADMIN")) {
            if (filter == 2 || filter == 1) {
                userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId, null);
            }
            if (filter == 0 || filter == 2) {
                userIds.add(userId);
            }
        } else {
            userIds.add(userId);
        }

        return userIds;
    }

//    public ConsJobApplicantResponse getMatchedApplicantsForJobByConsultancy(long consultancyId, long userId, long jobPostId, String role,
//                                                                            Integer pageNo, Integer pageSize) throws Exception {
//        Pageable pageable = PageRequest.of(pageNo, pageSize);
//        // Get applicants matched from job match consultancy
//        ConsJobApplicantResponse response = new ConsJobApplicantResponse();
//        //  List<Long> userIds = getUserIds(consultancyId, userId, role, 2);
//        try {
//            Page<JobMatchConsultancy> jobMatchConsList = jobMatchConsultancyRepository
//                    .findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(jobPostId, consultancyId, pageable);
//            List<Long> applicantList = jobMatchConsList.stream().map(JobMatchConsultancy::getApplicantId)
//                    .collect(Collectors.toList());
//            List<ConsJobApplicantDTO> appledProfilesDTOs = new ArrayList<>();
//            ConsJobApplicantDTO appledProfilesDTO;
//            List<Applicant> applicants = (List<Applicant>) applicantRepository.findAllById(applicantList);
//            List<ApplicantDetails> applicantDetailsList = (List<ApplicantDetails>) applicantDetailsRepository.findByUsersDetails(applicantList);
//            Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsList.stream()
//                    .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
//            for (Applicant applicant : applicants) {
//                appledProfilesDTO = new ConsJobApplicantDTO();
//                appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
//                appledProfilesDTO.setApplicantId(applicant.getApplicantId());
//                JobMatchConsultancy jobMatchConsultancy = jobMatchConsList.getContent().stream()
//                        .filter(jb -> applicant.getApplicantId() == jb.getApplicantId())
//                        .findAny()
//                        .orElse(null);
//                appledProfilesDTO.setStatus(jobMatchConsultancy != null && jobMatchConsultancy.getApplicantStatus() == 1 ? 1 : 0);
//                appledProfilesDTO.setRecommended(jobMatchConsultancy.getMatchScore() > 95 ? true : false);
//                ApplicantDetails applicantDetails = applicantDetailsMap.get(applicant.getApplicantId());
//                if (applicantDetails != null) {
//                    appledProfilesDTO.setJobFunction(applicantDetails.getJobFunction());
//                    appledProfilesDTO.setExperience(applicantDetails.getYearsOfExperience());
//                    appledProfilesDTO.setJobType(applicantDetails.getJobType());
//                }
//                List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
//                appledProfilesDTO.setQualification(educationHistories != null && educationHistories.size() > 0 ? educationHistories.get(0).getDegree() : "--");
//                appledProfilesDTO.setLocation(applicant.getLocation());
//                appledProfilesDTO.setPercentage(jobMatchConsultancy.getMatchScore());
//
//                //appledProfilesDTO.setPercentage(applicantService.profileCompletionStatus(applicant.getApplicantId()));
//                appledProfilesDTOs.add(appledProfilesDTO);
//            }
//            appledProfilesDTOs.sort(Comparator.comparing(ConsJobApplicantDTO::getStatus).thenComparing(ConsJobApplicantDTO::getPercentage).reversed());
//
//            response.setConsJobApplicantDTO(appledProfilesDTOs);
//            response.setTotalCount(jobMatchConsList.getTotalElements());
//            response.setTotalPages(jobMatchConsList.getTotalPages());
//        } catch (Exception e) {
//            throw new Exception(e.getMessage());
//        }
//        return response;
//    }

    public ConsJobViewResponse getMatchedJobsForConsultancy(long consultancyId, long userId, String role, Integer pageNo, Integer pageSize) {

        List<Long> userIds = getUserIds(consultancyId, userId, role, 2);

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        // 1. get users from job matches which no action taken
        ConsJobViewResponse response = new ConsJobViewResponse();

        try {
            //Page<Long> jobIdsList = jobMatchingRepository.findJobIdsByConsultancyIdAndConsultancyUserIdforJobs(consultancyId, userIds, pageable);
            //  Page<JobMatchConsultancy> jobMatchList = jobMatchingRepository.findJobMatchesByConsultancyIdAndConsultancyUserId(jobIdsList.getContent(), pageable);

            Page<JobMatchConsultancy> jobMatchList = jobMatchingRepository.findJobIdsByConsultancyIdAndConsultancyUserIdforJobs(consultancyId, userIds, userId, pageable);

            response = generateConsJobViewResponseforJobs(jobMatchList, userIds, consultancyId, pageable);
        } catch (Exception e) {
            log.error("Error while fetching matched jobs", e);
        }
        return response;
    }

    public ConsJobApplicantResponse getMatchedApplicantsForJobByConsultancy(long consultancyId, long userId, long jobPostId, String role,
                                                                            Integer pageNo, Integer pageSize) throws Exception {
        Sort sort = Sort.by(Sort.Order.desc("applicantStatus"), Sort.Order.desc("matchScore"));
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        // Get applicants matched from job match consultancy
        ConsJobApplicantResponse response = new ConsJobApplicantResponse();
        List<Long> userIds = getUserIds(consultancyId, userId, role, 2);
        try {
            Page<ConsApplicantStatusResultSet> jobMatchConsList = jobMatchConsultancyRepository
                    .findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(jobPostId, consultancyId, userIds, pageable);
            Map<Long, Long> applicantStatusMap = jobMatchConsList.stream().collect(Collectors.toMap(ConsApplicantStatusResultSet::getApplicantId, ConsApplicantStatusResultSet::getApplicantStatus));
            Map<Long, Integer> applicantScoreMap = jobMatchConsList.stream().collect(Collectors.toMap(ConsApplicantStatusResultSet::getApplicantId, ConsApplicantStatusResultSet::getMatchScore));
            List<Long> applicantList = jobMatchConsList.stream().map(ConsApplicantStatusResultSet::getApplicantId)
                    .collect(Collectors.toList());
            List<ConsJobApplicantDTO> appledProfilesDTOs = new ArrayList<>();
            ConsJobApplicantDTO appledProfilesDTO;
            List<Applicant> applicants = (List<Applicant>) applicantRepository.findAllById(applicantList);
            List<ApplicantDetails> applicantDetailsList = applicantDetailsRepository.findByUsersDetails(applicantList);
            Map<Long, ApplicantDetails> applicantDetailsMap = applicantDetailsList.stream()
                    .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));
            for (Applicant applicant : applicants) {
                appledProfilesDTO = new ConsJobApplicantDTO();
                appledProfilesDTO.setApplicantName(applicant.getFirstName() + " " + applicant.getLastName());
                String imageUrl = applicant.getProfileImageUrl();
                try {
                    appledProfilesDTO.setProfilePic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
                } catch (IOException e) {
                    appledProfilesDTO.setProfilePic(null);
                    e.printStackTrace();
                }

                appledProfilesDTO.setApplicantId(applicant.getApplicantId());
                appledProfilesDTO.setStatus(applicantStatusMap.get(applicant.getApplicantId()) != null && applicantStatusMap.get(applicant.getApplicantId()) == 1 ? 1 : 0);
                ApplicantDetails applicantDetails = applicantDetailsMap.get(applicant.getApplicantId());
                if (applicantDetails != null) {
                    appledProfilesDTO.setJobFunction(applicantDetails.getJobFunction());
                    appledProfilesDTO.setExperience(applicantDetails.getYearsOfExperience());
                    appledProfilesDTO.setJobType(applicantDetails.getJobType());
                }
                List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
                appledProfilesDTO.setQualification(educationHistories != null && educationHistories.size() > 0 ? educationHistories.get(0).getDegree() : "--");
                appledProfilesDTO.setLocation(applicant.getLocation());
                appledProfilesDTO.setRecommended(applicantScoreMap.get(applicant.getApplicantId()) >= 95);
                appledProfilesDTO.setPercentage(applicantScoreMap.get(applicant.getApplicantId()));
                //appledProfilesDTO.setPercentage(applicantService.profileCompletionStatus(applicant.getApplicantId()));
                appledProfilesDTOs.add(appledProfilesDTO);
            }
            appledProfilesDTOs.sort(Comparator.comparing(ConsJobApplicantDTO::getStatus).thenComparing(ConsJobApplicantDTO::getPercentage).reversed());
            response.setConsJobApplicantDTO(appledProfilesDTOs);
            response.setTotalCount(jobMatchConsList.getTotalElements());
            response.setTotalPages(jobMatchConsList.getTotalPages());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return response;
    }

    public void bulkApplyApplicantsForJob(long consultancyId, long jobPostId, long conUserId,
                                          List<ConsBulkApplyDTO> consBulkApply) throws Exception {
        List<JobQuestionAnswers> jobQuestionData = new ArrayList<>();
        List<Long> applicantsIds = new ArrayList<>();
        // For each applicant
        for (ConsBulkApplyDTO consBulkApplyDTO : consBulkApply) {
            List<QuestionAnswerDTO> questionAnswersDto = consBulkApplyDTO.getQuestionAnswerDTO();
            applicantsIds.add(consBulkApplyDTO.getApplicantId());
            // Fetch answers for questions
            for (QuestionAnswerDTO questionAnswersDTO : questionAnswersDto) {
                JobQuestionAnswers jobAnsData = new JobQuestionAnswers();
                jobAnsData.setApplicantId(consBulkApplyDTO.getApplicantId());
                jobAnsData.setConsultancyId(consBulkApplyDTO.getConsultancyId());
                jobAnsData.setCreatedDate(new Date());
                jobAnsData.setJobPostId(consBulkApplyDTO.getJobPostId());
                // Holds both string value and radio button or multiple choice value as string
                if (questionAnswersDTO.getQuestionAnswer() != null
                        && questionAnswersDTO.getQuestionAnswer().trim().length() > 0)
                    jobAnsData.setQuestionAnsValue(questionAnswersDTO.getQuestionAnswer());
                else {
                    jobAnsData.setQuestionAnsValue(String.valueOf(questionAnswersDTO.getQuestionValueId()));
                }
                jobAnsData.setQuestionId(questionAnswersDTO.getQuestionId());
                jobAnsData.setUpdatedDate(new Date());
                jobQuestionData.add(jobAnsData);
            }
        }
        jobQuestionAnswersRepository.saveAll(jobQuestionData);

        // TODO: Update job match table
        List<JobMatchConsultancy> jobMatchData = jobMatchConsultancyRepository.getProfilesByIds(jobPostId,
                applicantsIds);
        List<JobMatchConsultancy> jobMatchDataUpdated = new ArrayList<>();
        int matchdCount = 0;
        for (JobMatchConsultancy jobMatchConsultancy : jobMatchData) {
            jobMatchConsultancy.setApplicantJobStatus(ApplJobState.APPLIED.getValue());
            jobMatchConsultancy.setApplicantUpdatedDate(new Date());
            jobMatchConsultancy.setUpdatedByConsId(consultancyId);
            jobMatchConsultancy.setSavedByRecruiter(false);
            jobMatchConsultancy.setUpdatedByConsUserId(conUserId);
            jobMatchConsultancy.setLastactionPerformedConsultantUserId(conUserId);
            jobMatchConsultancy.setInterviewStatus(1);
            jobMatchConsultancy.setUpdatedDate(new Date());
            if (jobMatchConsultancy.getApplicantStatus() == 1) {
                matchdCount++;
            }
            jobMatchDataUpdated.add(jobMatchConsultancy);
        }
        jobMatchConsultancyRepository.saveAll(jobMatchDataUpdated);
        Optional<ConsultancyJobStatus> consultancyJobStatus = consultancyJobStatusRepository.findByJobPostIdAndConsultancyUserId(jobPostId, conUserId);
        if (consultancyJobStatus.isPresent()) {
            consultancyJobStatusRepository.deleteById(consultancyJobStatus.get().getConsultancyJobStatusId());
        }
        User user = userRepository.findById(conUserId).get();
        JobPost jobPost = jobPostRepository.findById(jobPostId).get();
        User recruiterUser = userRepository.findById(jobPost.getUserId()).get();
        Company company = companyRepository.findById(recruiterUser.getCompanyId()).get();

        String message = user.getFirstName() + " " + user.getLastName() + " has applied for "
                + jobPost.getTitle() + " job and shared " + consBulkApply.size() + " profiles.";
        alertService.saveAlertInfo(conUserId, message, consultancyId);

        if (matchdCount > 0) {
            message = user.getFirstName() + " " + user.getLastName() + " has " + matchdCount + " profile matched with " + company.getName() + " for " + jobPost.getTitle() + " job.";
            alertService.saveAlertInfo(conUserId, message, consultancyId);

            if (user.getNotificationToken() != null) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.HOUR, 1); // set the reminder time to 1 hour from now
                Date reminderTime1 = calendar1.getTime();
                String notificationMessage = user.getFirstName() + "" + user.getLastName() + " has matched with " + company.getName() + " for " + jobPost.getTitle() + " job " + matchdCount + " profiles matched.";
                try {
                    notificationScheduler.scheduleNotification("New Match.", notificationMessage, user.getNotificationToken(), reminderTime1, user.getUserId(), user.getConsultancyId());
                } catch (SchedulerException e) {
                    throw new NotificationException("Schedule notification failed");
                }
            }

        }

    }

//    private List<Long> getUserIdByFilter(int filter, long consultancyId, long userId) {
//        List<Long> userIds = new ArrayList<Long>();
//        if (filter == 2 || filter == 1) {
//            userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId, null);
//        }
//        if (filter == 0 || filter == 2) {
//            userIds.add(userId);
//        }
//        return userIds;
//    }

    @Transactional
    public void updatedConsultancyJobStatus(long userId, Long consultancyId, long jobId, int status) {
        ConsultancyJobStatus consultancyJobStatus = null;
        Optional<ConsultancyJobStatus> optionalConsultancyJobStatus = consultancyJobStatusRepository.findByJobPostIdAndConsultancyUserId(jobId, userId);
        if (optionalConsultancyJobStatus.isPresent()) {
            consultancyJobStatus = optionalConsultancyJobStatus.get();
            consultancyJobStatus.setStatus(status);
            consultancyJobStatus.setUpdatedDate(new Date());
        } else {
            consultancyJobStatus = new ConsultancyJobStatus();
            consultancyJobStatus.setConsultancyId(consultancyId);
            consultancyJobStatus.setConsultancyUserId(userId);
            consultancyJobStatus.setJobPostId(jobId);
            consultancyJobStatus.setStatus(status);
            consultancyJobStatus.setCreatedDate(new Date());
            consultancyJobStatus.setUpdatedDate(new Date());
        }
        consultancyJobStatusRepository.save(consultancyJobStatus);
    }


//    public ConsJobViewResponse filterMatchedJobs(MatchedJobsFilter matchedJobsFilter, List<ConsJobApplicantDTO> applicants, Integer pageNo, Integer pageSize) {
//
//        ConsJobViewResponse response = new ConsJobViewResponse();
//        try {
//            List<ApplicantRecommendedResultSet> recommendedResultSets = jobMatchConsultancyRepository.getApplicantsMatchScore(applicants.stream().map(ConsJobApplicantDTO::getApplicantId).collect(Collectors.toList()));
//            Set<Long> jobsIdList = recommendedResultSets.stream().map(ApplicantRecommendedResultSet::getJobPostId).collect(Collectors.toSet());
//            String selectQuery = "SELECT distinct(jp)";
//            String countSql = "SELECT COUNT(distinct jp.jobPostId)  ";
//            StringBuilder sqlBuilder = new StringBuilder(" FROM JobPost jp " +
//                    "LEFT JOIN jp.jobFunction jf  " +
//                    "LEFT JOIN jp.jobDegrees jd " +
//                    "LEFT JOIN jp.jobSkills js " +
//                    "WHERE  jp.status='ACTIVE' ");
//
//            Map<String, Object> params = new HashMap<>();
//
//            if(jobsIdList != null && ! jobsIdList.isEmpty()){
//                sqlBuilder.append(" AND jp.jobPostId in (:jobIdList) ");
//                params.put("jobIdList", jobsIdList);
//            }
//
//            if (matchedJobsFilter.getJobFunction() != null && matchedJobsFilter.getJobFunction() != 0) {
//                sqlBuilder.append(" AND jf.jobFunctionId = :jobFunction ");
//                params.put("jobFunction", matchedJobsFilter.getJobFunction());
//            }
//
//            if (matchedJobsFilter.getLocations() != null && !matchedJobsFilter.getLocations().isEmpty()) {
//                sqlBuilder.append(" AND jp.location IN (:location) ");
//                params.put("location", matchedJobsFilter.getLocations());
//            }
//
//            if (matchedJobsFilter.getJobTypes() != null && !matchedJobsFilter.getJobTypes().isEmpty()) {
//                sqlBuilder.append(" AND jp.jobType IN (:jobTypes)");
//                params.put("jobTypes", matchedJobsFilter.getJobTypes());
//            }
//
//            if (matchedJobsFilter.getWorklocValue() != null && !matchedJobsFilter.getWorklocValue().isEmpty()) {
//                sqlBuilder.append(" AND jp.worklocType IN (:worklocValue) ");
//                params.put("worklocValue", matchedJobsFilter.getWorklocValue());
//            }
//
//            if (matchedJobsFilter.getCitizenship() != null && !matchedJobsFilter.getCitizenship().isEmpty()) {
//                sqlBuilder.append(" AND jp.citizenship IN (:citizenship) ");
//                params.put("citizenship", matchedJobsFilter.getCitizenship());
//            }
//
//            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
//                sqlBuilder.append(" AND js.skillId IN (:jobSkills) ");
//                params.put("jobSkills", matchedJobsFilter.getJobSkills());
//            }
//
//            if (matchedJobsFilter.getEduQualification() != null && !matchedJobsFilter.getEduQualification().isEmpty()) {
//                sqlBuilder.append(" AND jd.degreeId IN (:eduQualification)");
//                params.put("eduQualification", matchedJobsFilter.getEduQualification());
//            }
//
//            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
//                sqlBuilder.append(" AND jp.noticePeriod IN (:noticePeriod) ");
//                params.put("noticePeriod", matchedJobsFilter.getNoticePeriod());
//            }
//
//            Query countQuery = entityManager.createQuery(countSql + sqlBuilder.toString());
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                countQuery.setParameter(entry.getKey(), entry.getValue());
//            }
//            long totalCount = (long) countQuery.getSingleResult();
//
//            Pageable pageable = PageRequest.of(pageNo, pageSize);
//            Query query = entityManager.createQuery(selectQuery + sqlBuilder.toString());
//            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
//            query.setMaxResults(pageable.getPageSize());
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                query.setParameter(entry.getKey(), entry.getValue());
//            }
//
//
//            List<JobPost> jobMatches = query.getResultList();
//            response = generateConsJobViewResponseWithFilter(jobMatches, totalCount, applicants, pageSize);
//        } catch (Exception e) {
//            log.error("Error while fetching matched jobs", e);
//        }
//        return response;
//    }

    public ConsJobViewResponse getSavedMatchedJobsForConsultancy(Long consultancyId, long userId, int status, int filter, String role, Integer pageNo, Integer pageSize) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        // 1. get users from job matches which no action taken
        ConsJobViewResponse response = new ConsJobViewResponse();
        //throw new Exception();
        try {
            Page<JobMatchConsultancyResultSet> jobMatchList = null;
            // List<Long> userIds = getUserIds(consultancyId, userId, role, filter);
            List<Long> userIds = new ArrayList<>();
            userIds.add(userId);
            List<Integer> statusList = new ArrayList<>();
            if (status == 0) {
                statusList.add(0);
                statusList.add(1);
                jobMatchList = jobMatchingRepository.findSavedJobMatchesByConsultancyIdAndConsultancyUserIdV1(consultancyId, userIds, statusList, pageable);
            } else if (status == 1) {
                statusList.add(1);
                jobMatchList = jobMatchingRepository.findSavedJobMatchesByConsultancyIdAndConsultancyUserIdV1(consultancyId, userIds, statusList, pageable);
            } else if (status == 2) {
                statusList.add(0);
                jobMatchList = jobMatchingRepository.findSavedJobMatchesByConsultancyIdAndConsultancyUserId(consultancyId, userIds, statusList, pageable);
            }

            response = generateConsJobViewResponse(jobMatchList.map(JobMatchConsultancyResultSet::getJobMatchConsultancy), userIds, consultancyId, pageable);
        } catch (Exception e) {
            log.error("Error while fetching saved matched jobs", e);
        }
        return response;
    }

    public ConsJobViewResponse filterMatchedJobs(MatchedJobsFilter matchedJobsFilter, Long consultancyId, long userId, String role, List<ConsJobApplicantDTO> applicants, Integer pageNo, Integer pageSize) {

        List<Long> userIds = getUserIds(consultancyId, userId, role, 2);
        ConsJobViewResponse response = new ConsJobViewResponse();
        // if (applicants != null && !applicants.isEmpty()) {
        try {
            String selectQuery = "SELECT DISTINCT jp.job_post_id,jp.title,jp.user_id,MAX(subquery.match_score) as maxMatchScore,\n" +
                    " GROUP_CONCAT(DISTINCT f.job_function_name SEPARATOR ', ') AS jobFunction,MAX(subquery.applicant_status) ";
            String countSql = "SELECT COUNT(DISTINCT jp.job_post_id)  ";
            StringBuilder sqlBuilder = new StringBuilder(" FROM job_post jp\n" +
                    "LEFT JOIN (\n" +
                    "  SELECT jpjf.job_post_id\n" +
                    "  FROM jobpost_jobfunctions jpjf\n" +
                    "  left join optional_jobpost_jobfunctions jpsjf on jpsjf.job_post_id=jpjf.job_post_id\n" +
                    "  filterJobFunction \n" +
                    ") AS filtered_jpjf ON filtered_jpjf.job_post_id = jp.job_post_id\n" +
                    "LEFT JOIN jobpost_jobfunctions jpjf ON jpjf.job_post_id = jp.job_post_id\n" +
                    "LEFT JOIN job_function f ON f.job_function_id = jpjf.job_function_id\n" +
                    "LEFT JOIN job_degrees jd ON jd.job_post_id = jp.job_post_id\n" +
                    "LEFT JOIN job_post_skills js ON js.job_post_id = jp.job_post_id\n" +
                    "LEFT JOIN (\n" +
                    "    SELECT jc.job_post_id, jc.applicant_status, jc.match_score, jc.consultancy_id, jc.applicant_job_status\n" +
                    "    FROM job_match_consultancy jc\n" +
                    "    WHERE jc.applicant_id IN (:applicantIdList) and jc.applicant_job_status=0  \n" +
                    "    ORDER BY jc.applicant_status DESC, jc.match_score DESC\n" +
                    ") AS subquery ON subquery.job_post_id = jp.job_post_id \n" +
                    "WHERE jp.status = 'ACTIVE' AND filtered_jpjf.job_post_id IS NOT NULL and subquery.applicant_job_status=0  and " +
                    " jp.job_post_id not in (select a.job_post_id from consultancy_job_status a where a.status in(1,2) and jp.job_post_id=a.job_post_id and a.consultancy_user_id in (:consultancyUserId))");

            Map<String, Object> params = new HashMap<>();
            // params.put("consultancyUserId", userIds);
            params.put("consultancyUserId", userId);

            if (role.equals("COMPANY_ADMIN") || role.equals("CONSULTANCY_ADMIN")) {
                sqlBuilder.append(" and jp.user_id != :userId ");
                params.put("userId", userId);
            }
            params.put("applicantIdList", applicants.stream().map(ConsJobApplicantDTO::getApplicantId).collect(Collectors.toList()));
            if (matchedJobsFilter.getJobFunction() != null && matchedJobsFilter.getJobFunction() != 0) {
                sqlBuilder.replace(sqlBuilder.indexOf("filterJobFunction"), sqlBuilder.indexOf("filterJobFunction") + "filterJobFunction".length(), " WHERE (jpjf.job_function_id = :jobFunction OR jpsjf.job_function_id = :jobFunction) ");
                params.put("jobFunction", matchedJobsFilter.getJobFunction());
            } else {
                sqlBuilder.replace(sqlBuilder.indexOf("filterJobFunction"), sqlBuilder.indexOf("filterJobFunction") + "filterJobFunction".length(), " ");
            }

            if (matchedJobsFilter.getLocations() != null && !matchedJobsFilter.getLocations().isEmpty()) {
                sqlBuilder.append(" AND jp.location IN (:location) ");
                params.put("location", matchedJobsFilter.getLocations());
            }

            if (matchedJobsFilter.getJobTypes() != null && !matchedJobsFilter.getJobTypes().isEmpty()) {
                sqlBuilder.append(" AND jp.job_type IN (:jobTypes)");
                params.put("jobTypes", matchedJobsFilter.getJobTypes());
            }

            if (matchedJobsFilter.getWorklocValue() != null && !matchedJobsFilter.getWorklocValue().isEmpty()) {
                sqlBuilder.append(" AND jp.workloc_type IN (:worklocValue) ");
                params.put("worklocValue", matchedJobsFilter.getWorklocValue());
            }

            if (matchedJobsFilter.getCitizenship() != null && !matchedJobsFilter.getCitizenship().isEmpty()) {
                sqlBuilder.append(" AND jp.citizenship_id IN (:citizenship) ");
                params.put("citizenship", matchedJobsFilter.getCitizenship());
            }

            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
                sqlBuilder.append(" AND js.skill_id IN (:jobSkills) ");
                params.put("jobSkills", matchedJobsFilter.getJobSkills());
            }

            if (matchedJobsFilter.getEduQualification() != null && !matchedJobsFilter.getEduQualification().isEmpty()) {
                sqlBuilder.append(" AND jd.degree_id IN (:eduQualification)");
                params.put("eduQualification", matchedJobsFilter.getEduQualification());
            }

            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
                sqlBuilder.append(" AND jp.notice_period IN (:noticePeriod) ");
                params.put("noticePeriod", matchedJobsFilter.getNoticePeriod());
            }

            Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                countQuery.setParameter(entry.getKey(), entry.getValue());
            }
            long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

            sqlBuilder.append(" group by jp.job_post_id ORDER BY MAX(subquery.applicant_status) DESC, MAX(subquery.match_score) DESC,jp.job_post_id DESC ");
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder);
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            List<Object[]> result = query.getResultList();
            response = generateConsJobViewResponseWithFilter(result, totalCount, applicants, pageSize);
        } catch (Exception e) {
            log.error("Error while fetching matched jobs", e);
        }
        //}
        return response;
    }
//    private ConsJobViewResponse generateConsJobViewResponseWithFilter(List<JobPost> jobPostObj, long totalCount, List<ConsJobApplicantDTO> applicants, Integer pageSize) {
//
//        if (jobPostObj.size() == 0) {
//            return null;
//        }
//        ConsJobViewResponse response = new ConsJobViewResponse();
//
//        List<ConsJobViewDTO> consJobViewDTOs = new ArrayList<>();
//        ConsJobViewDTO consJobViewDTO;
//        // Fetch consultancy data, applicant data, recruiter data
//        //  List<JobPost> jobPostObj = (List<JobPost>) jobPostRepository.findAllById(jobMatchList);
//        List<Long> recruiterIdList = jobPostObj.stream().map(JobPost::getUserId).collect(Collectors.toList());
////        List<UserCompanyResultSet> recruiterIds = userRepository.getCompanyInfoByUserList(recruiterIdList);
////
////        Map<Long, Company> userCompanyMap = new HashMap<>();
////        for (UserCompanyResultSet userCompanyResultSet : recruiterIds) {
////            userCompanyMap.put(userCompanyResultSet.getRecruiterId(), userCompanyResultSet.getCompany());
////        }
//
//        List<Long> applicantIds = applicants.stream().map(ConsJobApplicantDTO::getApplicantId).collect(Collectors.toList());
//        Map<Long, Company> userCompanyMap = userRepository.getCompanyInfoByUserList(recruiterIdList)
//                .stream()
//                .collect(Collectors.toMap(UserCompanyResultSet::getRecruiterId, UserCompanyResultSet::getCompany));
//
//        for (JobPost jobPost : jobPostObj) {
//            consJobViewDTO = new ConsJobViewDTO();
//            Company company = userCompanyMap.get(jobPost.getUserId());
//            consJobViewDTO.setCompanyName(company.getName());
//            String imageUrl = company.getProfileImageUrl();
//            try {
//                consJobViewDTO.setCompanyPic(imageService.getImage(imageUrl));
//            } catch (IOException e) {
//                consJobViewDTO.setCompanyPic(null);
//            }
//            consJobViewDTO.setJobTitle(jobPost.getTitle());
//            consJobViewDTO.setJobFunctionName(jobPost.getJobFunction().stream()
//                    .map(JobFunction::getJobFunctionName)
//                    .collect(Collectors.joining(",")));
//            consJobViewDTO.setJobPostId(jobPost.getJobPostId());
//            consJobViewDTO.setTotalApplicantCount(applicants.size());
//            consJobViewDTO.setLocation(company.getLocation());
//
//            List<ApplicantRecommendedResultSet> applicantsWithMatchScore = jobMatchConsultancyRepository.getApplicantRecommended(jobPost.getJobPostId(),applicantIds)
//                    .stream().sorted(Comparator.comparing(ApplicantRecommendedResultSet::getApplicantStatus).thenComparing(ApplicantRecommendedResultSet::getMatchScore).reversed()).collect(Collectors.toList());
//            Map<Long,Integer> applicantRecommended = applicantsWithMatchScore
//                    .stream().collect(Collectors.toMap(ApplicantRecommendedResultSet::getId,ApplicantRecommendedResultSet::getMatchScore,(score1, score2) -> score1,LinkedHashMap::new ));
//            consJobViewDTO.setInterestedCount(applicantsWithMatchScore.size());
//            consJobViewDTOs.add(consJobViewDTO);
//        }
//        response.setTotalCount(totalCount);
//        response.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
//        response.setConsJobViewDTO(consJobViewDTOs);
//        return response;
//    }

    private ConsJobViewResponse generateConsJobViewResponse(Page<JobMatchConsultancy> jobMatchList, List<Long> userIds, long consultancyId, Pageable pageable) throws IOException, ParseException {
        ConsJobViewResponse response = new ConsJobViewResponse();
        List<Long> jobIdList = new ArrayList<>();
        List<Long> recruiterIdList = new ArrayList<>();


        for (JobMatchConsultancy jobMatch : jobMatchList) {
            jobIdList.add(jobMatch.getJobPostId());
            recruiterIdList.add(jobMatch.getRecruiterId());
        }

        List<ConsJobViewDTO> consJobViewDTOs = new ArrayList<>();
        ConsJobViewDTO consJobViewDTO;
        // Fetch consultancy data, applicant data, recruiter data
        List<JobPost> jobPostObj = jobPostRepository.findAllById(jobIdList);
        List<UserCompanyResultSet> recruiterIds = userRepository.getCompanyInfoByUserList(recruiterIdList);

        Map<Long, Company> userCompanyMap = new HashMap<>();
        for (UserCompanyResultSet userCompanyResultSet : recruiterIds) {
            userCompanyMap.put(userCompanyResultSet.getRecruiterId(), userCompanyResultSet.getCompany());
        }

        Map<Long, JobPost> jobPostMap = jobPostObj.stream()
                .collect(Collectors.toMap(JobPost::getJobPostId, b -> b));

        for (JobMatchConsultancy jobMatch : jobMatchList) {
            consJobViewDTO = new ConsJobViewDTO();
            consJobViewDTO.setCompanyName(userCompanyMap.get(jobMatch.getRecruiterId()).getName());

            String imageUrl = userCompanyMap.get(jobMatch.getRecruiterId()).getProfileImageUrl();
            try {
                consJobViewDTO.setCompanyPic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                consJobViewDTO.setCompanyPic(null);
            }
            consJobViewDTO.setJobTitle(jobPostMap.get(jobMatch.getJobPostId()).getTitle());
            consJobViewDTO.setJobFunctionName(jobPostMap.get(jobMatch.getJobPostId()).getJobFunction().stream()
                    .map(a -> String.valueOf(a.getJobFunctionName())).collect(Collectors.joining(",")));
            consJobViewDTO.setJobPostId(jobMatch.getJobPostId());
            //TODO need to enhance
            Long intrestedCount = jobMatchConsultancyRepository.getIntrestedProfilesCountForRecruiter(jobMatch.getJobPostId(), userIds);
            Long recommendedCount = jobMatchConsultancyRepository.getRecommendedProfilesCountForRecruiter(jobMatch.getJobPostId(), userIds);
            //TODO need to enhance
            Page<ConsApplicantStatusResultSet> jobMatchConsList = jobMatchConsultancyRepository
                    .findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(jobMatch.getJobPostId(), consultancyId, userIds, pageable);


            Optional<ConsultancyJobStatus> optionalConsultancyJobStatus = consultancyJobStatusRepository.findByJobPostIdAndConsultancyUserId(jobMatch.getJobPostId(), userIds);
            if (optionalConsultancyJobStatus.isPresent()) {
                consJobViewDTO.setUpdatedDate(optionalConsultancyJobStatus.get().getUpdatedDate());
            }

            consJobViewDTO.setStatus(intrestedCount > 0 ? 1 : 0);
            consJobViewDTO.setInterestedCount(intrestedCount);
            consJobViewDTO.setTotalApplicantCount(jobMatchConsList.getTotalElements());
            consJobViewDTO.setLocation(userCompanyMap.get(jobMatch.getRecruiterId()).getLocation());

            consJobViewDTO.setRecommended(recommendedCount > 0);
            consJobViewDTO.setMatchScore(recommendedCount > 0 ? 100 : jobMatch.getMatchScore());
            if (jobMatch.getUpdatedDate() != null) {
                String[] recruiterUpdatedDate = consJobViewDTO.getUpdatedDate().toString().split("-");
                LocalDate recruiterUpdatedLocalDate = LocalDate.of(Integer.parseInt(recruiterUpdatedDate[0]), Integer.parseInt(recruiterUpdatedDate[1]), Integer.parseInt(recruiterUpdatedDate[2].split(" ")[0]));
                long daysDiff = ChronoUnit.DAYS.between(recruiterUpdatedLocalDate, LocalDate.now());
                consJobViewDTO.setExpiration(daysDiff < 0 ? "Expired" : (7 - daysDiff) == 0 ? "Expire today" : (7 - daysDiff) == 1 ? "Expires in 1 day" : (daysDiff > 7) ? "Expired" : "Expires in " + (7 - daysDiff) + " days");
            }
            consJobViewDTOs.add(consJobViewDTO);
        }
        response.setTotalCount(jobMatchList.getTotalElements());
        response.setTotalPages(jobMatchList.getTotalPages());
        //consJobViewDTOs.sort(Comparator.comparing(ConsJobViewDTO::getStatus).thenComparing(ConsJobViewDTO::getMatchScore).reversed());
        consJobViewDTOs.sort(Comparator.comparing(ConsJobViewDTO::getUpdatedDate).reversed());

        response.setConsJobViewDTO(consJobViewDTOs);
        return response;
    }

    private ConsJobViewResponse generateConsJobViewResponseforJobs(Page<JobMatchConsultancy> jobMatchList, List<Long> userIds, long consultancyId, Pageable pageable) throws IOException, ParseException {
        ConsJobViewResponse response = new ConsJobViewResponse();
        List<Long> jobIdList = new ArrayList<>();
        List<Long> recruiterIdList = new ArrayList<>();


        for (JobMatchConsultancy jobMatch : jobMatchList) {
            jobIdList.add(jobMatch.getJobPostId());
            recruiterIdList.add(jobMatch.getRecruiterId());
        }

        List<ConsJobViewDTO> consJobViewDTOs = new ArrayList<>();
        ConsJobViewDTO consJobViewDTO;
        // Fetch consultancy data, applicant data, recruiter data
        List<JobPost> jobPostObj = jobPostRepository.findAllById(jobIdList);
        List<UserCompanyResultSet> recruiterIds = userRepository.getCompanyInfoByUserList(recruiterIdList);

        Map<Long, Company> userCompanyMap = new HashMap<>();
        for (UserCompanyResultSet userCompanyResultSet : recruiterIds) {
            userCompanyMap.put(userCompanyResultSet.getRecruiterId(), userCompanyResultSet.getCompany());
        }

        Map<Long, JobPost> jobPostMap = jobPostObj.stream()
                .collect(Collectors.toMap(JobPost::getJobPostId, b -> b));

        for (JobMatchConsultancy jobMatch : jobMatchList) {
            consJobViewDTO = new ConsJobViewDTO();
            consJobViewDTO.setCompanyName(userCompanyMap.get(jobMatch.getRecruiterId()).getName());

            String imageUrl = userCompanyMap.get(jobMatch.getRecruiterId()).getProfileImageUrl();
            try {
                consJobViewDTO.setCompanyPic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);
            } catch (IOException e) {
                consJobViewDTO.setCompanyPic(null);
            }
            consJobViewDTO.setJobTitle(jobPostMap.get(jobMatch.getJobPostId()).getTitle());
            consJobViewDTO.setJobFunctionName(jobPostMap.get(jobMatch.getJobPostId()).getJobFunction().stream()
                    .map(a -> String.valueOf(a.getJobFunctionName())).collect(Collectors.joining(",")));
            consJobViewDTO.setJobPostId(jobMatch.getJobPostId());
            //TODO need to enhance
            Long intrestedCount = jobMatchConsultancyRepository.getIntrestedProfilesCountForRecruiter(jobMatch.getJobPostId(), userIds);
            Long recommendedCount = jobMatchConsultancyRepository.getRecommendedProfilesCountForRecruiter(jobMatch.getJobPostId(), userIds);
            //TODO need to enhance
            Page<ConsApplicantStatusResultSet> jobMatchConsList = jobMatchConsultancyRepository
                    .findByJobPostIdAndConsultancyIdAndConsultancyUserIdBasedOnStatus(jobMatch.getJobPostId(), consultancyId, userIds, pageable);


            consJobViewDTO.setUpdatedDate(jobMatch.getUpdatedDate());


            consJobViewDTO.setStatus(intrestedCount != null && intrestedCount > 0 ? 1 : 0);
            consJobViewDTO.setInterestedCount(intrestedCount);
            consJobViewDTO.setTotalApplicantCount(jobMatchConsList.getTotalElements());
            consJobViewDTO.setLocation(userCompanyMap.get(jobMatch.getRecruiterId()).getLocation());

            consJobViewDTO.setRecommended(recommendedCount > 0);
            consJobViewDTO.setMatchScore(recommendedCount > 0 ? 100 : jobMatch.getMatchScore());
            if (jobMatch.getUpdatedDate() != null) {
                String[] recruiterUpdatedDate = jobMatch.getUpdatedDate().toString().split("-");
                LocalDate recruiterUpdatedLocalDate = LocalDate.of(Integer.parseInt(recruiterUpdatedDate[0]), Integer.parseInt(recruiterUpdatedDate[1]), Integer.parseInt(recruiterUpdatedDate[2].split(" ")[0]));
                long daysDiff = ChronoUnit.DAYS.between(recruiterUpdatedLocalDate, LocalDate.now());
                consJobViewDTO.setExpiration(daysDiff < 0 ? "Expired" : (7 - daysDiff) == 0 ? "Expire today" : (7 - daysDiff) == 1 ? "Expires in 1 day" : ("Expires in " + (7 - daysDiff) + " days"));
            }
            consJobViewDTOs.add(consJobViewDTO);
        }
        response.setTotalCount(jobMatchList.getTotalElements());
        response.setTotalPages(jobMatchList.getTotalPages());
        consJobViewDTOs.sort(Comparator.comparing(ConsJobViewDTO::getStatus).thenComparing(ConsJobViewDTO::getMatchScore).reversed());
        //consJobViewDTOs.sort(Comparator.comparing(ConsJobViewDTO::getUpdatedDate).reversed());

        response.setConsJobViewDTO(consJobViewDTOs);
        return response;
    }

    private ConsJobViewResponse generateConsJobViewResponseWithFilter(List<Object[]> result, long totalCount, List<ConsJobApplicantDTO> applicants, Integer pageSize) throws Exception {

        if (result.size() == 0) {
            return null;
        }
        Set<Long> recruiterIdList = new HashSet<>();
        for (Object[] row : result) {
            recruiterIdList.add(((BigInteger) row[2]).longValue());
        }

        ConsJobViewResponse response = new ConsJobViewResponse();
        List<ConsJobViewDTO> consJobViewDTOs = new ArrayList<>();
        ConsJobViewDTO consJobViewDTO;

        Map<Long, Company> userCompanyMap = userRepository.getCompanyInfoByUserList(recruiterIdList.stream().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(UserCompanyResultSet::getRecruiterId, UserCompanyResultSet::getCompany));

        for (Object[] row : result) {
            consJobViewDTO = new ConsJobViewDTO();
            Company company = userCompanyMap.get(((BigInteger) row[2]).longValue());
            consJobViewDTO.setCompanyName(company.getName());
            String imageUrl = company.getProfileImageUrl();
            try {
                consJobViewDTO.setCompanyPic(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.company_default_image);

            } catch (IOException e) {
                consJobViewDTO.setCompanyPic(CommonConstants.company_default_image);
            }
            Long jobPostId = ((BigInteger) row[0]).longValue();
            consJobViewDTO.setJobTitle(((String) row[1]));
            consJobViewDTO.setJobFunctionName((String) row[4]);
            consJobViewDTO.setJobPostId(jobPostId);
            List<Long> applicantIds = applicants.stream().map(ConsJobApplicantDTO::getApplicantId).collect(Collectors.toList());
            consJobViewDTO.setTotalApplicantCount(jobMatchConsultancyRepository.getApplicantCountForJob(jobPostId, applicantIds));
            consJobViewDTO.setLocation(company.getLocation());
            Integer matchScore, status;
            if (row[3] != null && row[5] != null) {
                matchScore = getIntegerValue(row[3]);
                status = getIntegerValue(row[5]);
                Long intrestedCount = jobMatchConsultancyRepository.interestedCountForJobStatus(jobPostId, applicantIds);
                consJobViewDTO.setInterestedCount(intrestedCount);
                if (intrestedCount > 0) {
                    status = 1;
                }
            } else {
                JobPost jobPost = jobPostRepository.findById(jobPostId).get();
                List<Integer> skillList = jobPost.getJobSkills().stream().map(JobSkills::getSkillId)
                        .collect(Collectors.toList());
                long expMin = jobPost.getExperienceMin();
                long expMax = jobPost.getExperienceMax();
                List<Integer> jobFuncList = jobPost.getJobFunction().stream().map(JobFunction::getJobFunctionId)
                        .collect(Collectors.toList());
                List<Integer> jobSecFuncList = jobPost.getOptionalJobfunctions().stream().map(JobFunction::getJobFunctionId)
                        .collect(Collectors.toList());
                List<Long> matchedApplicants = jobPostRepository.findUserProfilesByCriteriaForFilteredApp(skillList, jobFuncList, expMin, expMax, applicantIds, jobSecFuncList, null);
                List<Integer> scores = matchedApplicants.stream()
                        .map(id -> {
                            Applicant applicant = applicantRepository.findById(id).get();
                            List<ApplicantJobSkill> applicantJobSkills = applicantJobSkillRepository.findByApplicantId(applicant.getApplicantId());
                            ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicant.getApplicantId());
                            List<ApplicantJobFunction> applicantJobFunctions = applicantJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
                            List<ApplicantSecondaryJobFunction> applicantSecondaryJobFunctions = applicantSecondaryJobFunctionRepository.findByApplicantId(applicant.getApplicantId());
                            Set<Integer> applJobSkillIdSet = applicantJobSkills.stream().map(ApplicantJobSkill::getJobSkillId).collect(Collectors.toSet());
                            Set<Integer> applJobFuncIdSet = applicantJobFunctions.stream().map(ApplicantJobFunction::getJobFunctionId).collect(Collectors.toSet());
                            Set<Integer> applSecJobFunIdSet = applicantSecondaryJobFunctions.stream().map(ApplicantSecondaryJobFunction::getJobFunctionId).collect(Collectors.toSet());
                            List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
                            Set<Integer> appDegreeIdSet = educationHistories.stream()
                                    .map(EducationHistory::getDegree)
                                    .map(degreesRepository::findByShortTitle)
                                    .filter(Objects::nonNull)
                                    .map(Degrees::getDegreeId)
                                    .collect(Collectors.toSet());

                            return jobPostService.calculateMatchScore(jobPost, applicant, applicantDetails, applJobFuncIdSet, applJobSkillIdSet, applSecJobFunIdSet, appDegreeIdSet);
                        })
                        .collect(Collectors.toList());
                matchScore = scores != null && !scores.isEmpty() ? Collections.max(scores) : 0;
                status = 0;
            }
            consJobViewDTO.setRecommended(matchScore >= 95);
            consJobViewDTO.setMatchScore(matchScore);
            consJobViewDTO.setStatus(status);
            consJobViewDTOs.add(consJobViewDTO);
        }
        response.setTotalCount(totalCount);
        response.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        response.setConsJobViewDTO(consJobViewDTOs.stream().sorted(Comparator.comparing(ConsJobViewDTO::isRecommended).thenComparing(ConsJobViewDTO::getInterestedCount).thenComparing(ConsJobViewDTO::getMatchScore).reversed()).collect(Collectors.toList()));
        return response;
    }

//    public ConsJobApplicantResponse filterMatchedApplicants(MatchedJobsFilter matchedJobsFilter, Long consultancyId, Integer pageNo, Integer pageSize,boolean needPagination) {
//
//        ConsJobApplicantResponse response = new ConsJobApplicantResponse();
//        try {
//
//            String selectQuery = "select distinct a.applicant_id, GROUP_CONCAT(DISTINCT eh.degree SEPARATOR ',') AS degrees,a.first_name,a.last_name,ad.job_function\n" +
//                    " ,ad.years_of_exp,\n" +
//                    " ad.job_type\n" +
//                    ",  a.location ";
//            String countSql = "SELECT count(distinct a.applicant_id)  ";
//            StringBuilder sqlBuilder = new StringBuilder(" from applicant a \n" +
//                    "left join applicant_details ad on ad.applicant_id=a.applicant_id\n" +
//                    "left join applicant_job_function ajf on ajf.applicant_id=a.applicant_id\n" +
//                    "left join education_history eh on eh.applicant_id=a.applicant_id\n" +
//                    "left join applicant_job_skill ajs on ajs.applicant_id=a.applicant_id\n" +
//                    "left join degrees d on (d.SHORT_TITLE = eh.degree or d.title = eh.degree) where  a.consultancy_id=:consultancyId and a.correction_required=0 ");
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("consultancyId", consultancyId);
//
//
//            if (matchedJobsFilter.getJobFunction() != null && matchedJobsFilter.getJobFunction() != 0) {
//                sqlBuilder.append(" AND ajf.job_function_id = :jobFunction ");
//                params.put("jobFunction", matchedJobsFilter.getJobFunction());
//            }
//
//            if (matchedJobsFilter.getLocations() != null && !matchedJobsFilter.getLocations().isEmpty()) {
//                sqlBuilder.append(" AND a.location IN (:location) ");
//                params.put("location", matchedJobsFilter.getLocations());
//            }
//
//            if (matchedJobsFilter.getJobTypes() != null && !matchedJobsFilter.getJobTypes().isEmpty()) {
//                sqlBuilder.append(" AND ad.job_type IN (:jobTypes)");
//                params.put("jobTypes", matchedJobsFilter.getJobTypes().stream().map(value -> JobType.getByValue(value).toString()).collect(Collectors.toList()));
//            }
//
//            if (matchedJobsFilter.getWorklocValue() != null && !matchedJobsFilter.getWorklocValue().isEmpty()) {
//                sqlBuilder.append(" AND ad.preferred_work_mode IN (:worklocValue) ");
//                params.put("worklocValue", matchedJobsFilter.getWorklocValue());
//            }
//
//            if (matchedJobsFilter.getCitizenship() != null && !matchedJobsFilter.getCitizenship().isEmpty()) {
//                sqlBuilder.append(" AND ad.citizenship IN (:citizenship) ");
//                params.put("citizenship", matchedJobsFilter.getCitizenship().stream().map(value -> Citizenship.getValueOf(value).toString()).collect(Collectors.toList()));
//            }
//
//            if (matchedJobsFilter.getEduQualification() != null && !matchedJobsFilter.getEduQualification().isEmpty()) {
//                sqlBuilder.append(" AND d.degree_id IN (:eduQualification)");
//                params.put("eduQualification", matchedJobsFilter.getEduQualification());
//            }
//            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
//                sqlBuilder.append(" AND ajs.job_skill_id IN (:jobSkills) ");
//                params.put("jobSkills", matchedJobsFilter.getJobSkills());
//            }
//
//            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
//                sqlBuilder.append(" AND ad.notice_period in (:noticePeriod)");
//                params.put("noticePeriod", matchedJobsFilter.getNoticePeriod().stream().map(value -> NoticePeriod.getValueOf(value).toString()).collect(Collectors.toList()));
//            }
//
////            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
////                sqlBuilder.append(" AND ad.notice_period IN (:noticePeriod)");
////                if (matchedJobsFilter.getNoticePeriod().size() == 1) {
////                    params.put("noticePeriod", NoticePeriod.getValueOf(matchedJobsFilter.getNoticePeriod()).toString());
////                } else {
////                    sqlBuilder.append(" AND (ad.notice_period= 'Immediate'  OR  ad.notice_period='Less Than 15 days') ");
////                }
////            }
//            Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder.toString());
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                countQuery.setParameter(entry.getKey(), entry.getValue());
//            }
//            long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();
//
//            Pageable pageable = PageRequest.of(pageNo, pageSize);
//            sqlBuilder.append(" group by a.applicant_id,\n" +
//                    " ad.job_function,\n" +
//                    " ad.years_of_exp ,ad.job_type ");
//            Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder.toString());
//            if(needPagination) {
//                query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
//                query.setMaxResults(pageable.getPageSize());
//            }
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                query.setParameter(entry.getKey(), entry.getValue());
//            }
//
//            List<Object[]> results = query.getResultList();
//            response = genarateMatchedApplicants(results, totalCount, pageSize,needPagination);
//        } catch (Exception e) {
//            log.error("Error while fetching matched jobs", e);
//        }
//        return response;
//    }

    public ConsJobApplicantResponse genarateMatchedApplicants(List<Object[]> results, MatchedJobsFilter matchedJobsFilter, long totalCount, Integer pageSize, boolean needPagination) throws Exception {
        ConsJobApplicantResponse consJobApplicantResponse = new ConsJobApplicantResponse();
        List<ConsJobApplicantDTO> consJobApplicantDTOS = new ArrayList<>();
        for (Object[] row : results) {
            ConsJobApplicantDTO consJobApplicantDTO = new ConsJobApplicantDTO();
            consJobApplicantDTO.setApplicantId(((BigInteger) row[0]).longValue());
            consJobApplicantDTO.setJobFunction(row[4] != null ? (String) row[4] : "");

            if (needPagination) {
                String firstname = row[2] != null ? (String) row[2] : "";
                String lastName = row[3] != null ? (String) row[3] : "";

                consJobApplicantDTO.setApplicantName(firstname + " " + lastName);
                consJobApplicantDTO.setExperience(row[5] != null ? ((String) row[5]) : "");
                consJobApplicantDTO.setJobType(row[6] != null ? (String) row[6] : "");
                consJobApplicantDTO.setLocation(row[7] != null ? (String) row[7] : "");
                consJobApplicantDTO.setProfilePic(row[10] != null ? imageService.getImage((String) row[10]) : CommonConstants.applicant_default_image);

                consJobApplicantDTO.setQualification(row[1] != null ? degreesRepository.findById(((BigInteger) row[1]).intValue()).get().getTitle() : "");
                Integer score, status;
                if (row[8] != null) {
                    score = getIntegerValue(row[8]);
                    status = getIntegerValue(row[9]);
                } else {
                    score = jobPostService.calculateAppFilterScore(((BigInteger) row[0]).longValue(), matchedJobsFilter, null);
                    status = 0;
                }
                consJobApplicantDTO.setRecommended(score >= 95);
                consJobApplicantDTO.setMatchScore(score);
                consJobApplicantDTO.setStatus(status);
            }
            consJobApplicantDTOS.add(consJobApplicantDTO);
        }

        consJobApplicantResponse.setConsJobApplicantDTO(consJobApplicantDTOS.stream().sorted(Comparator.comparing(ConsJobApplicantDTO::getStatus)
                .thenComparing(ConsJobApplicantDTO::isRecommended)
                .thenComparing(ConsJobApplicantDTO::getMatchScore).reversed()).collect(Collectors.toList()));
        consJobApplicantResponse.setTotalCount(totalCount);
        consJobApplicantResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return consJobApplicantResponse;
    }

    public ConsJobApplicantResponse filterMatchedApplicants(MatchedJobsFilter matchedJobsFilter, Long consultancyId, long userId, String role, Integer pageNo, Integer pageSize, boolean needPagination) {

        ConsJobApplicantResponse response = new ConsJobApplicantResponse();
        List<Long> userIds = getUserIds(consultancyId, userId, role, 2);
        try {

            String selectQuery = "select distinct a.applicant_id, ad.degree_id ,a.first_name,a.last_name,ad.job_function\n" +
                    ",ad.years_of_exp,\n" +
                    "ad.job_type\n" +
                    ",a.location,MAX(subquery.match_score) AS maxMatchScore, MAX(subquery.applicant_status),a.profile_image_url ";
            String countSql = "SELECT count(distinct a.applicant_id)  ";
            StringBuilder sqlBuilder = new StringBuilder(" from applicant a \n" +
                    "left join applicant_details ad on ad.applicant_id=a.applicant_id\n" +
//                    "left join applicant_job_function ajf on ajf.applicant_id=a.applicant_id\n" +
//                    "left join applicant_secondary_job_function asjf on asjf.applicant_id=a.applicant_id\n" +
//                    "left join education_history eh on eh.applicant_id=a.applicant_id\n" +
//                    "left join applicant_job_skill ajs on ajs.applicant_id=a.applicant_id\n" +
//                    "left join degrees d on (d.SHORT_TITLE = eh.degree or d.title = eh.degree)\n" +
                    " left JOIN (\n" +
                    "    SELECT jc.applicant_id, jc.applicant_status, jc.match_score,jc.job_post_id, jc.consultancy_id,jc.applicant_job_status \n" +
                    "    FROM job_match_consultancy jc where  jc.applicant_job_status=0 filterWithJobId \n" +
                    "    ORDER BY jc.applicant_status DESC, jc.match_score DESC\n" +
                    ") AS subquery ON subquery.applicant_id = a.applicant_id  where  a.consultancy_id=:consultancyId " +
                    "and a.correction_required=0 and a.consultancy_user_id in (:consultancyUserId) and subquery.applicant_job_status=0 and subquery.applicant_status in (0,1) " +
                    "and subquery.job_post_id not in (select a.job_post_id from consultancy_job_status a where a.status in(1,2) and  a.consultancy_user_id in (:savedConsultancyUserId) and subquery.job_post_id=a.job_post_id)");

            Map<String, Object> params = new HashMap<>();
            params.put("consultancyId", consultancyId);
            params.put("consultancyUserId", userIds);
            params.put("savedConsultancyUserId", userId);

            if (needPagination && matchedJobsFilter.getJobId() != null && matchedJobsFilter.getJobId() != 0) {
                sqlBuilder.replace(sqlBuilder.indexOf("filterWithJobId"), sqlBuilder.indexOf("filterWithJobId") + "filterWithJobId".length(), " and jc.job_post_id=:jobId ");
                params.put("jobId", matchedJobsFilter.getJobId());
            } else {
                sqlBuilder.replace(sqlBuilder.indexOf("filterWithJobId"), sqlBuilder.indexOf("filterWithJobId") + "filterWithJobId".length(), " ");
            }
            if (matchedJobsFilter.getJobFunction() != null && matchedJobsFilter.getJobFunction() != 0) {
                sqlBuilder.append(" AND (ad.job_function_id = :jobFunction OR FIND_IN_SET(:jobFunction, ad.secondary_job_function_id) > 0) ");
                params.put("jobFunction", matchedJobsFilter.getJobFunction());
            }


            if (matchedJobsFilter.getLocations() != null && !matchedJobsFilter.getLocations().isEmpty()) {
                sqlBuilder.append(" AND a.location IN (:location) ");
                params.put("location", matchedJobsFilter.getLocations());
            }

            if (matchedJobsFilter.getJobTypes() != null && !matchedJobsFilter.getJobTypes().isEmpty()) {
                sqlBuilder.append(" AND ad.job_type IN (:jobTypes) ");
                params.put("jobTypes", matchedJobsFilter.getJobTypes().stream().map(value -> JobType.getByValue(value).toString()).collect(Collectors.toList()));
            }

            if (matchedJobsFilter.getWorklocValue() != null && !matchedJobsFilter.getWorklocValue().isEmpty()) {
                sqlBuilder.append(" AND ad.preferred_work_mode IN (:worklocValue) ");
                params.put("worklocValue", matchedJobsFilter.getWorklocValue());
            }

            if (matchedJobsFilter.getCitizenship() != null && !matchedJobsFilter.getCitizenship().isEmpty()) {
                sqlBuilder.append(" AND ad.citizenship IN (:citizenship) ");
                params.put("citizenship", matchedJobsFilter.getCitizenship().stream().map(value -> Citizenship.getValueOf(value).toString()).collect(Collectors.toList()));
            }

            if (matchedJobsFilter.getEduQualification() != null && !matchedJobsFilter.getEduQualification().isEmpty()) {
                sqlBuilder.append(" AND ad.degree_id IN (:eduQualification) ");
                params.put("eduQualification", matchedJobsFilter.getEduQualification());
            }
//            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
//                sqlBuilder.append(" AND ajs.job_skill_id IN (:jobSkills) ");
//                params.put("jobSkills", matchedJobsFilter.getJobSkills());
//            }

            if (matchedJobsFilter.getJobSkills() != null && !matchedJobsFilter.getJobSkills().isEmpty()) {
                sqlBuilder.append(" AND ( ");
                for (Integer skillId : matchedJobsFilter.getJobSkills()) {
                    sqlBuilder.append("FIND_IN_SET('").append(skillId).append("', ad.skill_id) > 0");
                    if (skillId != matchedJobsFilter.getJobSkills().get(matchedJobsFilter.getJobSkills().size() - 1)) {
                        sqlBuilder.append(" OR ");
                    }
                }
                sqlBuilder.append(") ");
//                params.put("jobSkills", matchedJobsFilter.getJobSkills());
            }

            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
                sqlBuilder.append(" AND ad.notice_period in (:noticePeriod)");
                params.put("noticePeriod", matchedJobsFilter.getNoticePeriod().stream().map(value -> NoticePeriod.getValueOf(value).toString()).collect(Collectors.toList()));
            }

//            if (matchedJobsFilter.getNoticePeriod() != null && !matchedJobsFilter.getNoticePeriod().isEmpty()) {
//                sqlBuilder.append(" AND ad.notice_period IN (:noticePeriod)");
//                if (matchedJobsFilter.getNoticePeriod().size() == 1) {
//                    params.put("noticePeriod", NoticePeriod.getValueOf(matchedJobsFilter.getNoticePeriod()).toString());
//                } else {
//                    sqlBuilder.append(" AND (ad.notice_period= 'Immediate'  OR  ad.notice_period='Less Than 15 days') ");
//                }
//            }
            Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                countQuery.setParameter(entry.getKey(), entry.getValue());
            }
            long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

            Pageable pageable = PageRequest.of(pageNo, pageSize);
            sqlBuilder.append(" group by a.applicant_id,\n" +
                    " ad.job_function, ad.degree_id ,\n" +
                    " ad.years_of_exp ,ad.job_type ORDER BY MAX(subquery.applicant_status) DESC, MAX(subquery.match_score) DESC,a.applicant_id DESC ");

            Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder);
            if (needPagination) {
                query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
                query.setMaxResults(pageable.getPageSize());
            }
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            List<Object[]> results = query.getResultList();
            response = genarateMatchedApplicants(results, matchedJobsFilter, totalCount, pageSize, needPagination);
        } catch (Exception e) {
            log.error("Error while fetching matched jobs", e);
        }
        return response;
    }

}
