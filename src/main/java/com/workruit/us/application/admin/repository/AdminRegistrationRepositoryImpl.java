package com.workruit.us.application.admin.repository;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantStatus;
import com.workruit.us.application.admin.enums.TimePeriod;
import com.workruit.us.application.configuration.WorkruitException;
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
public class AdminRegistrationRepositoryImpl {
    @PersistenceContext
    private EntityManager entityManager;


    public Map<String, Object> getIncompleteCompanyRegistration(IncompleteRegistrationFilter incompleteRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) {
        String selectQuery = "select c.company_id,c.name,c.created_date,u.enabled,c.location,c.headquarters ";
        String countSelect = "select count(c.company_id) ";
        StringBuilder sql = new StringBuilder(" from company c\n" +
                "inner join user u on u.company_id=c.company_id\n" +
                "inner join user_role ur on ur.user_id=u.user_id and ur.role_id=2  " +
                "where (u.enabled = 0 or c.location is null or c.headquarters is null)  ");

        Map<String, Object> params = new HashMap<>();
        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sql.append(" AND DATE(c.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(c.name) LIKE LOWER(:name) ");
            params.put("name", "%" + name + "%");
        }

        if (incompleteRegistrationFilter != null) {
            if (incompleteRegistrationFilter.getLocation() != null && !incompleteRegistrationFilter.getLocation().equals("")) {
                sql.append(" AND LOWER(c.location) = LOWER(:location) ");
                params.put("location", incompleteRegistrationFilter.getLocation());
            }

            if (incompleteRegistrationFilter.getIsRegistrationStep1Completed() != null && !incompleteRegistrationFilter.getIsRegistrationStep1Completed().equals("")) {
                if (incompleteRegistrationFilter.getIsRegistrationStep1Completed().equalsIgnoreCase("yes"))
                    sql.append(" AND c.location is not null ");
                else
                    sql.append(" AND c.location is null ");
            }

            if (incompleteRegistrationFilter.getIsRegistrationStep2Completed() != null && !incompleteRegistrationFilter.getIsRegistrationStep2Completed().equals("")) {
                if (incompleteRegistrationFilter.getIsRegistrationStep2Completed().equalsIgnoreCase("yes"))
                    sql.append("AND c.headquarters is not null ");
                else
                    sql.append("AND c.headquarters is null ");
            }

            if (incompleteRegistrationFilter.getIsEmailVerified() != null && !incompleteRegistrationFilter.getIsEmailVerified().equals("")) {
                sql.append("AND u.enabled = :enabled ");
                params.put("enabled", incompleteRegistrationFilter.getIsEmailVerified().equalsIgnoreCase("yes") ? 1 : 0);
            }
        }


