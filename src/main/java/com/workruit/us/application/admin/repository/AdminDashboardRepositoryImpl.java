package com.workruit.us.application.admin.repository;

import com.workruit.us.application.admin.dto.JobStatsFilterDTO;
import com.workruit.us.application.admin.dto.OverallActivityFilterDTO;
import com.workruit.us.application.admin.dto.PendingJobFilterDTO;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AdminDashboardRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;

    public Object[] getDashBoardStats() {
        String sql = "select sum(consultancy) as consultancy,\n" +
                "sum(company) as company,\n" +
                "sum(consultancyMember) as consultancyMember,\n" +
                "sum(companyMember) as companyMember,\n" +
                "sum(activeJobs) as activeJobs,\n" +
                "sum(pendingJobs) as pendingJobs,\n" +
                "sum(closedJobs) as closedJobs,\n" +
                "sum(applicantsUnderApplied) as applicantsUnderApplied,\n" +
                "sum(applicantUnderRejected) as applicantUnderRejected,\n" +
                "sum(applicantUnderInterview) as applicantUnderInterview,\n" +
                "sum(applicantUnderHired) as applicantUnderHired,\n" +
                "sum(shortListedApplicants) as shortListedApplicants,\n" +
                "sum(uploadedApplicantProfile) as uploadedApplicantProfile\n" +
                "from (SELECT \n" +
                "  COUNT(CASE WHEN ur.role_id = 1 THEN 1 END) AS consultancy,\n" +
                "  COUNT(CASE WHEN ur.role_id = 2 THEN 1 END) AS company,\n" +
                "  COUNT(CASE WHEN ur.role_id = 3 THEN 1 END) AS consultancyMember,\n" +
                "  COUNT(CASE WHEN ur.role_id = 4 THEN 1 END) AS companyMember,\n" +
                "  null as activeJobs,\n" +
                "  null as pendingJobs,\n" +
                "  null as closedJobs,\n" +
                "  null AS applicantsUnderApplied,\n" +
                "  null AS applicantUnderRejected,\n" +
                "  null AS applicantUnderInterview,\n" +
                "  null AS applicantUnderHired,\n" +
                "  null AS shortListedApplicants,\n" +
                "  null as uploadedApplicantProfile\n" +
                "FROM user u \n" +
                "INNER JOIN user_role ur ON u.user_id=ur.user_id \n" +
                "WHERE ur.role_id IN (1, 2, 3, 4)\n" +
                "union all \n" +
                "select null as consultancy,null company,null consultancyMember,null companyMember, \n" +
                "\tCOUNT(CASE WHEN jp.status = 'ACTIVE' THEN 1 END) AS activeJobs,\n" +
                "\tCOUNT(CASE WHEN jp.status = 'PENDING' THEN 1 END) AS pendingJobs,\n" +
                "    COUNT(CASE WHEN jp.status = 'CLOSED' THEN 1 END) AS closedJobs,\n" +
                "    null AS applicantsUnderApplied,\n" +
                "    null AS applicantUnderRejected,\n" +
                "\tnull AS applicantUnderInterview,\n" +
                "    null AS applicantUnderHired,\n" +
                "\tnull AS shortListedApplicants,\n" +
                "    null as uploadedApplicantProfile\n" +
                "from job_post jp\n" +
                "union all \n" +
                "select null as consultancy,null company,null consultancyMember,null companyMember, \n" +
                "\tnull AS activeJobs,\n" +
                "\tnull AS pendingJobs,\n" +
                "    null AS closedJobs,\n" +
                "    COUNT(CASE WHEN jmc.applicant_job_status = 1 THEN 1 END) AS applicantsUnderApplied,\n" +
                "    COUNT(CASE WHEN jmc.interview_status in (5,8,10) THEN 1 END) AS applicantUnderRejected,\n" +
                "\tCOUNT(CASE WHEN jmc.interview_status in (2,3) THEN 1 END) AS applicantUnderInterview,\n" +
                "    COUNT(CASE WHEN jmc.interview_status = 7 THEN 1 END) AS applicantUnderHired,\n" +
                "\tCOUNT(CASE WHEN jmc.applicant_status = 1 THEN 1 END) AS shortListedApplicants,\n" +
                "    null as uploadedApplicantProfile\n" +
                " from job_match_consultancy jmc\n" +
                " union all \n" +
                "select null as consultancy,null company,null consultancyMember,null companyMember, \n" +
                "\tnull AS activeJobs,\n" +
                "\tnull AS pendingJobs,\n" +
                "    null AS closedJobs,\n" +
                "    null AS applicantsUnderApplied,\n" +
                "    null AS applicantUnderRejected,\n" +
                "\tnull AS applicantUnderInterview,\n" +
                "    null AS applicantUnderHired,\n" +
                "\tnull AS shortListedApplicants,\n" +
                "    count(1) as uploadedApplicantProfile\n" +
                "from applicant ap) tbl\n" +
                " ";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results.get(0);
    }

    public List<Object[]> getCompanyRegistrationDataByMonthly(String period, Date from, Date to) {
        String sql = "SELECT \n" +
                "  DATE_FORMAT(u.created_date, '%b-%Y') AS month,\n" +
                "  COUNT(CASE WHEN ur.role_id = 2 THEN 1 END) AS company,\n" +
                "  COUNT(CASE WHEN ur.role_id = 2 and u.enabled=true THEN 1 END) AS emailverified,\n" +
                "  COUNT(CASE WHEN c.location IS NOT NULL THEN 1 END) AS step1,\n" +
                "  COUNT(CASE WHEN c.headquarters IS NOT NULL THEN 1 END) AS step2\n" +
                "FROM user u \n" +
                "INNER JOIN user_role ur ON u.user_id = ur.user_id \n" +
                "LEFT JOIN company c ON c.company_id = u.company_id\n" +
                "WHERE ur.role_id IN (1, 2, 3, 4) AND DATE(u.created_date) BETWEEN :start AND :end " +
                "GROUP BY month \n" +
                "ORDER BY month \n";
        if (period == null || period.equals("")) {
            period = "last 12 month";
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (period.equals("7 day")) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        } else if (period.equals("last month")) {
            startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last quarter")) {
            startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last 6 month")) {
            startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("last 12 month")) {
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("custom date")) {
            startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getCompanyRegistrationData() {
        String sql = "SELECT \n" +
                "  COUNT(CASE WHEN ur.role_id = 2 THEN 1 END) AS company,\n" +
                "  COUNT(CASE WHEN ur.role_id = 2 and u.enabled=true THEN 1 END) AS emailverified,\n" +
                "  COUNT(CASE WHEN c.location IS NOT NULL THEN 1 END) AS step1,\n" +
                "  COUNT(CASE WHEN c.headquarters IS NOT NULL THEN 1 END) AS step2\n" +
                "FROM user u \n" +
                "INNER JOIN user_role ur ON u.user_id = ur.user_id \n" +
                "LEFT JOIN company c ON c.company_id = u.company_id\n" +
                "WHERE ur.role_id IN (1, 2, 3, 4)";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getEmployerRegistrationDataByMonthly(String period, Date from, Date to) {
        String sql = "SELECT \n" +
                "  DATE_FORMAT(u.created_date, '%b-%Y') AS month,\n" +
                "  COUNT(CASE WHEN ur.role_id = 4 THEN 1 END) AS invitesSent,\n" +
                "  COUNT(CASE WHEN ur.role_id = 4 and u.enabled=true THEN 1 END) AS accountActivations\n" +
                "FROM user u \n" +
                "INNER JOIN user_role ur ON u.user_id = ur.user_id \n" +
                "WHERE ur.role_id IN (1, 2, 3, 4) AND DATE(u.created_date) BETWEEN :start AND :end " +
                "GROUP BY month \n" +
                "ORDER BY month \n";
        if (period == null || period.equals("")) {
            period = "last 12 month";
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (period.equals("7 day")) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        } else if (period.equals("last month")) {
            startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last quarter")) {
            startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last 6 month")) {
            startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("last 12 month")) {
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("custom date")) {
            startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getEmployerRegistrationData() {
        String sql = "SELECT \n" +
                "  COUNT(CASE WHEN ur.role_id = 4 THEN 1 END) AS invitesSent,\n" +
                "  COUNT(CASE WHEN ur.role_id = 4 and u.enabled=true THEN 1 END) AS accountActivations\n" +
                "FROM user u \n" +
                "INNER JOIN user_role ur ON u.user_id = ur.user_id \n" +
                "WHERE ur.role_id IN (1, 2, 3, 4)";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getJobData() {
        String sql = "SELECT\n" +
                "COUNT(1) as createdJobs,\n" +
                "COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) AS activeJobs,\n" +
                "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) AS pendingJobs,\n" +
                "COUNT(CASE WHEN status = 'CLOSED' THEN 1 END) AS closedJobs,\n" +
                "SUM(CASE WHEN status = 'ACTIVE' THEN vacancies END) as totalVacancies,\n" +
                "COUNT(CASE WHEN activated_date IS NOT NULL THEN 1 END) AS activatedJobs\n" +
                " FROM job_post;";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getJobDataByMonthlyAndFilter(JobStatsFilterDTO jobStatsFilterDTO, String period, Date from, Date to) {
        StringBuilder sql = new StringBuilder("SELECT month,\n" +
                "SUM(createdJobs) as createdJobs,\n" +
                "SUM(activeJobs) AS activeJobs,\n" +
                "SUM(pendingJobs) AS pendingJobs,\n" +
                "SUM(closedJobs) AS closedJobs,\n" +
                "SUM(totalVacancies) AS totalVacancies,\n" +
                "SUM(activatedJobs) AS activatedJobs\n" +
                "FROM (\n" +
                "SELECT DATE_FORMAT(jp.created_date, '%b-%Y') AS month,\n" +
                "COUNT(1) as createdJobs,\n" +
                "COUNT(CASE when (jp.activated_date is not null and jp.closed_date is not null and DATE_FORMAT(jp.created_date, '%b-%Y') BETWEEN DATE_FORMAT(jp.activated_date, '%b-%Y') AND DATE_FORMAT(jp.closed_date, '%b-%Y')) \n" +
                "or (jp.activated_date is not null and jp.closed_date is null and DATE_FORMAT(jp.activated_date, '%b-%Y') >=DATE_FORMAT(jp.created_date, '%b-%Y'))\n" +
                "THEN 1 END) AS activeJobs,\n" +
                "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) AS pendingJobs,\n" +
                "COUNT(CASE WHEN status = 'CLOSED' THEN 1 END) AS closedJobs,\n" +
                "SUM(CASE WHEN status = 'ACTIVE' THEN vacancies END) as totalVacancies,\n" +
                "0 AS activatedJobs\n" +
                "FROM job_post jp LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jp.job_post_id WHERE DATE(jp.created_date) BETWEEN :start AND :end\n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT DATE_FORMAT(jp.activated_date, '%b-%Y') AS month,\n" +
                "0 as createdJobs,\n" +
                "0 AS activeJobs,\n" +
                "0 AS pendingJobs,\n" +
                "0 AS closedJobs,\n" +
                "0 as totalVacancies,\n" +
                "COUNT(CASE WHEN activated_date IS NOT NULL THEN 1 END) AS activatedJobs\n" +
                "FROM job_post jp LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jp.job_post_id WHERE DATE(jp.activated_date) BETWEEN :start AND :end\n" +
                "filter\n" +
                "GROUP BY month\n" +
                ") td GROUP BY month ORDER BY month;");

        if (period == null || period.equals("")) {
            period = "last 12 month";
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (period.equals("7 day")) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        } else if (period.equals("last month")) {
            startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last quarter")) {
            startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last 6 month")) {
            startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("last 12 month")) {
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("custom date")) {
            startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        StringBuilder filterQuery = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (jobStatsFilterDTO.getLocation() != null && !jobStatsFilterDTO.getLocation().equals("")) {
            filterQuery.append(" AND LOWER(jp.location) = LOWER(:location)");
            params.put("location", jobStatsFilterDTO.getLocation());
        }
        if (jobStatsFilterDTO.getWorkMode() != null && !jobStatsFilterDTO.getWorkMode().equals("")) {
            filterQuery.append(" AND LOWER(jp.workloc_type) = LOWER(:workloc_type)");
            params.put("workloc_type", jobStatsFilterDTO.getWorkMode());
        }
        if (jobStatsFilterDTO.getJobFunction() != null && !jobStatsFilterDTO.getJobFunction().equals("")) {
            filterQuery.append(" AND jpf.job_function_id = :job_function");
            params.put("job_function", jobStatsFilterDTO.getJobFunction());
        }
        if (jobStatsFilterDTO.getJobType() != null && !jobStatsFilterDTO.getJobType().equals("")) {
            filterQuery.append(" AND jp.job_type = :job_type");
            params.put("job_type", jobStatsFilterDTO.getJobType().getValue());
        }


        sql = new StringBuilder(sql.toString().replaceAll("filter", filterQuery.toString()));
        Query query = entityManager.createNativeQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getActivityStatsData() {
        String sql = "SELECT\n" +
                "COUNT(CASE WHEN jmc.applicant_status = 1 AND jmc.recruiter_updated_date IS NOT NULL THEN 1 END) AS shortListedProfile,\n" +
                "COUNT(CASE WHEN jmc.interview_requested_date IS NOT NULL THEN 1 END) AS requestedInterview,\n" +
                "COUNT(CASE WHEN jmc.interview_scheduled_date IS NOT NULL THEN 1 END) AS scheduledInterview,\n" +
                "COUNT(CASE WHEN jmc.rescheduled_request_date IS NOT NULL THEN 1 END) AS rescheduledRequest,\n" +
                "COUNT(CASE WHEN jmc.rescheduled_interview_date IS NOT NULL THEN 1 END) AS rescheduledInterview,\n" +
                "COUNT(CASE WHEN jmc.on_hold_date IS NOT NULL THEN 1 END) AS holdApplicant,\n" +
                "COUNT(CASE WHEN jmc.selected_date IS NOT NULL THEN 1 END) AS selectedApplicant,\n" +
                "COUNT(CASE WHEN jmc.offer_sent_date IS NOT NULL THEN 1 END) AS offerLetterSent,\n" +
                "COUNT(DISTINCT CASE WHEN jmc.offer_sent_date IS NOT NULL THEN od.applicant_id END) AS uniqueOfferLetterSent,\n" +
                "COUNT(CASE WHEN jmc.offer_accepted_date IS NOT NULL THEN 1 END) AS acceptedOfferLetter,\n" +
                "COUNT(DISTINCT CASE WHEN jmc.offer_accepted_date IS NOT NULL THEN od.applicant_id END) AS applAcceptedOfferLetter,\n" +
                "COUNT(CASE WHEN od.joining_date IS NOT NULL THEN 1 END) AS joinedApplicant,\n" +
                "COUNT(CASE WHEN od.offer_status = 5 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS rejectedOfferLetter,\n" +
                "COUNT(DISTINCT CASE WHEN od.offer_status = 5 AND jmc.rejected_date IS NOT NULL THEN od.applicant_id END) AS applRejectedOfferLetter,\n" +
                "COUNT(CASE WHEN jmc.interview_status = 5 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS noShowApplicant,\n" +
                "COUNT(CASE WHEN jmc.interview_status = 10 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS notFitApplicant,\n" +
                "COUNT(CASE WHEN od.offer_status = 6 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS notJoinApplicant\n" +
                " FROM job_match_consultancy jmc LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id;";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getActivityStatsDataByMonthlyAndFilter(JobStatsFilterDTO jobStatsFilterDTO, String period, Date from, Date to) {
        StringBuilder sql = new StringBuilder("SELECT month,\n" +
                "sum(shortListedProfile) as shortListedProfile,\n" +
                "sum(requestedInterview) as  requestedInterview,\n" +
                "sum(scheduledInterview) as  scheduledInterview,\n" +
                "sum(rescheduledRequest) as  rescheduledRequest,\n" +
                "sum(rescheduledInterview) as  rescheduledInterview,\n" +
                "sum(holdApplicant) as  holdApplicant,\n" +
                "sum(selectedApplicant) as  selectedApplicant,\n" +
                "sum(offerLetterSent) as  offerLetterSent,\n" +
                "sum(uniqueOfferLetterSent) as  uniqueOfferLetterSent,\n" +
                "sum(acceptedOfferLetter) as  acceptedOfferLetter,\n" +
                "sum(applAcceptedOfferLetter) as  applAcceptedOfferLetter,\n" +
                "sum(noShowApplicant) as  noShowApplicant,\n" +
                "sum(notFitApplicant) as  notFitApplicant,\n" +
                "sum(rejectedOfferLetter) as  rejectedOfferLetter,\n" +
                "sum(applRejectedOfferLetter) as  applRejectedOfferLetter,\n" +
                "sum(notJoinApplicant) as  notJoinApplicant,\n" +
                "sum(joinedApplicant) as  joinedApplicant from(\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.recruiter_updated_date, '%b-%Y') AS month,\n" +
                "COUNT(CASE WHEN jmc.applicant_status = 1 AND jmc.recruiter_updated_date IS NOT NULL THEN 1 END) AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc\n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.recruiter_updated_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.interview_requested_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "COUNT(CASE WHEN jmc.interview_requested_date IS NOT NULL THEN 1 END) AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc\n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.interview_requested_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.interview_scheduled_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "COUNT(CASE WHEN jmc.interview_scheduled_date IS NOT NULL THEN 1 END) AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.interview_scheduled_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.rescheduled_request_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "COUNT(CASE WHEN jmc.rescheduled_request_date IS NOT NULL THEN 1 END) AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.rescheduled_request_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.rescheduled_interview_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "COUNT(CASE WHEN jmc.rescheduled_interview_date IS NOT NULL THEN 1 END) AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.rescheduled_interview_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.on_hold_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "COUNT(CASE WHEN jmc.on_hold_date IS NOT NULL THEN 1 END) AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.on_hold_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.selected_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "COUNT(CASE WHEN jmc.selected_date IS NOT NULL THEN 1 END) AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.selected_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.offer_sent_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "COUNT(CASE WHEN jmc.offer_sent_date IS NOT NULL THEN 1 END) AS offerLetterSent,\n" +
                "COUNT(DISTINCT CASE WHEN jmc.offer_sent_date IS NOT NULL THEN od.applicant_id END) AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.offer_sent_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.offer_accepted_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "COUNT(CASE WHEN jmc.offer_accepted_date IS NOT NULL THEN 1 END) AS acceptedOfferLetter,\n" +
                "COUNT(DISTINCT CASE WHEN jmc.offer_accepted_date IS NOT NULL THEN od.applicant_id END) AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.offer_accepted_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(jmc.rejected_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "COUNT(CASE WHEN jmc.interview_status = 5 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS noShowApplicant,\n" +
                "COUNT(CASE WHEN jmc.interview_status = 10 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS notFitApplicant,\n" +
                "COUNT(CASE WHEN od.offer_status = 5 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS rejectedOfferLetter,\n" +
                "COUNT(DISTINCT CASE WHEN od.offer_status = 5 AND jmc.rejected_date IS NOT NULL THEN od.applicant_id END) AS applRejectedOfferLetter,\n" +
                "COUNT(CASE WHEN od.offer_status = 6 AND jmc.rejected_date IS NOT NULL THEN 1 END) AS notJoinApplicant,\n" +
                "0 AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(jmc.rejected_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                "\n" +
                "UNION ALL\n" +
                "\n" +
                "SELECT\n" +
                "DATE_FORMAT(od.joining_date, '%b-%Y') AS month,\n" +
                "0 AS shortListedProfile,\n" +
                "0 AS requestedInterview,\n" +
                "0 AS scheduledInterview,\n" +
                "0 AS rescheduledRequest,\n" +
                "0 AS rescheduledInterview,\n" +
                "0 AS holdApplicant,\n" +
                "0 AS selectedApplicant,\n" +
                "0 AS offerLetterSent,\n" +
                "0 AS uniqueOfferLetterSent,\n" +
                "0 AS acceptedOfferLetter,\n" +
                "0 AS applAcceptedOfferLetter,\n" +
                "0 AS noShowApplicant,\n" +
                "0 AS notFitApplicant,\n" +
                "0 AS rejectedOfferLetter,\n" +
                "0 AS applRejectedOfferLetter,\n" +
                "0 AS notJoinApplicant,\n" +
                "COUNT(CASE WHEN od.joining_date IS NOT NULL THEN 1 END)  AS joinedApplicant\n" +
                "FROM job_match_consultancy jmc \n" +
                "LEFT JOIN offer_details od ON jmc.job_post_id=od.job_post_id\n" +
                "LEFT JOIN job_post jp ON jp.job_post_id=jmc.job_post_id \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jmc.job_post_id \n" +
                "where DATE(od.joining_date) BETWEEN :start AND :end \n" +
                "filter\n" +
                "GROUP BY month\n" +
                ") td \n" +
                "GROUP BY month;\n" +
                "\n" +
                "\n");

        if (period == null || period.equals("")) {
            period = "last 12 month";
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (period.equals("7 day")) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        } else if (period.equals("last month")) {
            startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last quarter")) {
            startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                    LocalDate.now().minusMonths(1).lengthOfMonth()
            );
        } else if (period.equals("last 6 month")) {
            startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("last 12 month")) {
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(
                    LocalDate.now().lengthOfMonth()
            );
        } else if (period.equals("custom date")) {
            startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        StringBuilder filterQuery = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (jobStatsFilterDTO.getLocation() != null && !jobStatsFilterDTO.getLocation().equals("")) {
            filterQuery.append(" AND LOWER(jp.location) = LOWER(:location)");
            params.put("location", jobStatsFilterDTO.getLocation());
        }
        if (jobStatsFilterDTO.getWorkMode() != null && !jobStatsFilterDTO.getWorkMode().equals("")) {
            filterQuery.append(" AND LOWER(jp.workloc_type) = LOWER(:workloc_type)");
            params.put("workloc_type", jobStatsFilterDTO.getWorkMode());
        }
        if (jobStatsFilterDTO.getJobFunction() != null && !jobStatsFilterDTO.getJobFunction().equals("")) {
            filterQuery.append(" AND jpf.job_function_id = :job_function");
            params.put("job_function", jobStatsFilterDTO.getJobFunction());
        }
        if (jobStatsFilterDTO.getJobType() != null && !jobStatsFilterDTO.getJobType().equals("")) {
            filterQuery.append(" AND jp.job_type = :job_type");
            params.put("job_type", jobStatsFilterDTO.getJobType().getValue());
        }

        sql = new StringBuilder(sql.toString().replaceAll("filter", filterQuery.toString()));
        Query query = entityManager.createNativeQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setParameter("start", startDate);
        query.setParameter("end", endDate);
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> getPendingJobsData(PendingJobFilterDTO pendingJobFilterDTO, String jobTitle, Date from, Date to) {
        StringBuilder sql = new StringBuilder("SELECT jp.title,jp.location,jf.job_function_name,c.name,jp.created_date,jp.job_apply_by,jp.vacancies,jp.job_post_id FROM job_post jp \n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id=jp.job_post_id\n" +
                "LEFT JOIN job_function jf ON jf.job_function_id=jpf.job_function_id \n" +
                "LEFT JOIN user u ON jp.user_id=u.user_id\n" +
                "LEFT JOIN company c ON c.company_id=u.company_id\n" +
                "WHERE jp.status='PENDING' ");

        Map<String, Object> params = new HashMap<>();

        if (jobTitle != null && !jobTitle.equals("")) {
            sql.append(" AND LOWER(jp.title) LIKE LOWER(:job_title)");
            params.put("job_title", "%" + jobTitle + "%");
        }

        if (from != null && !from.equals("") && to != null && !from.equals("")) {
            sql.append(" AND DATE(jp.created_date) BETWEEN :start AND :end");
            LocalDate startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            params.put("start", startDate);
            params.put("end", endDate);
        }

        if (pendingJobFilterDTO.getLocation() != null && !pendingJobFilterDTO.getLocation().equals("")) {
            sql.append(" AND LOWER(jp.location) = LOWER(:location)");
            params.put("location", pendingJobFilterDTO.getLocation());
        }
        if (pendingJobFilterDTO.getJobFunction() != null && !pendingJobFilterDTO.getJobFunction().equals("")) {
            sql.append(" AND jpf.job_function_id = :job_function");
            params.put("job_function", pendingJobFilterDTO.getJobFunction());
        }
        if (pendingJobFilterDTO.getPostedBy() != null && !pendingJobFilterDTO.getPostedBy().equals("")) {
            sql.append(" AND LOWER(c.name) = LOWER(:name)");
            params.put("name", pendingJobFilterDTO.getPostedBy());
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> results = query.getResultList();
        return results;
    }

    public Map<String, Object> getOverallActivityData(OverallActivityFilterDTO overallActivityFilterDTO, String name, Date from, Date to, int pageNumber, int pageSize) {
        String selectQuery = "select jp.title as jobTitle, jp.created_date as jobPostedDate, com.name as companyName,a.first_name as applicantFirstName,\n" +
                " a.last_name as applicantLastName,a.location as applicantLocation,con.name as consultancyName,j.applicant_status as applicantStatus,j.applicant_job_status as applicantJobStatus,\n" +
                " j.interview_status as interviewStatus,consultancyuser.first_name as consultancyUserFirstName,consultancyuser.last_name as consultancyUserLastName,\n" +
                " recruteruser.first_name as recruterUserFirstName, recruteruser.last_name as recruterUserLastName,j.updated_date as dateOfLastStatusUpdate, j.match_score as profileMatchScore \n";
        String countSelect = "select count(*) ";
        StringBuilder sql = new StringBuilder(" from job_match_consultancy j\n" +
                "left join applicant a on a.applicant_id=j.applicant_id\n" +
                "left join job_post jp on jp.job_post_id=j.job_post_id\n" +
                "left join user juser on juser.user_id=jp.user_id\n" +
                "left join company com on com.company_id=juser.company_id\n" +
                "left join consultancy con on con.consultancy_id=j.consultancy_id\n" +
                "left join user consultancyuser on consultancyuser.user_id=j.consultancy_user_id\n" +
                "left join user recruteruser on recruteruser.user_id=j.recruiter_id\n" +
                "where (j.applicant_status = 1 or j.applicant_job_status=1) \n");

        Map<String, Object> params = new HashMap<>();

        if (from != null && !from.equals("") && to != null && !from.equals("")) {
            sql.append(" AND DATE(j.updated_date) BETWEEN :start AND :end");
            LocalDate startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            params.put("start", startDate);
            params.put("end", endDate);
        }

        if (overallActivityFilterDTO.getConsultancyName() != null && !overallActivityFilterDTO.getConsultancyName().isEmpty()) {
            sql.append(" AND LOWER(con.name) LIKE LOWER(:consultancyName)");
            params.put("consultancyName", "%" + overallActivityFilterDTO.getConsultancyName() + "%");
        }

        if (overallActivityFilterDTO.getCompanyName() != null && !overallActivityFilterDTO.getCompanyName().isEmpty()) {
            sql.append(" AND LOWER(com.name) LIKE LOWER(:companyName)");
            params.put("companyName", "%" + overallActivityFilterDTO.getCompanyName() + "%");
        }

        if (overallActivityFilterDTO.getJobTitle() != null && !overallActivityFilterDTO.getJobTitle().isEmpty()) {
            sql.append(" AND LOWER(jp.title) LIKE LOWER(:jobTitle)");
            params.put("jobTitle", "%" + overallActivityFilterDTO.getJobTitle() + "%");
        }
        if (name != null && !name.isEmpty()) {
            sql.append(" AND (LOWER(a.first_name) LIKE LOWER(:name) OR LOWER(a.last_name) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        BigInteger totalCount = (BigInteger) countQuery.getSingleResult();

        sql.append(" order by j.updated_date desc");
        Query query = entityManager.createNativeQuery(selectQuery + sql).setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> results = query.getResultList();
        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", results);
        return map;
    }

    public Map<String, Object> getOverallAlertData(String name, Date from, Date to, int pageNumber, int pageSize) {
        String selectQuery = "select a.message,c.name, a.created_date,a.alert_id  ";
        String countSelect = "select count(*) ";
        StringBuilder sql = new StringBuilder(" from alert a\n" +
                "left join user u on u.user_id=a.user_id\n" +
                "left join company c on c.company_id=u.company_id\n" +
                "where 1=1 ");

        Map<String, Object> params = new HashMap<>();

        if (from != null && !from.equals("") && to != null && !from.equals("")) {
            sql.append(" AND DATE(a.created_date) BETWEEN :start AND :end");
            LocalDate startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            params.put("start", startDate);
            params.put("end", endDate);
        }

        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(c.name) LIKE LOWER(:name) ");
            params.put("name", "%" + name + "%");
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        BigInteger totalCount = (BigInteger) countQuery.getSingleResult();

        sql.append(" order by a.created_date desc");
        Query query = entityManager.createNativeQuery(selectQuery + sql).setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Object[]> results = query.getResultList();
        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", results);
        return map;
    }
}
