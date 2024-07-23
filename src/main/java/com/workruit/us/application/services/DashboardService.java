/**
 *
 */
package com.workruit.us.application.services;

import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.JobStatus;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santosh Bhima
 */
@Service
public class DashboardService {
    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired InterviewRepository inteviewRepository;
    private @Autowired CompanyRepository companyRepository;
    private @Autowired JobPostService jobPostService;
    private @Autowired AlertService alertService;
    private @Autowired JobMatchingRepository jobMatchingRepository;
    private @Autowired ApplicantRepository applicantRepository;

    /**
     * select applicant_status,count(*) from job_match_consultancy where
     * consultancy_id=1 group by applicant_status
     *
     * @param consultancyId
     * @param size
     * @param page
     * @return
     */
    public DashboardDTO dashboard(Long consultancyId, int page, int size) {
        List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository
                .findByConsultancyId(consultancyId);
        int shortlistedCount = jobMatchConsultancyRepository.countByConsultancyIdAndApplicantStatus(consultancyId, 1)
                .intValue();
        int interviewedCount = jobMatchConsultancyRepository.countByConsultancyIdAndApplicantStatus(consultancyId, 2)
                .intValue();
        int hiredCount = jobMatchConsultancyRepository.countByConsultancyIdAndApplicantStatus(consultancyId, 3)
                .intValue();
        int rejectedCount = jobMatchConsultancyRepository.countByConsultancyIdAndApplicantStatus(consultancyId, 4)
                .intValue();
        int profilesUploaded = jobMatchConsultancies.size();
        DashboardDTO dashboardDTO = new DashboardDTO();
        StatusDTO statusDTO = new StatusDTO();
        OverviewDTO overviewDTO = new OverviewDTO();
        dashboardDTO.setStatus(statusDTO);
        Map<TeamProfileDTO, List<JobAppliedFor>> teamProfileMap = new HashMap<>();
        Set<Long> recruiterIds = new HashSet<>();
        for (JobMatchConsultancy jobMatchConsultancy : jobMatchConsultancies) {
            TeamProfileDTO teamProfileDTO = new TeamProfileDTO();
            if (jobMatchConsultancy.getApplicantStatus() == 1) {
                shortlistedCount++;
            } else if (jobMatchConsultancy.getApplicantStatus() == 2) {
                interviewedCount++;
            } else if (jobMatchConsultancy.getApplicantStatus() == 3) {
                hiredCount++;
            } else if (jobMatchConsultancy.getApplicantStatus() == 4) {
                rejectedCount++;
            }
            User user = userRepository.findById(jobMatchConsultancy.getRecruiterId()).get();
            teamProfileDTO.setName(user.getFirstName() + " " + user.getLastName());
            teamProfileDTO.setRecruiterId(jobMatchConsultancy.getRecruiterId());
            teamProfileDTO.setTotalProfilesUploaded(profilesUploaded);

            if (teamProfileMap.get(teamProfileDTO) == null) {
                teamProfileMap.put(teamProfileDTO, new ArrayList<>());
            }
            recruiterIds.add(jobMatchConsultancy.getRecruiterId());
        }

        if (recruiterIds.size() > 0) {
            int lastIndex = recruiterIds.size() >= size ? page * size + size : recruiterIds.size();
            recruiterIds = new HashSet<>(new ArrayList<>(recruiterIds).subList(page * size, lastIndex));
        }

        for (TeamProfileDTO teamProfileDTO : teamProfileMap.keySet()) {
            Long recruitorId = teamProfileDTO.getRecruiterId();
            List<JobMatchConsultancy> jobMatches = jobMatchConsultancyRepository
                    .findByRecruiterIdAndConsultancyId(recruitorId, consultancyId);
            Set<Long> joPostIds = jobMatches.stream().map(jobMatch -> jobMatch.getJobPostId())
                    .collect(Collectors.toSet());
            teamProfileDTO.setJobAppliedFors(new ArrayList<>());
            for (Long jobPostId : joPostIds) {
                List<JobMatchConsultancy> recruiterJobMatches = jobMatchConsultancyRepository
                        .findByRecruiterIdAndConsultancyIdAndJobPostId(recruitorId, consultancyId, jobPostId);
                JobPost jobPost = jobPostRepository.findJobPostByJobPostIdAndStatus(jobPostId, JobStatus.ACTIVE);
                JobAppliedFor e = new JobAppliedFor();
                e.setJobName(jobPost.getTitle());
                shortlistedCount = recruiterJobMatches.stream().filter(x -> x.getApplicantStatus() == 1)
                        .mapToInt(x -> x.getApplicantStatus()).sum();
                interviewedCount = recruiterJobMatches.stream().filter(x -> x.getApplicantStatus() == 2)
                        .mapToInt(x -> x.getApplicantStatus()).sum();
                hiredCount = recruiterJobMatches.stream().filter(x -> x.getApplicantStatus() == 3)
                        .mapToInt(x -> x.getApplicantStatus()).sum();
                rejectedCount = recruiterJobMatches.stream().filter(x -> x.getApplicantStatus() == 4)
                        .mapToInt(x -> x.getApplicantStatus()).sum();
                e.setHired(hiredCount);
                e.setInterviewed(interviewedCount);
                e.setHired(hiredCount);
                e.setRejected(rejectedCount);
                teamProfileDTO.getJobAppliedFors().add(e);
            }
        }
        dashboardDTO.setTeamProfiles(teamProfileMap.keySet());
        statusDTO.setHired(hiredCount);
        statusDTO.setInterviewed(interviewedCount);
        statusDTO.setRejected(rejectedCount);
        statusDTO.setShortlisted(shortlistedCount);
        overviewDTO.setProfilesUploaded(profilesUploaded);
        overviewDTO.setJobsAppliedFor(
                jobMatchConsultancyRepository.countByConsultancyIdAndApplicantJobStatus(consultancyId, 1).intValue());
        List<ApplicantInterviewDTO> applicantInterviews = new ArrayList<>();
        dashboardDTO.setApplicantInterviews(applicantInterviews);
        List<Interview> interviews = inteviewRepository.findByRecruiterIdIn(recruiterIds);
        if (interviews != null && interviews.size() > 0) {
            dashboardDTO.setApplicantInterviews(interviews.stream().map(interview -> {
                Long applicantId = interview.getApplicantId();
                Long jobPostID = interview.getJobPostId();
                String interviewDate = interview.getInterviewDate() + " " + interview.getInterviewStartTime();
                JobPost jobPost = jobPostRepository.findById(jobPostID).get();
                String jobTitle = jobPost.getTitle();
                User user = userRepository.findById(interview.getRecruiterId()).get();
                Long companyId = user.getCompanyId();
                Company company = companyRepository.findById(companyId).get();
                String companyName = company.getName();
                ApplicantInterviewDTO applicantInterviewDTO = new ApplicantInterviewDTO();
                applicantInterviewDTO.setApplicantId(applicantId);
                applicantInterviewDTO.setInteviewId(interview.getInterviewId());
                applicantInterviewDTO.setCompanyId(company.getCompanyId());
                applicantInterviewDTO.setInterviewDate(interviewDate);
                applicantInterviewDTO.setJobTitle(jobTitle);
                applicantInterviewDTO.setRecruiterId(interview.getRecruiterId());
                applicantInterviewDTO.setRecruiterName(user.getFirstName() + " " + user.getLastName());
                applicantInterviewDTO.setCompanyName(companyName);
                return applicantInterviewDTO;
            }).collect(Collectors.toList()));
        }
        return dashboardDTO;
    }

