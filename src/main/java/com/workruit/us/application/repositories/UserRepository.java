/**
 *
 */
package com.workruit.us.application.repositories;

import com.workruit.us.application.dto.UserCompanyResultSet;
import com.workruit.us.application.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Santosh Bhima
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByWorkEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    @Query(value = "select * from user u where u.consultancy_id =?1", nativeQuery = true)
    User findByConsultancyId(Long consultancyId);

    List<User> findByConsultancyIdAndEnabled(Long consultancyId, boolean enabled, Pageable page);

    @Query("select u from User u where u.consultancyId =?1 and enabled=?2 and createdBy=?3 and role_name=?4 order by updatedDate DESC")
    Page<User> findByConsultancyIdAndEnabledAndCreatedBy(Long consultancyId, boolean enabled, Long createdBy, String role,
                                                         Pageable page);

    @Query("select u from User u where u.consultancyId =?1 and  u.enabled=?2 and u.createdBy=?3 and u.firstName!='' and u.lastName!=''  and role_name=?4")
    Page<User> findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(Long consultancyId, boolean enabled, Long createdBy, String role,
                                                                     Pageable page);

    @Query("select u from User u where u.consultancyId =?1 and u.enabled=?2 and u.createdBy=?3 and u.firstName!='' and u.lastName!='' and u.workEmail like %?4% and role_name=?5")
    List<User> findCollabratorsByConsultancyIdAndEnabledAndCreatedBy(Long consultancyId, boolean enabled, Long createdBy, String text, String role);

    @Query("select u.userId from User u where u.consultancyId =?1 and enabled=?2 and createdBy=?3")
    List<Long> findUsersIdsByConsultancyIdAndEnabled(Long consultancyId, boolean enabled, Long createdBy,
                                                     Pageable page);

    @Query("select u.userId from User u where u.consultancyId =?1 and enabled=?2 and departmentId=?3")
    List<Long> findSameDepartmentUsersIdsByConsultancyIdAndEnabled(Long consultancyId, boolean enabled, Long department,
                                                                   Pageable page);

    @Query("select u from User u where u.consultancyId =?1 and enabled=?2 and departmentId=?3 and u.firstName!='' and u.lastName!='' and userId!=?4 and role_name=?5")
    Page<User> findSameDepartmentUsersIdsByUserIdAndEnabled(Long consultancyId, boolean enabled, Long department, Long userId, String role,
                                                            Pageable page);

    @Query("select u from User u where u.consultancyId =?1 and u.enabled=?2 and u.departmentId=?3 and u.userId!=?4 and u.firstName!='' and u.lastName!='' and u.workEmail like %?5%  and role_name=?6")
    List<User> findSameDepartmentUsersIdsByUserIdAndEnabled(Long consultancyId, boolean enabled, Long department, Long userId, String text, String role);

    @Query("select u.userId as recruiterId, c as company from User u, Company c  where u.companyId = c.companyId and u.userId in (?1)")
    List<UserCompanyResultSet> getCompanyInfoByUserList(List<Long> recruiterId);

    @Query("select u from User u where  u.companyId=?1 and u.enabled=1 and u.workEmail like %?2% ")
    List<User> getCompanyInfoByUserList(Long companyId, String text);

    @Query("select u from User u where u.userId in (?1)")
    List<User> findByConsultancyUsers(List<Long> consultancyId);

    @Query("select u from User u where u.consultancyId in (?1)")
    List<User> findByConsultancyUsersIds(List<Long> consultancyId);

    Page<User> findByConsultancyIdAndDepartmentId(Long consultancyId, Long departmentId, Pageable pageable);

    @Query(value = "SELECT * FROM user u INNER JOIN user_role ur ON ur.user_id = u.user_id WHERE u.consultancy_id = ?1 AND ur.role_id in ?2 ORDER BY u.user_id LIMIT 1", nativeQuery = true)
    User findMainConsultancyUser(Long consultancyId, List<Long> roleIds);

    @Query(value = "SELECT * FROM user u INNER JOIN user_role ur ON ur.user_id = u.user_id WHERE u.company_id = ?1 AND ur.role_id = 2 ORDER BY u.user_id LIMIT 1", nativeQuery = true)
    User findMainCompanyHrUser(Long companyId);

    @Query(value = "SELECT u.* FROM user u WHERE u.enabled = 1\n" +
            "AND NOT EXISTS (SELECT 1 FROM applicant a WHERE a.consultancy_user_id = u.user_id)\n" +
            "AND TIMESTAMPDIFF(SECOND, updated_date, NOW()) >= 86400\n" +
            "AND NOT EXISTS (SELECT 1 FROM notification n WHERE n.title LIKE '%Find the right job%' AND n.user_id = u.user_id) ", nativeQuery = true)
    List<User> findUseNotUploadedAnyApplicant();

}