        Query countQuery = entityManager.createNativeQuery(countSelect + sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        BigInteger totalCount = (BigInteger) countQuery.getSingleResult();

        sql.append(" order by c.created_date desc ");
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

    public Map<String, Object> getCompleteCompanyRegistration(CompleteRegistrationFilter completeCompanyRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        String selectQuery = "SELECT\n" +
                "distinct\n" +
                "        c.company_id,\n" +
                "        c.name,\n" +
                "        c.created_date,\n" +
                "        c.location,\n" +
                "        c.industry_types,\n" +
                "        COUNT(CASE WHEN jp.applicant_status = 1 AND jp.interview_status = 0 THEN 1 END) AS shortlisted,\n" +
                "        COUNT(CASE WHEN jp.interview_status IN (2, 3) THEN 1 END) AS underInterview,\n" +
                "        COUNT(CASE WHEN jp.interview_status = 7 THEN 1 END) AS underHire,\n" +
                "        COUNT(CASE WHEN jp.interview_status IN (5, 8, 10) THEN 1 END) AS underReject,\n" +
                "        employeeMembers.registeredEmployeeMember,\n" +
                "        employeeMembers.pendingRegistrationEmployeeMember,\n" +
                "         jobPost.pendingJob,\n" +
                "\t\tjobPost.activeJob,\n" +
                "jobPost.closedJob,\n" +
                "\tjobPost.vacancies,userExp.lastActiveDate";
        String countSelect = "select count(\n" +
                "distinct\n" +
                "c.company_id) ";

        StringBuilder sql = new StringBuilder(" FROM\n" +
                "        company c\n" +
                "    INNER JOIN user u ON u.company_id = c.company_id\n" +
                "    INNER JOIN user_role ur ON ur.user_id = u.user_id\n" +
                "    Left join job_match_consultancy jp On jp.recruiter_id = u.user_id    \n" +
                "    LEFT JOIN\n" +
                "    (SELECT\n" +
                "        COUNT(CASE WHEN enabled = 1 THEN 1 END) AS registeredEmployeeMember,\n" +
                "        COUNT(CASE WHEN enabled = 0 THEN 1 END) AS pendingRegistrationEmployeeMember,\n" +
                "        company_id\n" +
                "    FROM\n" +
                "        user\n" +
                "    Group By\n" +
                "        company_id) AS employeeMembers ON employeeMembers.company_id = u.company_id\n" +
                "        \n" +
                "        Left JOIN\n" +
                "    (SELECT\n" +
                "        COUNT(CASE WHEN jp.status = 'ACTIVE' THEN 1 END) AS pendingJob,\n" +
                "        COUNT(CASE WHEN jp.status = 'PENDING' THEN 1 END) AS activeJob,\n" +
                "        COUNT(CASE WHEN jp.status = 'CLOSED' THEN 1 END) AS closedJob,\n" +
                "        SUM(CASE WHEN jp.status = 'ACTIVE' THEN jp.vacancies ELSE 0 END) AS vacancies,\n" +
                "        usr.company_id\n" +
                "    FROM\n" +
                "        job_post jp\n" +
                "    INNER JOIN user usr ON jp.user_id = usr.user_id\n" +
                "    WHERE usr.enabled = 1 group by usr.company_id ) AS jobPost  ON jobPost.company_id = u.company_id    \n" +
                "Left JOIN (SELECT\n" +
                "   usx.last_active_date as lastActiveDate,\n" +
                "   usr.company_id\n" +
                "   FROM\n" +
                "   user_expired_tokens usx\n" +
                "   INNER JOIN user usr ON usx.user_id = usr.user_id\n" +
                "   ORDER BY lastActiveDate DESC\n" +
                "   LIMIT 1) AS userExp  ON userExp.company_id = u.company_id\n" +
                "    WHERE\n" +
                "     ur.role_id = 2 AND u.enabled = 1 AND c.location IS NOT NULL AND c.headquarters IS NOT NULL ");

        Map<String, Object> params = new HashMap<>();

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sql.append(" AND DATE(c.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(c.name) LIKE LOWER(:name) ");
            params.put("name", "%" + name + "%");
        }
        if (completeCompanyRegistrationFilter != null) {
            if (completeCompanyRegistrationFilter.getLocation() != null && !completeCompanyRegistrationFilter.getLocation().equals("")) {
                sql.append("AND LOWER(c.location) = LOWER(:location) ");
                params.put("location", completeCompanyRegistrationFilter.getLocation());
            }
//
//            if (completeCompanyRegistrationFilter.getIndustryType() != null && !completeCompanyRegistrationFilter.getIndustryType().equals("")) {
//                sql.append(" AND LOWER(c.industry_types) = LOWER(:industryTypes) ");
//                params.put("industryTypes", completeCompanyRegistrationFilter.getIndustryType());
//            }
            if (completeCompanyRegistrationFilter.getIndustryType() != null && !completeCompanyRegistrationFilter.getIndustryType().isEmpty()) {
                List<String> ind_types = completeCompanyRegistrationFilter.getIndustryType();
                for (int i = 0; i < ind_types.size(); i++) {
                    sql.append(" AND (FIND_IN_SET(:industryTypes" + i + ", c.industry_types) > 0) ");
                    params.put("industryTypes" + i, ind_types.get(i));
                }
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        BigInteger totalCount = (BigInteger) countQuery.getSingleResult();

        sql.append(" group by c.company_id,  employeeMembers.registeredEmployeeMember,employeeMembers.pendingRegistrationEmployeeMember,jobPost.pendingJob,jobPost.activeJob,\n" +
                "jobPost.closedJob,\n" +
                "\tjobPost.vacancies,userExp.lastActiveDate  ");
        sql.append(" order by ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sql.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "registration date":
                        sql.append(" c.created_date " + sort.getOrderBy().toString());
                        break;
                    case "no. of registered employer members":
                        sql.append(" employeeMembers.registeredEmployeeMember " + sort.getOrderBy().toString());
                        break;
                    case "no. of pending employer member registrations":
                        sql.append(" employeeMembers.pendingRegistrationEmployeeMember " + sort.getOrderBy().toString());
                        break;
                    case "no. of active jobs":
                        sql.append(" jobPost.activeJob " + sort.getOrderBy().toString());
                        break;
                    case "no. of pending jobs":
                        sql.append(" jobPost.pendingJob " + sort.getOrderBy().toString());
                        break;
                    case "no. of closed jobs":
                        sql.append(" jobPost.closedJob " + sort.getOrderBy().toString());
                        break;
                    case "total no. of vacancies":
                        sql.append(" jobPost.vacancies " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under shortlist":
                        sql.append(" shortlisted " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under interview":
                        sql.append(" underInterview " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under hire":
                        sql.append(" underHire " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under reject":
                        sql.append(" underReject " + sort.getOrderBy().toString());
                        break;
                    case "last active":
                        sql.append(" userExp.lastActiveDate  " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sql.append(" c.created_date desc ");
        }


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

    public List<Object[]> getJobPostCount(Long companyId) {

        String sql = " SELECT\n" +
                "      COUNT(CASE WHEN jp.status = 'ACTIVE' THEN 1 END) AS pendingJob,\n" +
                "      COUNT(CASE WHEN jp.status = 'PENDING' THEN 1 END) AS activeJob,\n" +
                "      COUNT(CASE WHEN jp.status = 'CLOSED' THEN 1 END) AS closedJob,\n" +
                "      SUM(case WHEN jp.status = 'ACTIVE' THEN jp.vacancies ELSE 0 END ) AS vacancies" +
                "    FROM\n" +
                "      job_post jp\n" +
                "    WHERE\n" +
                "      jp.user_id IN (SELECT usr.user_id FROM user usr WHERE usr.company_id= :companyId and usr.enabled=1)  ";


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        return query.getResultList();
    }

    public List<Object[]> getApplicantStatusCount(Long companyId) {

        String sql = " SELECT\n" +
                "      COUNT(CASE WHEN jp.applicant_status = 1 and jp.interview_status=0 THEN 1 END) AS shortlisted,\n" +
                "      COUNT(CASE WHEN jp.interview_status in (2,3) THEN 1 END) AS underInterview,\n" +
                "      COUNT(CASE WHEN jp.interview_status = 7 THEN 1 END) AS underHire,\n" +
                "      COUNT(CASE WHEN jp.interview_status in (5,8,10) THEN 1 END) AS underReject\n" +
                "    FROM\n" +
                "      job_match_consultancy jp\n" +
                "    WHERE\n" +
                "      jp.recruiter_id IN (SELECT usr.user_id FROM user usr WHERE usr.company_id= :companyId and usr.enabled=1) ";


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        return query.getResultList();
    }

    public List<Object[]> getEmployerMemberCount(Long companyId) {

        String sql = "SELECT\n" +
                "      COUNT(CASE WHEN usr.enabled = 1 THEN 1 END) AS registerdEmployeeMember,\n" +
                "      COUNT(CASE WHEN usr.enabled = 0 THEN 1 END) AS pendingRegisterationEmployeeMember\n" +
                "    FROM\n" +
                "      user usr\n" +
                "    WHERE usr.company_id= :companyId ";


        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("companyId", companyId);
        return query.getResultList();
    }


    public Map<String, Object> getHrManagerOfCompany(Long userId, String name, Date from, Date to, int pageNo, int pageSize, TimePeriod period) {

        String selectQuery = "SELECT DISTINCT u";
        String countSql = "SELECT COUNT(DISTINCT u.userId)";
        StringBuilder sqlBuilder = new StringBuilder(
                " FROM User u" +
                        " WHERE u.createdBy = :createdBy");

        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", userId);

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sqlBuilder.append(" AND DATE(u.createdDate) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(u.firstName) LIKE LOWER(:name) OR LOWER(u.lastName) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        sqlBuilder.append(" ORDER BY u.createdDate");
        String queryString = selectQuery + sqlBuilder;

        Query countQuery = entityManager.createQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = (long) countQuery.getSingleResult();

        Query query = entityManager.createQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }


    public Map<String, Object> getCompanyJobs(Long companyId, CompanyJobFilter companyJobFilter, String jobTitle, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {

        String selectQuery = "SELECT DISTINCT jp,\n" +
                "  COUNT(DISTINCT jmc.applicantId) AS relevant,\n" +
                "  COUNT(CASE WHEN jmc.applicantJobStatus = 1 AND jmc.interviewStatus = 0 THEN 1 END) AS interested,\n" +
                "  COUNT(CASE WHEN jmc.applicantStatus = 1 AND jmc.interviewStatus = 0 THEN 1 END) AS shortlisted,\n" +
                "  COUNT(CASE WHEN jmc.interviewStatus IN (2, 3) THEN 1 END) AS underInterview,\n" +
                "  COUNT(CASE WHEN jmc.interviewStatus = 7 THEN 1 END) AS underHire,\n" +
                "  COUNT(CASE WHEN jmc.interviewStatus IN (5, 8, 10) THEN 1 END) AS underReject\n";
        String countSql = "SELECT COUNT(DISTINCT jp.jobPostId) \n";
        StringBuilder sqlBuilder = new StringBuilder(" FROM JobPost jp\n" +
                "LEFT JOIN JobMatchConsultancy jmc ON jmc.jobPostId = jp.jobPostId\n" +
                "WHERE jp.userId IN (SELECT userId FROM User WHERE companyId = :companyId) ");

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sqlBuilder.append(" AND DATE(jp.createdDate) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (jobTitle != null && !jobTitle.isEmpty()) {
            sqlBuilder.append(" AND LOWER(jp.title) LIKE LOWER(:title)");
            params.put("title", "%" + jobTitle + "%");
        }
        if (companyJobFilter != null) {
            if (companyJobFilter.getJobLocation() != null && !companyJobFilter.getJobLocation().isEmpty()) {
                sqlBuilder.append(" AND LOWER(jp.location) = LOWER(:location) ");
                params.put("location", companyJobFilter.getJobLocation());
            }

            if (companyJobFilter.getJobOwner() != null && companyJobFilter.getJobOwner() != 0) {
                sqlBuilder.append(" AND LOWER(jp.userId) = LOWER(:userId) ");
                params.put("userId", companyJobFilter.getJobOwner());
            }

            if (companyJobFilter.getStatus() != null && !companyJobFilter.getStatus().isEmpty()) {
                sqlBuilder.append(" AND LOWER(jp.status) = LOWER(:status) ");
                params.put("status", companyJobFilter.getStatus());
            }
        }

        Query countQuery = entityManager.createQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = (long) countQuery.getSingleResult();
        sqlBuilder.append(" GROUP BY jp.jobPostId ORDER BY ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sqlBuilder.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "no. of interested profiles":
                        sqlBuilder.append(" interested " + sort.getOrderBy().toString());
                        break;
                    case "no. of vacancies":
                        sqlBuilder.append(" jp.vacancies " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under shortlist":
                        sqlBuilder.append(" shortlisted " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under interview":
                        sqlBuilder.append(" underInterview " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under hire":
                        sqlBuilder.append(" underHire " + sort.getOrderBy().toString());
                        break;
                    case "no. of applicants under reject":
                        sqlBuilder.append(" underReject " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" jp.createdDate DESC ");
        }
        String queryString = selectQuery + sqlBuilder;
        Query query = entityManager.createQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getApplicantData(Long jobPostId, JobApplicantFilter jobApplicantFilter, String name, ApplicantStatus applicantStatus, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) throws WorkruitException {

        String selectQuery = "SELECT jc.applicant_id,ap.first_name as apFirstName,ap.last_name as apLastName,c.name,ad.job_function, \n" +
                "ad.secondary_job_function,ap.location,ru.first_name as ruFirstName,ru.last_name as ruLastName,cu.first_name as cuFirstName,cu.last_name as cuLastName,\n" +
                "jc.updated_date,jc.match_score,jc.applicant_status,jc.applicant_job_status,\n" +
                "jc.interview_status ";
        String countSql = "SELECT COUNT(DISTINCT jc.job_match_con_id)";
        StringBuilder sqlBuilder = new StringBuilder(" FROM job_match_consultancy jc\n" +
                "left join applicant ap on ap.applicant_id=jc.applicant_id\n" +
                "left join applicant_details ad on ap.applicant_id=ad.applicant_id\n" +
                "left join consultancy c on c.consultancy_id=jc.consultancy_id\n" +
                "left join user ru on ru.user_id=jc.updated_by_rec_id\n" +
                "left join user cu on cu.user_id=jc.updated_by_cons_user_id\n" +
                "where jc.job_post_id= :jobPostId ");

        Map<String, Object> params = new HashMap<>();
        params.put("jobPostId", jobPostId);
        switch (applicantStatus) {
            case SHORTLISTED:
                sqlBuilder.append(" AND jc.applicant_status=1 and jc.interview_status=0 ");
                if (jobApplicantFilter != null) {
                    if (jobApplicantFilter.getStatus() == 1) {
                        sqlBuilder.append(" AND jc.applicant_job_status=1 ");
                    } else if (jobApplicantFilter.getStatus() == 2) {
                        sqlBuilder.append(" AND jc.applicant_job_status!=1 ");
                    }
                }
                break;
            case UNDER_INTERVIEW:
                if (jobApplicantFilter != null && jobApplicantFilter.getStatus() != 0) {
                    sqlBuilder.append(" AND jc.interview_status in (:interviewStatus) ");
                    params.put("interviewStatus", jobApplicantFilter.getStatus());
                } else {
                    sqlBuilder.append(" AND jc.interview_status in (2,3,4,5,6,9) ");
                }
                break;
            case UNDER_HIRED:
                if (jobApplicantFilter != null && jobApplicantFilter.getStatus() != 0) {
                    sqlBuilder.append(" AND jc.interview_status in (:interviewStatus) ");
                    params.put("interviewStatus", jobApplicantFilter.getStatus());
                } else {
                    sqlBuilder.append(" AND jc.interview_status in (1,7,14,16,17) ");
                }
                break;
            case UNDER_REJECTED:
                if (jobApplicantFilter != null && jobApplicantFilter.getStatus() != 0) {
                    sqlBuilder.append(" AND jc.interview_status in (:interviewStatus) ");
                    params.put("interviewStatus", jobApplicantFilter.getStatus());
                } else {
                    sqlBuilder.append(" AND jc.interview_status in (5,10,15,18) ");
                }
                break;
            default:
                throw new WorkruitException("Invalid applicant status.");
        }

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sqlBuilder.append(" AND DATE(jc.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(ap.first_name) LIKE LOWER(:name) OR LOWER(ap.last_name) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        if (jobApplicantFilter != null) {
            if (jobApplicantFilter.getLocation() != null && !jobApplicantFilter.getLocation().isEmpty()) {
                sqlBuilder.append(" AND LOWER(ap.location) = LOWER(:location) ");
                params.put("location", jobApplicantFilter.getLocation());
            }

            if (jobApplicantFilter.getConsultancy() != null && jobApplicantFilter.getConsultancy() != 0) {
                sqlBuilder.append(" AND jc.consultancy_id = :consultancy  ");
                params.put("consultancy", jobApplicantFilter.getConsultancy());
            }
        }


        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        sqlBuilder.append(" ORDER BY ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sqlBuilder.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "profile uploaded date":
                        sqlBuilder.append(" ap.created_date " + sort.getOrderBy().toString());
                        break;
                    case "date of last status update":
                        sqlBuilder.append(" jc.updated_date " + sort.getOrderBy().toString());
                        break;
                    case "profile match %":
                        sqlBuilder.append(" jc.match_score " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" jc.created_date desc ");
        }
        String queryString = selectQuery + sqlBuilder;
        Query query = entityManager.createNativeQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getCompanyAlertData(long companyId, List<Long> userId, int pageNumber, int pageSize, Date from, Date to, TimePeriod period) {
        String selectQuery = "select a.message,c.name, a.created_date  ";
        String countSelect = "select count(*) ";
        StringBuilder sql = new StringBuilder(" from alert a\n" +
                "left join user u on u.user_id=a.user_id\n" +
                "left join company c on c.company_id=u.company_id\n" +
                "where c.company_id= :companyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("companyId", companyId);

        if (userId != null && !userId.isEmpty() && !userId.contains(0L)) {
            sql.append(" AND a.user_id in (:userId)  ");
            params.put("userId", userId);
        }

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sql.append(" AND DATE(a.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
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

    public Object[] getCompanyJobDetails(long jobPostId) {
        String sql = "SELECT jp.title, GROUP_CONCAT(DISTINCT jf.job_function_name SEPARATOR ', ') AS job_function_names,\n" +
                "    ur.first_name, jp.created_date, jp.description, GROUP_CONCAT(DISTINCT sk.skill_name SEPARATOR ', ') AS skill_names,\n" +
                "    GROUP_CONCAT(DISTINCT jq.question_title SEPARATOR ', ') AS question_titles, jp.collaborator_id, jp.job_apply_by,\n" +
                "    GROUP_CONCAT(DISTINCT d.title SEPARATOR ', ') AS degree_titles\n" +
                "    , jp.job_type, jp.location, jp.experience_max, jp.salary_max, jp.salary_type, jp.currency,\n" +
                "    GROUP_CONCAT(DISTINCT sp.supplemental_pay_name SEPARATOR ', ') AS supplemental_pay_names, jp.vacancies,\n" +
                "    jp.notice_period, c.citizenship_name, GROUP_CONCAT(DISTINCT b.benefit_name SEPARATOR ', ') AS benefit_names,\n" +
                "    e.ethnicity_name, ur.last_name\n" +
                "FROM job_post jp\n" +
                "LEFT JOIN jobpost_jobfunctions jpf ON jpf.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN job_function jf ON jf.job_function_id = jpf.job_function_id\n" +
                "LEFT JOIN job_post_skills jps ON jps.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN skills sk ON sk.skill_id = jps.skill_id\n" +
                "LEFT JOIN job_question jq ON jq.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN job_degrees jd ON jd.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN degrees d ON d.degree_id = jd.degree_id\n" +
                "LEFT JOIN citizenship c ON c.citizenship_id = jp.citizenship_id\n" +
                "LEFT JOIN ethnicity e ON e.ethnicity_id = jp.ethnicity\n" +
                "LEFT JOIN job_supplemental_pay jsp ON jsp.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN supplemental_pay sp ON sp.supplemental_pay_id = jsp.supplemental_pay_id\n" +
                "LEFT JOIN job_post_benefits jpb ON jpb.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN benefits b ON b.benefit_id = jpb.benefit_id\n" +
                "LEFT JOIN user ur ON ur.user_id = jp.user_id\n" +
                "WHERE jp.job_post_id = :jobPostId\n";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("jobPostId", jobPostId);
        List<Object[]> results = query.getResultList();
        return results.get(0);
    }

    public Map<String, Object> getRelevantProfiles(long jobPostId, ApplicantProfileFilterDTO applicantProfileFilterDTO, String name, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {

        String selectQuery = "SELECT a.first_name,a.last_name,c.name,a.created_date,ad.job_function,ad.secondary_job_function,a.location,jmc.match_score,jmc.applicant_id  ";
        String countSelect = "select count(jmc.job_match_con_id) ";
        StringBuilder sqlBuilder = new StringBuilder("  FROM job_match_consultancy jmc\n" +
                "LEFT JOIN applicant a ON a.applicant_id=jmc.applicant_id\n" +
                "LEFT JOIN consultancy c ON c.consultancy_id=a.consultancy_id\n" +
                "LEFT JOIN applicant_details ad ON ad.applicant_id=jmc.applicant_id where jmc.job_post_id = :jobPostId AND jmc.applicant_job_status = 0 and jmc.interview_status=0 AND jmc.applicant_status=0 ");

        Map<String, Object> params = new HashMap<>();
        params.put("jobPostId", jobPostId);

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sqlBuilder.append(" AND DATE(jmc.created_date) BETWEEN :start AND :end");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (applicantProfileFilterDTO != null) {
            if (applicantProfileFilterDTO.getConsultancy() != null && applicantProfileFilterDTO.getConsultancy() != 0) {
                sqlBuilder.append(" AND jmc.consultancy_id = :consultancy  ");
                params.put("consultancy", applicantProfileFilterDTO.getConsultancy());
            }
            if (applicantProfileFilterDTO.getLocation() != null && !applicantProfileFilterDTO.getLocation().equals("")) {
                sqlBuilder.append(" AND LOWER(a.location) = LOWER(:location)");
                params.put("location", applicantProfileFilterDTO.getLocation());
            }
        }
        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(a.first_name) LIKE LOWER(:name) OR LOWER(a.last_name) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        sqlBuilder.append(" ORDER BY ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sqlBuilder.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "profile uploaded date":
                        sqlBuilder.append(" a.created_date " + sort.getOrderBy().toString());
                        break;
                    case "profile match %":
                        sqlBuilder.append(" jmc.match_score " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" jmc.created_date desc ");
        }

        Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());
        return map;
    }

    public Map<String, Object> getInterestedProfiles(long jobPostId, ApplicantProfileFilterDTO applicantProfileFilterDTO, String name, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {

        String selectQuery = "SELECT a.first_name,a.last_name,c.name,a.created_date,ad.job_function,ad.secondary_job_function,a.location,jmc.match_score,jmc.updated_date,jmc.applicant_id  ";
        String countSelect = "select count(jmc.job_match_con_id) ";
        StringBuilder sqlBuilder = new StringBuilder(" FROM job_match_consultancy jmc\n" +
                "LEFT JOIN applicant a ON a.applicant_id=jmc.applicant_id\n" +
                "LEFT JOIN consultancy c ON c.consultancy_id=a.consultancy_id\n" +
                "LEFT JOIN applicant_details ad ON ad.applicant_id=jmc.applicant_id where jmc.job_post_id = :jobPostId AND jmc.applicant_job_status = 1 and jmc.interview_status=0 AND jmc.applicant_status=0");

        Map<String, Object> params = new HashMap<>();
        params.put("jobPostId", jobPostId);

        if (period != null) {
            LocalDate startDate = null;
            LocalDate endDate = null;
            switch (period) {
                case LAST_7_DAYS:
                    startDate = LocalDate.now().minusDays(7);
                    endDate = LocalDate.now();
                    break;
                case LAST_MONTH:
                    startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_QUARTER:
                    startDate = LocalDate.now().minusMonths(4).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                            LocalDate.now().minusMonths(1).lengthOfMonth()
                    );
                    break;
                case LAST_6_MONTHS:
                    startDate = LocalDate.now().minusMonths(6).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case LAST_12_MONTHS:
                    startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
                    endDate = LocalDate.now().withDayOfMonth(
                            LocalDate.now().lengthOfMonth()
                    );
                    break;
                case CUSTOM_DATE:
                    if (from != null && !from.equals("") && to != null && !from.equals("")) {
                        startDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        endDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    break;
            }
            if (startDate != null && endDate != null) {
                Date sqlStartDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date sqlEndDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                sqlBuilder.append(" AND DATE(jmc.updated_date) BETWEEN :start AND :end");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (applicantProfileFilterDTO != null) {
            if (applicantProfileFilterDTO.getConsultancy() != null && applicantProfileFilterDTO.getConsultancy() != 0) {
                sqlBuilder.append(" AND jmc.consultancy_id = :consultancy  ");
                params.put("consultancy", applicantProfileFilterDTO.getConsultancy());
            }
            if (applicantProfileFilterDTO.getLocation() != null && !applicantProfileFilterDTO.getLocation().equals("")) {
                sqlBuilder.append(" AND LOWER(a.location) = LOWER(:location)");
                params.put("location", applicantProfileFilterDTO.getLocation());
            }
        }
        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(a.first_name) LIKE LOWER(:name) OR LOWER(a.last_name) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();
        sqlBuilder.append(" ORDER BY ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sqlBuilder.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "profile uploaded date":
                        sqlBuilder.append(" a.created_date " + sort.getOrderBy().toString());
                        break;
                    case "date marked as interested":
                        sqlBuilder.append(" jmc.updated_date " + sort.getOrderBy().toString());
                        break;
                    case "profile match %":
                        sqlBuilder.append(" jmc.match_score " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" jmc.created_date desc ");
        }
        Query query = entityManager.createNativeQuery(selectQuery + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNo * pageSize).setMaxResults(pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());
        return map;
    }
}