    public DashboardDTO dashboardStats(Long consultancyId, Long receuiterId, String role) {

        DashboardDTO dashboardDTO = new DashboardDTO();
        StatusDTO statusDTO = new StatusDTO();
        List<Long> userIds = new ArrayList<>();
        if (role.equals("COMPANY_ADMIN")) {
            userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, receuiterId, null);
            userIds.add(receuiterId);
        } else if (role.equals("HR_MANAGER")) {
            userIds.add(receuiterId);
        }
        List<Long> jobPostIdUnsortedList = new ArrayList<>();
        for (Long userId : userIds) {
            List<Long> jobPostIdTempList = jobPostRepository.findJobPostByJobPostIdAndStatus(userId, String.valueOf(userId));
            jobPostIdUnsortedList.addAll(jobPostIdTempList);
        }
        List<Long> jobPostIdList = jobPostIdUnsortedList.stream()
                .distinct()
                .collect(Collectors.toList());

        JobViewResponse jobView = jobPostService.getDashbaordStats(userIds, jobPostIdList);

        OverviewDTO overviewDTO = new OverviewDTO();
        overviewDTO.setActiveJobs((int) jobView.getActiveCount());
        overviewDTO.setClosedJobs((int) jobView.getClosedCount());
        overviewDTO.setPendingJobs((int) jobView.getPendingCount());
        dashboardDTO.setOverview(overviewDTO);
        dashboardDTO.setJobViewResponse(jobView);

        List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findDashboardStats(jobPostIdList);
        int profilesUploaded = jobMatchConsultancies.size();

        int shortlistedCount = 0;
        int interviewedCount = 0;
        int hiredCount = 0;
        int rejectedCount = 0;
        long matchCount = jobMatchConsultancyRepository.findDashboardMatchStats(jobPostIdList);

