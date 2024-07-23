package com.workruit.us.application.admin.service;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.repository.AdminDashboardRepositoryImpl;
import com.workruit.us.application.enums.InterviewStatus;
import com.workruit.us.application.models.Company;
import com.workruit.us.application.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private AdminDashboardRepositoryImpl adminDashboardRepository;

    @Autowired
    private CompanyRepository companyRepository;


    public DashboardStatsDTO getDashBoardStats() {
        Object[] result = adminDashboardRepository.getDashBoardStats();
        EmployerStatsDTO employerStatsDTO = new EmployerStatsDTO();
        ConsultancyStatsDTO consultancyStatsDTO = new ConsultancyStatsDTO();
        if (result != null) {
            consultancyStatsDTO.setConsultancies(((BigDecimal) result[0]).longValue());
            employerStatsDTO.setCompanies(((BigDecimal) result[1]).longValue());
            consultancyStatsDTO.setConsultancyMembers(((BigDecimal) result[2]).longValue());
            employerStatsDTO.setEmployerMembers(((BigDecimal) result[3]).longValue());

            employerStatsDTO.setActiveJobs(((BigDecimal) result[4]).longValue());
            employerStatsDTO.setPendingJobs(((BigDecimal) result[5]).longValue());
            employerStatsDTO.setClosedJobs(((BigDecimal) result[6]).longValue());


            consultancyStatsDTO.setApplicantsUnderApplied(((BigDecimal) result[7]).longValue());
            consultancyStatsDTO.setApplicantUnderRejected(((BigDecimal) result[8]).longValue());
            consultancyStatsDTO.setApplicantUnderInterview(((BigDecimal) result[9]).longValue());
            consultancyStatsDTO.setApplicantUnderHired(((BigDecimal) result[10]).longValue());

            employerStatsDTO.setApplicantUnderRejected(((BigDecimal) result[8]).longValue());
            employerStatsDTO.setApplicantUnderInterview(((BigDecimal) result[9]).longValue());
            employerStatsDTO.setApplicantUnderHired(((BigDecimal) result[10]).longValue());
            employerStatsDTO.setShortListedApplicants(((BigDecimal) result[11]).longValue());

            consultancyStatsDTO.setUploadedApplicantProfile(((BigDecimal) result[12]).longValue());

            DashboardStatsDTO dashboardStatsDTO = new DashboardStatsDTO();
            dashboardStatsDTO.setConsultancy(consultancyStatsDTO);
            dashboardStatsDTO.setEmployer(employerStatsDTO);
            return dashboardStatsDTO;
        }
        return null;
    }

    public List<CompanyRegistrationMonthlyDTO> getCompanyRegistrationDataByMonthly(String period, Date from, Date to) {
        List<Object[]> result = adminDashboardRepository.getCompanyRegistrationDataByMonthly(period, from, to);
        List<CompanyRegistrationMonthlyDTO> companyRegistrationMonthlyDTOS = new ArrayList<>();
        for (Object[] row : result) {
            CompanyRegistrationMonthlyDTO companyRegistrationMonthlyDTO = new CompanyRegistrationMonthlyDTO();
            companyRegistrationMonthlyDTO.setMonth(row[0].toString());
            companyRegistrationMonthlyDTO.setCompanySignups(((BigInteger) row[1]).longValue());
            companyRegistrationMonthlyDTO.setEmailVerified(((BigInteger) row[2]).longValue());
            companyRegistrationMonthlyDTO.setStep1RegCompleted(((BigInteger) row[3]).longValue());
            companyRegistrationMonthlyDTO.setStep2RegCompleted(((BigInteger) row[4]).longValue());
            companyRegistrationMonthlyDTOS.add(companyRegistrationMonthlyDTO);
        }
        return companyRegistrationMonthlyDTOS;
    }

    public List<CompanyRegistrationStatsDTO> getCompanyRegistrationData() {
        List<Object[]> result = adminDashboardRepository.getCompanyRegistrationData();
        List<CompanyRegistrationStatsDTO> companyRegistrationDTOS = new ArrayList<>();
        for (Object[] row : result) {
            CompanyRegistrationStatsDTO companyRegistrationDTO = new CompanyRegistrationStatsDTO();
            companyRegistrationDTO.setCompanySignups(((BigInteger) row[0]).longValue());
            companyRegistrationDTO.setEmailVerified(((BigInteger) row[1]).longValue());
            companyRegistrationDTO.setStep1RegCompleted(((BigInteger) row[2]).longValue());
            companyRegistrationDTO.setStep2RegCompleted(((BigInteger) row[3]).longValue());
            companyRegistrationDTOS.add(companyRegistrationDTO);
        }
        return companyRegistrationDTOS;
    }

    public List<EmployerRegistrationMonthlyDTO> getEmployerRegistrationDataByMonthly(String period, Date from, Date to) {
        List<Object[]> result = adminDashboardRepository.getEmployerRegistrationDataByMonthly(period, from, to);
        List<EmployerRegistrationMonthlyDTO> employerRegistrationMonthlyDTOS = new ArrayList<>();
        for (Object[] row : result) {
            EmployerRegistrationMonthlyDTO employerRegistrationMonthlyDTO = new EmployerRegistrationMonthlyDTO();
            employerRegistrationMonthlyDTO.setMonth(row[0].toString());
            employerRegistrationMonthlyDTO.setInvitesSent(((BigInteger) row[1]).longValue());
            employerRegistrationMonthlyDTO.setAccountActivations(((BigInteger) row[2]).longValue());
            employerRegistrationMonthlyDTOS.add(employerRegistrationMonthlyDTO);
        }
        return employerRegistrationMonthlyDTOS;
    }

    public List<EmployerRegistrationStatsDTO> getEmployerRegistrationData() {
        List<Object[]> result = adminDashboardRepository.getEmployerRegistrationData();
        List<EmployerRegistrationStatsDTO> employerRegistrationStatsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            EmployerRegistrationStatsDTO employerRegistrationStatsDTO = new EmployerRegistrationStatsDTO();
            employerRegistrationStatsDTO.setInvitesSent(((BigInteger) row[0]).longValue());
            employerRegistrationStatsDTO.setAccountActivations(((BigInteger) row[1]).longValue());
            employerRegistrationStatsDTOS.add(employerRegistrationStatsDTO);
        }
        return employerRegistrationStatsDTOS;
    }

    public List<JobStatsDTO> getJobData() {
        List<Object[]> result = adminDashboardRepository.getJobData();
        List<JobStatsDTO> jobStatsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            JobStatsDTO jobStatsDTO = new JobStatsDTO();
            jobStatsDTO.setCreatedJobs(((BigInteger) row[0]).longValue());
            jobStatsDTO.setActiveJobs(((BigInteger) row[1]).longValue());
            jobStatsDTO.setPendingJobs(((BigInteger) row[2]).longValue());
            jobStatsDTO.setClosedJobs(((BigInteger) row[3]).longValue());
            jobStatsDTO.setTotalVacancies(((BigDecimal) row[4]).longValue());
            jobStatsDTO.setActivatedJobs(((BigInteger) row[5]).longValue());
            jobStatsDTOS.add(jobStatsDTO);
        }
        return jobStatsDTOS;
    }

    public List<JobMonthlyStatsDTO> getJobDataByMonthlyAndFilter(JobStatsFilterDTO jobStatsFilterDTO, String period, Date from, Date to) {
        List<Object[]> result = adminDashboardRepository.getJobDataByMonthlyAndFilter(jobStatsFilterDTO, period, from, to);
        List<JobMonthlyStatsDTO> jobMonthlyStatsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            JobMonthlyStatsDTO jobMonthlyStatsDTO = new JobMonthlyStatsDTO();
            jobMonthlyStatsDTO.setMonth(row[0].toString());
            jobMonthlyStatsDTO.setCreatedJobs(((BigDecimal) row[1]).longValue());
            jobMonthlyStatsDTO.setActiveJobs(((BigDecimal) row[2]).longValue());
            jobMonthlyStatsDTO.setPendingJobs(((BigDecimal) row[3]).longValue());
            jobMonthlyStatsDTO.setClosedJobs(((BigDecimal) row[4]).longValue());
            jobMonthlyStatsDTO.setTotalVacancies(((BigDecimal) row[5]).longValue());
            jobMonthlyStatsDTO.setActivatedJobs(((BigDecimal) row[6]).longValue());
            jobMonthlyStatsDTOS.add(jobMonthlyStatsDTO);
        }
        return jobMonthlyStatsDTOS;
    }

    public List<ActivityStatsDTO> getActivityStatsData() {
        List<Object[]> result = adminDashboardRepository.getActivityStatsData();
        List<ActivityStatsDTO> activityStatsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            ActivityStatsDTO activityStatsDTO = new ActivityStatsDTO();
            activityStatsDTO.setShortListedProfile(((BigInteger) row[0]).longValue());
            activityStatsDTO.setRequestedInterview(((BigInteger) row[1]).longValue());
            activityStatsDTO.setScheduledInterview(((BigInteger) row[2]).longValue());
            activityStatsDTO.setRescheduledRequest(((BigInteger) row[3]).longValue());
            activityStatsDTO.setRescheduledInterview(((BigInteger) row[4]).longValue());
            activityStatsDTO.setHoldApplicant(((BigInteger) row[5]).longValue());
            activityStatsDTO.setSelectedApplicant(((BigInteger) row[6]).longValue());
            activityStatsDTO.setOfferLetterSent(((BigInteger) row[7]).longValue());
            activityStatsDTO.setUniqueOfferLetterSent(((BigInteger) row[8]).longValue());
            activityStatsDTO.setAcceptedOfferLetter(((BigInteger) row[9]).longValue());
            activityStatsDTO.setApplicantAcceptedOfferLetter(((BigInteger) row[10]).longValue());
            activityStatsDTO.setJoinedApplicant(((BigInteger) row[11]).longValue());
            activityStatsDTO.setRejectedOfferLetter(((BigInteger) row[12]).longValue());
            activityStatsDTO.setApplicantRejectedOfferLetter(((BigInteger) row[13]).longValue());
            activityStatsDTO.setNoShowApplicant(((BigInteger) row[14]).longValue());
            activityStatsDTO.setNotFitApplicant(((BigInteger) row[15]).longValue());
            activityStatsDTO.setNotJoinApplicant(((BigInteger) row[16]).longValue());
            activityStatsDTOS.add(activityStatsDTO);
        }
        return activityStatsDTOS;
    }

    public List<ActivityMonthlyStatsDTO> getActivityStatsDataByMonthlyAndFilter(JobStatsFilterDTO jobStatsFilterDTO, String period, Date from, Date to) {
        List<Object[]> result = adminDashboardRepository.getActivityStatsDataByMonthlyAndFilter(jobStatsFilterDTO, period, from, to);
        List<ActivityMonthlyStatsDTO> activityStatsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            ActivityMonthlyStatsDTO activityStatsDTO = new ActivityMonthlyStatsDTO();
            activityStatsDTO.setMonth(row[0].toString());
            activityStatsDTO.setShortListedProfile(((BigDecimal) row[1]).longValue());
            activityStatsDTO.setRequestedInterview(((BigDecimal) row[2]).longValue());
            activityStatsDTO.setScheduledInterview(((BigDecimal) row[3]).longValue());
            activityStatsDTO.setRescheduledRequest(((BigDecimal) row[4]).longValue());
            activityStatsDTO.setRescheduledInterview(((BigDecimal) row[5]).longValue());
            activityStatsDTO.setHoldApplicant(((BigDecimal) row[6]).longValue());
            activityStatsDTO.setSelectedApplicant(((BigDecimal) row[7]).longValue());
            activityStatsDTO.setOfferLetterSent(((BigDecimal) row[8]).longValue());
            activityStatsDTO.setUniqueOfferLetterSent(((BigDecimal) row[9]).longValue());
            activityStatsDTO.setAcceptedOfferLetter(((BigDecimal) row[10]).longValue());
            activityStatsDTO.setApplicantAcceptedOfferLetter(((BigDecimal) row[11]).longValue());
            activityStatsDTO.setNoShowApplicant(((BigDecimal) row[12]).longValue());
            activityStatsDTO.setNotFitApplicant(((BigDecimal) row[13]).longValue());
            activityStatsDTO.setRejectedOfferLetter(((BigDecimal) row[14]).longValue());
            activityStatsDTO.setApplicantRejectedOfferLetter(((BigDecimal) row[15]).longValue());
            activityStatsDTO.setNotJoinApplicant(((BigDecimal) row[16]).longValue());
            activityStatsDTO.setJoinedApplicant(((BigDecimal) row[17]).longValue());
            activityStatsDTOS.add(activityStatsDTO);
        }
        return activityStatsDTOS;
    }

    public List<PendingJobsDTO> getPendingJobsData(PendingJobFilterDTO pendingJobFilterDTO, String jobTitle, Date from, Date to) {
        List<Object[]> result = adminDashboardRepository.getPendingJobsData(pendingJobFilterDTO, jobTitle, from, to);
        List<PendingJobsDTO> pendingJobsDTOS = new ArrayList<>();
        for (Object[] row : result) {
            PendingJobsDTO pendingJobsDTO = new PendingJobsDTO();
            pendingJobsDTO.setJobTitle((row[0]).toString());
            pendingJobsDTO.setJobLocation((row[1]).toString());
            pendingJobsDTO.setJobFunction((row[2]).toString());
            pendingJobsDTO.setPostedBy((row[3]).toString());
            pendingJobsDTO.setPostedDate(((Date) row[4]));
            pendingJobsDTO.setApplyBy(((Date) row[5]));
            pendingJobsDTO.setNoOfVacancies(((BigInteger) row[6]).longValue());
            pendingJobsDTO.setJobPostId(((BigInteger) row[7]).longValue());
            pendingJobsDTOS.add(pendingJobsDTO);
        }
        return pendingJobsDTOS;
    }

    public PaginationResponse getOverallActivityData(OverallActivityFilterDTO overallActivityFilterDTO, String name, Date from, Date to, int pageNumber, int pageSize) {

        Map<String, Object> result = adminDashboardRepository.getOverallActivityData(overallActivityFilterDTO, name, from, to, pageNumber, pageSize);
        List<Object[]> data = (List<Object[]>) result.get("data");
        List<OverallActivityDTO> overallActivityDTOS = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
        // 0 : title, 1: jobPostedDate, 2:companyName, 3:applicantFirstName,4:applicantLastName
        // 5:applicantLocation, 6:consultancyName, 7:applicantStatus,8:applicantJobStatus
        // 9:interviewStatus, 10:consultancyUserFirstName,11:consultancyUserLastName
        // 12:recruterUserFirstName, 13:recruterUserLastName,14:dateOfLastStatusUpdate
        // 15:profileMatchScore

        for (Object[] row : data) {
            OverallActivityDTO overallActivityDTO = new OverallActivityDTO();
            overallActivityDTO.setJobTitle((String) row[0]);
            if (row[1] != null)
                overallActivityDTO.setJobPostedDate(sdf.format(((Date) row[1])));
            overallActivityDTO.setCompanyName((String) row[2]);
            overallActivityDTO.setApplicantFirstName((String) row[3]);

            overallActivityDTO.setApplicantLastName((String) row[4]);
            overallActivityDTO.setApplicantLocation((String) row[5]);
            overallActivityDTO.setConsultancyName((String) row[6]);

            int applicantStatus = (int) row[7];
            int applicantJobStatus = (int) row[8];
            int interviewStatus = ((BigInteger) row[9]).intValue();

            if (interviewStatus == 0 && applicantJobStatus == 1 && applicantStatus == 1) {
                overallActivityDTO.setApplicantStatus("Matched");
            } else if (interviewStatus == 0 && applicantJobStatus == 0 && applicantStatus == 1) {
                overallActivityDTO.setApplicantStatus("Shortlisted");
            } else if (interviewStatus == 0 && applicantJobStatus == 1 && applicantStatus == 0) {
                overallActivityDTO.setApplicantStatus("Applied");
            } else if (interviewStatus != 0) {
                overallActivityDTO.setApplicantStatus(InterviewStatus.getByValue(interviewStatus).toString());
            }
            String updatedName = "";
            if (row[10] != null) {

                if (row[11] != null)
                    updatedName += row[10] + " " + row[11];
                else
                    updatedName += (String) row[10];
            }

            if (row[12] != null) {
                if (!updatedName.equals("")) {
                    updatedName += ",";
                }
                if (row[13] != null)
                    updatedName += row[12] + " " + row[13];
                else
                    updatedName += (String) row[12];
            }
            overallActivityDTO.setStatusUpdatedBy(updatedName);
            if (row[14] != null)
                overallActivityDTO.setDateOfLastStatusUpdate(sdf.format(((Date) row[14])));
            overallActivityDTO.setProfileMatchScore((Integer) row[15]);
            overallActivityDTOS.add(overallActivityDTO);
        }
        PaginationResponse overallActivityResponse = new PaginationResponse();
        long totalCount = ((BigInteger) result.get("count")).longValue();
        overallActivityResponse.setTotalCount(totalCount);
        overallActivityResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        overallActivityResponse.setData(overallActivityDTOS);
        return overallActivityResponse;
    }

    public PaginationResponse getOverallAlertData(String name, Date from, Date to, int pageNumber, int pageSize) {
        Map<String, Object> result = adminDashboardRepository.getOverallAlertData(name, from, to, pageNumber, pageSize);
        List<Object[]> data = (List<Object[]>) result.get("data");
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a | dd MMM yyyy");
        PaginationResponse overallAlertResponse = new PaginationResponse();
        List<OverallAlertDTO> overallAlertDTOS = new ArrayList<>();
        for (Object[] row : data) {
            OverallAlertDTO overallAlertDTO = new OverallAlertDTO();
            overallAlertDTO.setMsg((row[0]).toString());
            overallAlertDTO.setCompanyName((row[1]).toString());
            overallAlertDTO.setAlertDate(sdf.format(((Date) row[2])));
            overallAlertDTO.setId(((BigInteger) row[3]).longValue());
            overallAlertDTOS.add(overallAlertDTO);
        }
        overallAlertResponse.setData(overallAlertDTOS);
        long totalCount = ((BigInteger) result.get("count")).longValue();
        overallAlertResponse.setTotalCount(totalCount);
        overallAlertResponse.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return overallAlertResponse;
    }

    public List<String> getCompanyNameOnSearch(String name) {
        return companyRepository.getCompanyNameOnSearch(name != null ? name : "");
    }
}
