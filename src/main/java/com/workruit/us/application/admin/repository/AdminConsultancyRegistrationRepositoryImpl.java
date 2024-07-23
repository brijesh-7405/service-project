package com.workruit.us.application.admin.repository;

import com.workruit.us.application.admin.dto.*;
import com.workruit.us.application.admin.enums.ApplicantJobs;
import com.workruit.us.application.admin.enums.ApplicantProfileStatus;
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
public class AdminConsultancyRegistrationRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> getIncompleteConsultancyRegistration(IncompleteRegistrationFilter incompleteRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, TimePeriod period) {
        String selectQuery = "select c.consultancy_id,c.name,c.created_date,u.enabled,c.location,c.domains ";
        String countSelect = "select count(c.consultancy_id) ";
        StringBuilder sql = new StringBuilder("from consultancy c\n" +
                "inner join user u on u.consultancy_id=c.consultancy_id\n" +
                "inner join user_role ur on ur.user_id=u.user_id and ur.role_id IN (1,2) \n" +
                "where (u.enabled = 0 or c.location is null or c.domains is null) ");

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
                sql.append("AND LOWER(c.location) = LOWER(:location) ");
                params.put("location", incompleteRegistrationFilter.getLocation());
            }

            if (incompleteRegistrationFilter.getIsRegistrationStep1Completed() != null && !incompleteRegistrationFilter.getIsRegistrationStep1Completed().equals("")) {
                if (incompleteRegistrationFilter.getIsRegistrationStep1Completed().equalsIgnoreCase("yes"))
                    sql.append("AND c.location is not null ");
                else
                    sql.append("AND c.location is null ");
            }

            if (incompleteRegistrationFilter.getIsRegistrationStep2Completed() != null && !incompleteRegistrationFilter.getIsRegistrationStep2Completed().equals("")) {
                if (incompleteRegistrationFilter.getIsRegistrationStep2Completed().equalsIgnoreCase("yes"))
                    sql.append("AND c.domains is not null ");
                else
                    sql.append("AND c.domains is null ");
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

    public Map<String, Object> getCompleteConsultancyRegistration(CompleteRegistrationFilter completeRegistrationFilter, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period) {
        String selectQuery = "SELECT\n" +
                "distinct\n" +
                "        c.consultancy_id,\n" +
                "        c.name,\n" +
                "        c.created_date,\n" +
                "        c.location,\n" +
                "        c.industry_types,\n" +
                "        c.domains,\n" +
                "        COUNT(CASE WHEN jp.applicant_status = 1 AND jp.interview_status = 0 THEN 1 END) AS shortlisted,\n" +
                "        COUNT(CASE WHEN jp.interview_status IN (2, 3) THEN 1 END) AS underInterview,\n" +
                "        COUNT(CASE WHEN jp.interview_status = 7 THEN 1 END) AS underHire,\n" +
                "        COUNT(CASE WHEN jp.interview_status IN (5, 8, 10) THEN 1 END) AS underReject,\n" +
                "        consultancyMembers.registeredConsultancyMember,\n" +
                "        consultancyMembers.pendingRegistrationConsultancyMember,\n" +
                "        applicants.uploadedProfiles,userExp.lastActiveDate ";
        String countSelect = "select count(\n" +
                "distinct\n" +
                "c.consultancy_id) ";

        StringBuilder sql = new StringBuilder(" FROM\n" +
                "        consultancy c\n" +
                "    INNER JOIN user u ON u.consultancy_id = c.consultancy_id\n" +
                "    INNER JOIN user_role ur ON ur.user_id = u.user_id\n" +
                "    Left join job_match_consultancy jp On jp.consultancy_user_id = u.user_id    \n" +
                "    LEFT JOIN\n" +
                "    (SELECT\n" +
                "        COUNT(CASE WHEN enabled = 1 THEN 1 END) AS registeredConsultancyMember,\n" +
                "        COUNT(CASE WHEN enabled = 0 THEN 1 END) AS pendingRegistrationConsultancyMember,\n" +
                "        consultancy_id\n" +
                "    FROM\n" +
                "        user\n" +
                "    Group By\n" +
                "        consultancy_id) AS consultancyMembers ON consultancyMembers.consultancy_id = u.consultancy_id \n" +
                "\tLEFT JOIN\n" +
                "    (\n" +
                "    SELECT\n" +
                "        COUNT(applicant_id) AS uploadedProfiles,\n" +
                "        consultancy_id\n" +
                "    FROM\n" +
                "        applicant\n" +
                "    Group By\n" +
                "        consultancy_id\n" +
                "    ) AS applicants ON applicants.consultancy_id = u.consultancy_id \n" +
                "Left JOIN (SELECT\n" +
                "   usx.last_active_date as lastActiveDate,\n" +
                "   usr.consultancy_id as consultancyId\n" +
                "   FROM\n" +
                "   user_expired_tokens usx\n" +
                "   INNER JOIN user usr ON usx.user_id = usr.user_id\n" +
                "   ORDER BY lastActiveDate DESC\n" +
                "   LIMIT 1) AS userExp  ON userExp.consultancyId =u.consultancy_id\n" +
                "    WHERE\n" +
                "        ur.role_id IN (1,2) AND u.enabled = 1 AND c.location IS NOT NULL AND c.industry_types IS NOT NULL ");

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
        if (completeRegistrationFilter != null) {
            if (completeRegistrationFilter.getLocation() != null && !completeRegistrationFilter.getLocation().equals("")) {
                sql.append("AND LOWER(c.location) = LOWER(:location) ");
                params.put("location", completeRegistrationFilter.getLocation());
            }

            if (completeRegistrationFilter.getIndustryType() != null && !completeRegistrationFilter.getIndustryType().isEmpty()) {
                List<String> ind_types = completeRegistrationFilter.getIndustryType();
                for (int i = 0; i < ind_types.size(); i++) {
                    sql.append(" AND (FIND_IN_SET(:industryTypes" + i + ", c.industry_types) > 0) ");
                    params.put("industryTypes" + i, ind_types.get(i));
                }
            }

            if (completeRegistrationFilter.getDomainSpecialization() != null && !completeRegistrationFilter.getDomainSpecialization().isEmpty()) {
                List<String> domain = completeRegistrationFilter.getDomainSpecialization();
                for (int i = 0; i < domain.size(); i++) {
                    sql.append(" AND (FIND_IN_SET(:domain" + i + ", c.domains) > 0) ");
                    params.put("domain" + i, domain.get(i));
                }
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSelect + sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        BigInteger totalCount = (BigInteger) countQuery.getSingleResult();

        sql.append(" group by c.consultancy_id,  consultancyMembers.registeredConsultancyMember,\n" +
                " consultancyMembers.pendingRegistrationConsultancyMember,applicants.uploadedProfiles,userExp.lastActiveDate  ");
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
                    case "no. of registered consultancy members":
                        sql.append(" consultancyMembers.registeredConsultancyMember " + sort.getOrderBy().toString());
                        break;
                    case "no. of pending consultancy member registrations":
                        sql.append(" consultancyMembers.pendingRegistrationConsultancyMember " + sort.getOrderBy().toString());
                        break;
                    case "no. of uploaded applicants profiles":
                        sql.append(" applicants.uploadedProfiles " + sort.getOrderBy().toString());
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

    public Map<String, Object> getConsManagerOfConsultancy(Long userId, String name, Date from, Date to, int pageNo, int pageSize, TimePeriod period) {

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

    public Map<String, Object> getApplicantsOfCons(long consultancyId, String name, int pageNumber, int pageSize) {
        String selectQuery = "SELECT distinct\n" +
                "        u.user_id,u.first_name,u.last_name,\n" +
                "        applicants.uploadedProfiles ";
        String countSql = "SELECT COUNT(distinct u.user_id) ";
        StringBuilder sqlBuilder = new StringBuilder(
                " FROM user u INNER JOIN\n" +
                        "(SELECT COUNT(applicant_id) AS uploadedProfiles,consultancy_user_id FROM applicant Group By consultancy_user_id) AS applicants ON applicants.consultancy_user_id = u.user_id \n" +
                        " WHERE u.consultancy_id = :consultancyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);

        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(u.first_name) LIKE LOWER(:name) OR LOWER(u.last_name) LIKE LOWER(:name))");
            params.put("name", "%" + name + "%");
        }

        String queryString = selectQuery + sqlBuilder;

        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        Query query = entityManager.createNativeQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getUploadedApplicants(Long consultancyId, String name, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period, UploadedApplicantFilterDTO uploadedApplicantFilterDTO, Long userId) {
        String selectQuery = "SELECT distinct a.applicant_id,a.first_name,a.last_name,a.created_date,a.location,ad.job_function,ad.secondary_job_function,a.gender,a.ethnicity,\n" +
                "ad.citizenship,ad.years_of_exp,ad.career_level,ad.preferred_work_mode,ad.job_type,ad.notice_period,ad.current_work_status,jobMatches.RelevantJobs AS RelevantJobs,\n" +
                "jobMatches.AppliedForJobs AS AppliedForJobs,u.first_name as userFirstName,u.last_name as userLastName";
        String countSql = "SELECT COUNT(distinct a.applicant_id) ";
        StringBuilder sqlBuilder = new StringBuilder(" FROM applicant a \n" +
                "left join applicant_details ad on ad.applicant_id=a.applicant_id \n" +
                "left join applicant_job_function ajf on ajf.applicant_id=a.applicant_id\n" +
                "left join (\n" +
                "SELECT COUNT(CASE WHEN applicant_job_status=0 and applicant_status=0 and interview_status=0 THEN 1 END) AS RelevantJobs,\n" +
                "COUNT(CASE WHEN applicant_job_status=1 THEN 1 END) AS AppliedForJobs,applicant_id FROM job_match_consultancy\n" +
                "group by applicant_id\n" +
                ") AS jobMatches ON jobMatches.applicant_id = a.applicant_id \n" +
                " inner join user u on u.user_id=a.consultancy_user_id \n" +
                " where a.consultancy_id= :consultancyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);
        if (userId != null && userId != 0) {
            sqlBuilder.append(" AND a.consultancy_user_id= :userId ");
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
                sqlBuilder.append(" AND DATE(a.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (name != null && !name.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(a.first_name) LIKE LOWER(:name) OR LOWER(a.last_name) LIKE LOWER(:name)) ");
            params.put("name", "%" + name + "%");
        }

        if (uploadedApplicantFilterDTO != null) {
            if (uploadedApplicantFilterDTO.getLocation() != null && !uploadedApplicantFilterDTO.getLocation().equals("")) {
                sqlBuilder.append("AND LOWER(a.location) = LOWER(:location) ");
                params.put("location", uploadedApplicantFilterDTO.getLocation());
            }
            if (uploadedApplicantFilterDTO.getJobFunction() != null && !uploadedApplicantFilterDTO.getJobFunction().isEmpty()) {
                sqlBuilder.append(" AND ajf.job_function_id IN (:jobFunction) ");
                params.put("jobFunction", uploadedApplicantFilterDTO.getJobFunction());
            }
            if (uploadedApplicantFilterDTO.getGender() != null && !uploadedApplicantFilterDTO.getGender().equals("")) {
                sqlBuilder.append(" AND a.gender = :gender ");
                params.put("gender", uploadedApplicantFilterDTO.getGender());
            }
            if (uploadedApplicantFilterDTO.getEthnicity() != null && !uploadedApplicantFilterDTO.getEthnicity().isEmpty()) {
                sqlBuilder.append(" AND a.ethnicity IN (:ethnicity) ");
                params.put("ethnicity", uploadedApplicantFilterDTO.getEthnicity());
            }
            if (uploadedApplicantFilterDTO.getCitizenship() != null && !uploadedApplicantFilterDTO.getCitizenship().isEmpty()) {
                sqlBuilder.append(" AND ad.citizenship IN (:citizenship) ");
                params.put("citizenship", uploadedApplicantFilterDTO.getCitizenship());
            }
            if (uploadedApplicantFilterDTO.getExpMin() != null && uploadedApplicantFilterDTO.getExpMax() != null) {
                sqlBuilder.append(" AND ad.years_of_exp >= :yearsOfExpMin AND ad.years_of_exp <= :yearsOfExpMax ");
                params.put("yearsOfExpMin", uploadedApplicantFilterDTO.getExpMin());
                params.put("yearsOfExpMax", uploadedApplicantFilterDTO.getExpMax());
            }
            if (uploadedApplicantFilterDTO.getCareerLevel() != null && !uploadedApplicantFilterDTO.getCareerLevel().isEmpty()) {
                sqlBuilder.append(" AND ad.career_level IN (:careerLevel) ");
                params.put("careerLevel", uploadedApplicantFilterDTO.getCareerLevel());
            }
            if (uploadedApplicantFilterDTO.getWorkMode() != null && !uploadedApplicantFilterDTO.getWorkMode().isEmpty()) {
                sqlBuilder.append(" AND ad.preferred_work_mode IN (:workModes) ");
                params.put("workModes", uploadedApplicantFilterDTO.getWorkMode());
            }
            if (uploadedApplicantFilterDTO.getJobTypes() != null && !uploadedApplicantFilterDTO.getJobTypes().isEmpty()) {
                sqlBuilder.append(" AND ad.job_type IN (:jobTypes) ");
                params.put("jobTypes", uploadedApplicantFilterDTO.getJobTypes());
            }
            if (uploadedApplicantFilterDTO.getNoticePeriod() != null && !uploadedApplicantFilterDTO.getNoticePeriod().isEmpty()) {
                sqlBuilder.append(" AND ad.notice_period IN (:noticePeriod) ");
                params.put("noticePeriod", uploadedApplicantFilterDTO.getNoticePeriod());
            }
            if (uploadedApplicantFilterDTO.getCurrentStatus() != null && !uploadedApplicantFilterDTO.getCurrentStatus().isEmpty()) {
                sqlBuilder.append(" AND ad.current_work_status IN (:currentWorkStatus) ");
                params.put("currentWorkStatus", uploadedApplicantFilterDTO.getCurrentStatus());
            }
            if (userId == null || userId == 0) {
                if (uploadedApplicantFilterDTO.getUploadedBy() != null && !uploadedApplicantFilterDTO.getUploadedBy().isEmpty()) {
                    sqlBuilder.append(" AND a.consultancy_user_id IN (:uploadedBy) ");
                    params.put("uploadedBy", uploadedApplicantFilterDTO.getUploadedBy());
                }
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        sqlBuilder.append(" order by ");
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
                    case "no. of jobs applied for":
                        sqlBuilder.append(" jobMatches.AppliedForJobs " + sort.getOrderBy().toString());
                        break;
                    case "no. of relevant jobs":
                        sqlBuilder.append(" jobMatches.RelevantJobs " + sort.getOrderBy().toString());
                        break;
                    //TODO profile completion % is storing not in DB
                    case "profile completion %":
                        sqlBuilder.append(" a.completion_percentage " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" a.created_date desc ");
        }

        String queryString = selectQuery + sqlBuilder;

        Query query = entityManager.createNativeQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getAppliedJobs(Long consultancyId, Long applicantId, String title, Date from, Date to, int pageNumber, int pageSize, List<SortByDTO> sortBy, TimePeriod period, JobAppliedFilterDTO jobAppliedFilterDTO, ApplicantJobs applicantJobs) {
        String selectQuery = "SELECT jp.job_post_id, jp.title, GROUP_CONCAT(DISTINCT jf.job_function_name SEPARATOR ',') AS job_function_names, " +
                " jp.location, jp.workloc_value, jp.job_type, c.name, jmc.interview_status,jmc.applicant_status, jmc.updated_date, jmc.match_score\n ";
        String countSql = "SELECT count(distinct jp.job_post_id) ";
        StringBuilder sqlBuilder = new StringBuilder(" FROM job_post jp\n" +
                "LEFT JOIN user u ON u.user_id = jp.user_id\n" +
                "LEFT JOIN jobpost_jobfunctions jpjf ON jpjf.job_post_id = jp.job_post_id\n" +
                "LEFT JOIN job_function jf ON jf.job_function_id = jpjf.job_function_id\n" +
                "LEFT JOIN company c ON c.company_id = u.company_id\n" +
                "LEFT JOIN job_match_consultancy jmc ON jmc.job_post_id = jp.job_post_id\n" +
                "WHERE jmc.consultancy_id = :consultancyId and jmc.applicant_id=:applicantId \n");

        if (applicantJobs != null && !applicantJobs.equals("")) {
            if (applicantJobs.equals(ApplicantJobs.APPLIED_JOBS)) {
                sqlBuilder.append(" and jmc.applicant_job_status=1 ");
            }
            if (applicantJobs.equals(ApplicantJobs.RELEVANT_JOBS)) {
                sqlBuilder.append(" and jmc.applicant_job_status=0 and jmc.applicant_status=0 and jmc.interview_status=0 ");
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);
        params.put("applicantId", applicantId);

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
                sqlBuilder.append(" AND DATE(jmc.created_date) BETWEEN :start AND :end ");
                params.put("start", sqlStartDate);
                params.put("end", sqlEndDate);
            }
        }

        if (title != null && !title.isEmpty()) {
            sqlBuilder.append(" AND LOWER(jp.title) LIKE LOWER(:title) ");
            params.put("title", "%" + title + "%");
        }

        if (jobAppliedFilterDTO != null) {
            if (jobAppliedFilterDTO.getLocation() != null && !jobAppliedFilterDTO.getLocation().isEmpty()) {
                sqlBuilder.append("AND jp.location IN (:location) ");
                params.put("location", jobAppliedFilterDTO.getLocation());
            }
            if (jobAppliedFilterDTO.getJobFunction() != null && !jobAppliedFilterDTO.getJobFunction().isEmpty()) {
                sqlBuilder.append(" AND  jf.job_function_id IN (:jobFunction) ");
                params.put("jobFunction", jobAppliedFilterDTO.getJobFunction());
            }
            if (jobAppliedFilterDTO.getWorkMode() != null && !jobAppliedFilterDTO.getWorkMode().equals("")) {
                sqlBuilder.append(" AND jp.workloc_value = :workMode ");
                params.put("workMode", jobAppliedFilterDTO.getWorkMode());
            }
            if (jobAppliedFilterDTO.getJobTypes() != null && !jobAppliedFilterDTO.getJobTypes().isEmpty()) {
                sqlBuilder.append(" AND jp.job_type IN (:jobTypes) ");
                params.put("jobTypes", jobAppliedFilterDTO.getJobTypes());
            }
            if (jobAppliedFilterDTO.getCompanyName() != null && !jobAppliedFilterDTO.getCompanyName().isEmpty()) {
                sqlBuilder.append(" AND c.name IN (:companies) ");
                params.put("companies", jobAppliedFilterDTO.getCompanyName());
            }
            if (jobAppliedFilterDTO.getStatus() != null && !jobAppliedFilterDTO.getStatus().isEmpty()) {
                sqlBuilder.append(" AND jmc.interview_status IN (:status) ");
                params.put("status", jobAppliedFilterDTO.getStatus());
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        sqlBuilder.append(" GROUP BY jp.job_post_id, jmc.interview_status, jmc.updated_date, jmc.match_score ");

        sqlBuilder.append(" order by ");
        if (sortBy != null && !sortBy.isEmpty()) {
            boolean isFirstSortColumn = true;
            for (SortByDTO sort : sortBy) {
                if (!isFirstSortColumn) {
                    sqlBuilder.append(", ");
                }
                switch (sort.getColumnName().toLowerCase()) {
                    case "date of last status update":
                        sqlBuilder.append(" jmc.updated_date " + sort.getOrderBy().toString());
                        break;
                    case "profile match %":
                        sqlBuilder.append(" jmc.match_score " + sort.getOrderBy().toString());
                        break;
                }
                isFirstSortColumn = false;
            }
        } else {
            sqlBuilder.append(" jmc.updated_date desc ");
        }

        String queryString = selectQuery + sqlBuilder;

        Query query = entityManager.createNativeQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getJobsActivity(Long consultancyId, String title, int pageNumber, int pageSize, ConsActivityFilterDTO consActivityFilterDTO) {
        String selectQuery = "select distinct jp.job_post_id,jp.title,c.name,matchedCons.applied,matchedCons.underInterview,matchedCons.underHired,matchedCons.underRejected,\n" +
                "matchedCons.fname,matchedCons.lname ";
        String countSql = "select count(distinct jp.job_post_id) ";
        StringBuilder sqlBuilder = new StringBuilder(" from job_post jp\n" +
                "left join user u  on u.user_id=jp.user_id\n" +
                "left join jobpost_jobfunctions jpjf on jpjf.job_post_id = jp.job_post_id\n" +
                "left join job_function jf on jf.job_function_id = jpjf.job_function_id\n" +
                "left join company c on c.company_id=u.company_id\n" +
                "inner join \n" +
                "(select \n" +
                "count(case when jmc.applicant_job_status=1 then 1 END) AS applied,\n" +
                "count(case when jmc.interview_status in (2,3,4,5,6,9)  then 1 END) AS underInterview,\n" +
                "count(case when jmc.interview_status in (1,14,16,17)   then 1 END) AS underHired,\n" +
                "count(case when jmc.interview_status in (5,10,15,18)   then 1 END) AS underRejected,\n" +
                "jmc.job_post_id,uj.first_name as fname,uj.last_name as lname,jmc.consultancy_user_id as userId  from  job_match_consultancy jmc left join user uj on uj.user_id=jmc.consultancy_user_id\n" +
                "where jmc.consultancy_id=:consultancyId group by jmc.job_post_id,fname,lname,userId) as matchedCons on matchedCons.job_post_id=jp.job_post_id ");

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);


        if (title != null && !title.isEmpty()) {
            sqlBuilder.append(" AND LOWER(jp.title) LIKE LOWER(:title) ");
            params.put("title", "%" + title + "%");
        }

        if (consActivityFilterDTO != null) {
            if (consActivityFilterDTO.getCompanyName() != null && !consActivityFilterDTO.getCompanyName().isEmpty()) {
                sqlBuilder.append(" AND c.name IN (:companies) ");
                params.put("companies", consActivityFilterDTO.getCompanyName());
            }
            if (consActivityFilterDTO.getAppliedBy() != null && !consActivityFilterDTO.getAppliedBy().isEmpty()) {
                sqlBuilder.append(" AND matchedCons.userId IN (:userId) ");
                params.put("userId", consActivityFilterDTO.getAppliedBy());
            }
        }

        Query countQuery = entityManager.createNativeQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = ((BigInteger) countQuery.getSingleResult()).longValue();

        String queryString = selectQuery + sqlBuilder;
        Query query = entityManager.createNativeQuery(queryString);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult(pageNumber * pageSize).setMaxResults(pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("count", totalCount);
        map.put("data", query.getResultList());

        return map;
    }

    public Map<String, Object> getApplicantData(Long consultancyId, Long jobPostId, JobApplicantFilter jobApplicantFilter, String name, ApplicantProfileStatus applicantStatus, Date from, Date to, int pageNo, int pageSize, List<SortByDTO> sortBy, TimePeriod period) throws WorkruitException {

        String selectQuery = "SELECT jc.applicant_id,ap.first_name as apFirstName,ap.last_name as apLastName,ap.created_date,ad.job_function,\n" +
                "ad.secondary_job_function,ap.location,\n" +
                "jc.updated_date,jc.match_score,jc.applicant_status,\n" +
                "jc.interview_status  ";
        String countSql = "SELECT COUNT(DISTINCT jc.job_match_con_id)";
        StringBuilder sqlBuilder = new StringBuilder(" FROM job_match_consultancy jc\n" +
                "left join applicant ap on ap.applicant_id=jc.applicant_id\n" +
                "left join applicant_details ad on ap.applicant_id=ad.applicant_id\n" +
                "where jc.job_post_id= :jobPostId AND jc.consultancy_id = :consultancyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("jobPostId", jobPostId);
        params.put("consultancyId", consultancyId);
        switch (applicantStatus) {
            case APPLIED:
                sqlBuilder.append(" AND jc.applicant_job_status=1 ");
                if (jobApplicantFilter != null) {
                    if (jobApplicantFilter.getStatus() == 1) {
                        sqlBuilder.append(" AND jc.applicant_status=1 ");
                    } else if (jobApplicantFilter.getStatus() == 2) {
                        sqlBuilder.append(" AND jc.applicant_status!=1 ");
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
                    sqlBuilder.append(" AND jc.interview_status in (1,14,16,17) ");
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
                sqlBuilder.append(" AND DATE(jc.updated_date) BETWEEN :start AND :end ");
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

    public Map<String, Object> getConsultancyAlertData(long consultancyId, List<Long> userId, int pageNumber, int pageSize, Date from, Date to, TimePeriod period) {
        String selectQuery = "select a.message,a.created_date  ";
        String countSelect = "select count(*) ";
        StringBuilder sql = new StringBuilder(" from alert a\n" +
                "where a.consultancy_id= :consultancyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);

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
}