        Map<TeamProfileDTO, List<JobAppliedFor>> teamProfileMap = new HashMap<>();
        Set<Long> recruiterIds = new HashSet<>();
        for (JobMatchConsultancy jobMatchConsultancy : jobMatchConsultancies) {
            TeamProfileDTO teamProfileDTO = new TeamProfileDTO();
            // User user = userRepository.findById(jobMatchConsultancy.getRecruiterId()).get();
            //teamProfileDTO.setName(user.getFirstName() + " " + user.getLastName());
            //teamProfileDTO.setRecruiterId(jobMatchConsultancy.getRecruiterId());
            //teamProfileDTO.setTotalProfilesUploaded(profilesUploaded);

            if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getApplicantJobStatus() == 0 && jobMatchConsultancy.getApplicantStatus() == 1) {
                shortlistedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                    || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                    || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9
                    || jobMatchConsultancy.getInterviewStatus() == 12) {
                interviewedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                hiredCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                    || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                rejectedCount++;
            }
            if (teamProfileMap.get(teamProfileDTO) == null) {
                teamProfileMap.put(teamProfileDTO, new ArrayList<>());
            }
            recruiterIds.add(jobMatchConsultancy.getRecruiterId());
        }
        statusDTO.setShortlisted(shortlistedCount + matchCount);
        statusDTO.setHired(hiredCount);
        statusDTO.setInterviewed(interviewedCount);
        statusDTO.setMatchCount(matchCount);
        statusDTO.setRejected(rejectedCount);
        dashboardDTO.setStatus(statusDTO);

        AlertsResponse alerts = alertService.alerts(consultancyId, 0, 10, receuiterId);
        dashboardDTO.setAlerts(alerts);

        return dashboardDTO;
    }

    public StatusDTO getStats(Long jobId, List<Long> userIds) {

        List<Integer> interviewStatusList = new ArrayList<Integer>();

        interviewStatusList.add(1);
        interviewStatusList.add(2);
        interviewStatusList.add(10);
        interviewStatusList.add(7);
        interviewStatusList.add(8);
        interviewStatusList.add(11);
        int shortlistedCount = 0;
        int interviewedCount = 0;
        int hiredCount = 0;
        int rejectedCount = 0;

        List<JobMatchStateResultSet> statsList = jobMatchingRepository.getStats(jobId, userIds);
        Long matchCount = jobMatchingRepository.getDashboardStatsJobApplied(jobId, userIds);

        for (JobMatchStateResultSet jobMatchConsultancy : statsList) {
            //jobMatchConsultancy.getHiredStatus() is user job action
            if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getHiredStatus() == 0) {
                shortlistedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                    || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                    || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9
                    || jobMatchConsultancy.getInterviewStatus() == 12) {
                interviewedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                hiredCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                    || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                rejectedCount++;
            }
        }
        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setHired(hiredCount);
        statusDTO.setInterviewed(interviewedCount);
        statusDTO.setRejected(rejectedCount);
        statusDTO.setShortlisted(shortlistedCount + matchCount);
        statusDTO.setMatchCount(matchCount);
        return statusDTO;
    }


    public ConsDashboardStatsDTO dashboardConsStats(long consultancyId, long recruiterId, String role) {
        List<Long> userIds = new ArrayList<>();
        if (role.equals("CONSULTANCY_ADMIN")) {
            userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, recruiterId, null);
            userIds.add(recruiterId);
        } else {
            userIds.add(recruiterId);
        }
        List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findDashboardConsStats(userIds);
        long shortlistedCount = 0;
        long interviewedCount = 0;
        long hiredCount = 0;
        long rejectedCount = 0;
        long matchCount = jobMatchConsultancyRepository.findDashboardMatchConsStats(userIds);

        Map<TeamProfileDTO, List<JobAppliedFor>> teamProfileMap = new HashMap<>();
        ConsDashboardStatsDTO consDashboardStatsDTO = new ConsDashboardStatsDTO();

        Set<Long> recruiterIds = new HashSet<>();
        for (JobMatchConsultancy jobMatchConsultancy : jobMatchConsultancies) {
            TeamProfileDTO teamProfileDTO = new TeamProfileDTO();

            if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getApplicantJobStatus() == 1 && jobMatchConsultancy.getApplicantStatus() == 0) {
                shortlistedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                    || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                    || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9
                    || jobMatchConsultancy.getInterviewStatus() == 12) {
                interviewedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                hiredCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                    || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15
            ) {
                rejectedCount++;
            }
            if (teamProfileMap.get(teamProfileDTO) == null) {
                teamProfileMap.put(teamProfileDTO, new ArrayList<>());
            }
            recruiterIds.add(jobMatchConsultancy.getRecruiterId());
        }
        consDashboardStatsDTO.setAppliedCount(shortlistedCount + matchCount);
        consDashboardStatsDTO.setHiredCount(hiredCount);
        consDashboardStatsDTO.setInterviewCount(interviewedCount);
        consDashboardStatsDTO.setRejectedCount(rejectedCount);
        consDashboardStatsDTO.setSharedCount(shortlistedCount + matchCount + hiredCount + interviewedCount + rejectedCount);
        Long appliedJob = jobMatchingRepository.getDashboardStatsJobApplied(userIds);
        consDashboardStatsDTO.setJobApplied(appliedJob != null ? appliedJob : 0);
        consDashboardStatsDTO.setProfilesUploaded(applicantRepository.countByConsultancyId(userIds));
        AlertsResponse alerts = alertService.alerts(consultancyId, 0, 10, recruiterId);
        consDashboardStatsDTO.setAlerts(alerts);
        return consDashboardStatsDTO;
    }

    public ConsDashboardStatsDTO dashboardConsStats(long jobId, long consultancyId, List<Long> userIds) {


        List<JobMatchConsultancy> jobMatchConsultancies = jobMatchConsultancyRepository.findDashboardConsStats(jobId, userIds);
        long shortlistedCount = 0;
        long interviewedCount = 0;
        long hiredCount = 0;
        long rejectedCount = 0;
        long matchCount = jobMatchConsultancyRepository.findDashboardMatchConsStats(jobId, userIds);

        Map<TeamProfileDTO, List<JobAppliedFor>> teamProfileMap = new HashMap<>();
        ConsDashboardStatsDTO consDashboardStatsDTO = new ConsDashboardStatsDTO();

        Set<Long> recruiterIds = new HashSet<>();
        for (JobMatchConsultancy jobMatchConsultancy : jobMatchConsultancies) {
            TeamProfileDTO teamProfileDTO = new TeamProfileDTO();

            if (jobMatchConsultancy.getInterviewStatus() == 1 && jobMatchConsultancy.getApplicantJobStatus() == 1 && (jobMatchConsultancy.getApplicantStatus() == 0 || jobMatchConsultancy.getApplicantStatus() == 2)) {
                shortlistedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 2 || jobMatchConsultancy.getInterviewStatus() == 3
                    || jobMatchConsultancy.getInterviewStatus() == 4 || jobMatchConsultancy.getInterviewStatus() == 5
                    || jobMatchConsultancy.getInterviewStatus() == 6 || jobMatchConsultancy.getInterviewStatus() == 9
                    || jobMatchConsultancy.getInterviewStatus() == 12) {
                interviewedCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 7) {
                hiredCount++;
            } else if (jobMatchConsultancy.getInterviewStatus() == 8 || jobMatchConsultancy.getInterviewStatus() == 10
                    || jobMatchConsultancy.getInterviewStatus() == 11 || jobMatchConsultancy.getInterviewStatus() == 18 || jobMatchConsultancy.getInterviewStatus() == 15) {
                rejectedCount++;
            }
            if (teamProfileMap.get(teamProfileDTO) == null) {
                teamProfileMap.put(teamProfileDTO, new ArrayList<>());
            }
        }
        consDashboardStatsDTO.setAppliedCount(shortlistedCount + matchCount);
        consDashboardStatsDTO.setRejectedCount(rejectedCount);
        consDashboardStatsDTO.setHiredCount(hiredCount);
        consDashboardStatsDTO.setInterviewCount(interviewedCount);
        consDashboardStatsDTO.setProfilesUploaded(applicantRepository.countByConsultancyId(userIds));
        return consDashboardStatsDTO;

//        if (userIds != null) {
//            ConsDashBoardStatsResultSet dashboardStats = jobMatchingRepository.getDashboardStats(jobId, userIds);
//            //Long appliedJob = jobMatchingRepository.getDashboardStatsJobApplied(userIds);
//            ConsDashboardStatsDTO consDashboardStatsDTO = new ConsDashboardStatsDTO();
//            if (dashboardStats != null) {
//                consDashboardStatsDTO.setAppliedCount(dashboardStats.getApplied());
//                consDashboardStatsDTO.setRejectedCount(dashboardStats.getRejected());
//                consDashboardStatsDTO.setHiredCount(dashboardStats.getHired());
//                consDashboardStatsDTO.setInterviewCount(dashboardStats.getInterview());
//            }
//            //consDashboardStatsDTO.setJobApplied(appliedJob!=null?appliedJob:0);
//            consDashboardStatsDTO.setProfilesUploaded(applicantRepository.countByConsultancyId(consultancyId));
//            return consDashboardStatsDTO;
//        }
    }
}
