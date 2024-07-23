/**
 *
 */
package com.workruit.us.application.service;

import com.workruit.us.application.dto.JobMatchStateResultSet;
import com.workruit.us.application.dto.SavedProfilesDTO;
import com.workruit.us.application.dto.SavedProfilesResponse;
import com.workruit.us.application.models.*;
import com.workruit.us.application.repositories.*;
import com.workruit.us.application.services.ActivityService;
import com.workruit.us.application.services.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Mahesh
 */
@Slf4j
@Service
public class SaveProfileService {

    private @Autowired JobMatchConsultancyRepository jobMatchConsultancyRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired JobPostRepository jobPostRepository;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;

    private @Autowired ActivityService activityService;

    private @Autowired SaveProfileService saveProfileService;

    private @Autowired ConsultancyJobStatusRepository consultancyJobStatusRepository;


    private @Autowired ImageService imageService;

    public SavedProfilesResponse getSavedProfilesForJob(long recruiterId, long jobId, Integer pageNo, Integer pageSize)
            throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<JobMatchConsultancy> jobMatchRs = jobMatchConsultancyRepository
                .findByJobPostIdAndRecruiterIdAndIsSavedByRecruiter(jobId, recruiterId, true, pageable);
        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        for (JobMatchConsultancy jobMatchCons : jobMatchRs) {
            if (jobMatchCons.getConsultancyId() != 0)
                consultancyIds.add(jobMatchCons.getConsultancyId());
            applicantIds.add(jobMatchCons.getApplicantId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        Optional<User> recruiterObj = userRepository.findById(recruiterId);
        User recruiter = null;
        if (!recruiterObj.isPresent()) {
            log.error("Couldn't load recruiter by id " + recruiterId);
            throw new Exception("Recruiter not found");
        }
        recruiter = recruiterObj.get();

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        SavedProfilesResponse response = new SavedProfilesResponse();
        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<SavedProfilesDTO> savedProfileDTOs = new ArrayList<>();
        SavedProfilesDTO savedProfileDTO = null;
        for (JobMatchConsultancy jobMatchCons : jobMatchRs) {
            savedProfileDTO = new SavedProfilesDTO();
            savedProfileDTO.setApplicantId(jobMatchCons.getApplicantId());
            savedProfileDTO.setApplicantName(applicantMap.get(jobMatchCons.getApplicantId()).getFirstName() + " "
                    + applicantMap.get(jobMatchCons.getApplicantId()).getLastName());
            savedProfileDTO.setConsultancyName(
                    jobMatchCons.getConsultancyId() != 0 ? consultancyMap.get(jobMatchCons.getConsultancyId()) : "");
            savedProfileDTO.setLocation(applicantMap.get(jobMatchCons.getApplicantId()).getCountry());
            String imageUrl = applicantMap.get(jobMatchCons.getApplicantId()).getProfileImageUrl();
            try {
                savedProfileDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                savedProfileDTO.setProfilePicUrl(null);
            }
            savedProfileDTO.setConsultancy(jobMatchCons.getConsultancyId() != 0);
            savedProfileDTO.setJobMatchId(jobMatchCons.getJobMatchConId());
            savedProfileDTOs.add(savedProfileDTO);
        }
        response.setSavedProfilesDTO(savedProfileDTOs);
        return response;
    }


    // status 0 All, 1 Intrested,2 no action
    public SavedProfilesResponse getSavedProfilesForJob(long recruiterId, long conId, int status, int filter,
                                                        Integer pageNo, Integer pageSize, long consultantId, String role) throws Exception {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<JobMatchStateResultSet> jobMatchRs = null;
        SavedProfilesResponse response = new SavedProfilesResponse();
        List<Long> userIds = new ArrayList<>();
        List<Long> jobPostIds = new ArrayList<>();

        if (role.equals("COMPANY_ADMIN")) {
            userIds.addAll(activityService.getUserIds(consultantId, recruiterId, 0, role, filter));
            if (status == 0) {
                jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, pageable);
            } else if (status == 1) {
                jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, 0, 1, pageable);
            } else if (status == 2) {
                jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, 0, 0, pageable);
            }
        } else {
            Page<JobPost> jobPostList = null;
            userIds.add(recruiterId);
            if (filter == 0) {
                if (status == 0) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, pageable);
                } else if (status == 1) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, 0, 1, pageable);
                } else if (status == 2) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJob(userIds, 0, 0, pageable);
                }
            } else if (filter == 1) {
                jobPostList = jobPostRepository.findJobByCollaboratorIdAndStatusforSavedJobs(String.valueOf(recruiterId), pageable);
                for (JobPost job : jobPostList) {
                    jobPostIds.add(job.getJobPostId());
                }
                if (status == 0) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIdsandNotByUser(jobPostIds, userIds, pageable);
                } else if (status == 1) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIdsandNotByUser(jobPostIds, 0, 1, userIds, pageable);
                } else if (status == 2) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIdsandNotByUser(jobPostIds, 0, 0, userIds, pageable);
                }
            } else {
                jobPostList = jobPostRepository.findJobByCollaboratorIdAndStatusforSavedJobs(String.valueOf(recruiterId), pageable);
                for (JobPost job : jobPostList) {
                    jobPostIds.add(job.getJobPostId());
                }
                if (status == 0) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIds(jobPostIds, userIds, pageable);
                } else if (status == 1) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIds(jobPostIds, 0, 1, userIds, pageable);
                } else if (status == 2) {
                    jobMatchRs = jobMatchConsultancyRepository.getSavedProfilesforJobBasedonjobIds(jobPostIds, 0, 0, userIds, pageable);
                }
            }

        }

        if (jobMatchRs == null) {
            return response;
        }
        // Load consultancy details
        List<Long> consultancyIds = new ArrayList<>();
        List<Long> applicantIds = new ArrayList<>();
        List<Long> jobIds = new ArrayList<>();
        for (JobMatchStateResultSet jobMatchCons : jobMatchRs) {
            if (jobMatchCons.getConsultancyId() != 0)
                consultancyIds.add(jobMatchCons.getConsultancyId());
            applicantIds.add(jobMatchCons.getApplicantId());
            jobIds.add(jobMatchCons.getJobPostId());
        }

        // Fetch consultancy data, applicant data, recruiter data
        List<Consultancy> consultancyObj = (List<Consultancy>) consultancyRepository.findAllById(consultancyIds);
        List<Applicant> applicantObj = (List<Applicant>) applicantRepository.findAllById(applicantIds);
        List<JobPost> jobIdsObj = jobPostRepository.findAllById(jobIds);
        List<ApplicantDetails> applicatDetailsList = applicantDetailsRepository
                .findByUsersDetails(applicantIds);

        Optional<User> recruiterObj = userRepository.findById(recruiterId);
        User recruiter = null;
        if (!recruiterObj.isPresent()) {
            log.error("Couldn't load recruiter by id " + recruiterId);
            throw new Exception("Recruiter not found");
        }
        recruiter = recruiterObj.get();

        Map<Long, String> consultancyMap = consultancyObj.stream()
                .collect(Collectors.toMap(Consultancy::getConsultancyId, Consultancy::getName));
        Map<Long, Applicant> applicantMap = applicantObj.stream()
                .collect(Collectors.toMap(Applicant::getApplicantId, b -> b));

        Map<Long, ApplicantDetails> applicantDetailsMap = applicatDetailsList.stream()
                .collect(Collectors.toMap(ApplicantDetails::getApplicantId, b -> b));

        Map<Long, JobPost> jobDetailsMap = jobIdsObj.stream().collect(Collectors.toMap(JobPost::getJobPostId, b -> b));

        response.setTotalCount(jobMatchRs.getTotalElements());
        response.setTotalPages(jobMatchRs.getTotalPages());
        List<SavedProfilesDTO> savedProfileDTOs = new ArrayList<>();
        SavedProfilesDTO savedProfileDTO = null;
        for (JobMatchStateResultSet jobMatchCons : jobMatchRs) {
            savedProfileDTO = new SavedProfilesDTO();
            savedProfileDTO.setApplicantId(jobMatchCons.getApplicantId());
            savedProfileDTO.setApplicantStatus(jobMatchCons.getApplicantStatus());
            savedProfileDTO.setApplicantName(applicantMap.get(jobMatchCons.getApplicantId()).getFirstName() + " "
                    + applicantMap.get(jobMatchCons.getApplicantId()).getLastName());
            savedProfileDTO.setConsultancyName(
                    jobMatchCons.getConsultancyId() != 0 ? consultancyMap.get(jobMatchCons.getConsultancyId()) : "");
            savedProfileDTO.setLocation(applicantMap.get(jobMatchCons.getApplicantId()).getLocation());

            String imageUrl = applicantMap.get(jobMatchCons.getApplicantId()).getProfileImageUrl();
            try {
                savedProfileDTO.setProfilePicUrl(imageService.getImage(imageUrl));
            } catch (IOException e) {
                savedProfileDTO.setProfilePicUrl(null);
            }
            savedProfileDTO.setConsultancy(jobMatchCons.getConsultancyId() != 0);
            savedProfileDTO.setJobMatchId(jobMatchCons.getJobMatchId());
            savedProfileDTO.setConsultancyId(jobMatchCons.getConsultancyId());

            ApplicantDetails applicantDetails = applicantDetailsMap.get(jobMatchCons.getApplicantId());
            savedProfileDTO.setJobFunctionName(String.valueOf(applicantDetails.getJobFunction()));
            savedProfileDTO.setLocation(applicantMap.get(jobMatchCons.getApplicantId()).getLocation());

            JobPost jobDetails = jobDetailsMap.get(jobMatchCons.getJobPostId());

            savedProfileDTO.setJobTitle(jobDetails.getTitle());
            savedProfileDTO.setJobId(jobDetails.getJobPostId());

            //  if (jobMatchCons.getSavedRecruiter() != null && jobMatchCons.getSavedRecruiter() == 1) {
            if (jobMatchCons.getUpdatedDate() != null) {
                String[] recruiterUpdatedDate = jobMatchCons.getUpdatedDate().toString().split("-");
                LocalDate recruiterUpdatedLocalDate = LocalDate.of(Integer.parseInt(recruiterUpdatedDate[0]), Integer.parseInt(recruiterUpdatedDate[1]), Integer.parseInt(recruiterUpdatedDate[2].split(" ")[0]));
                long daysDiff = ChronoUnit.DAYS.between(recruiterUpdatedLocalDate, LocalDate.now());
                savedProfileDTO.setExpiration(daysDiff < 0 ? "Expired" : (7 - daysDiff) == 0 ? "Expire today" : (7 - daysDiff) == 1 ? "Expires in 1 day" : daysDiff > 7 ? "Expired" : "Expires in " + (7 - daysDiff) + " days");
                savedProfileDTO.setDiffDays(daysDiff);
            }
            // }
            savedProfileDTOs.add(savedProfileDTO);
        }
        response.setSavedProfilesDTO(savedProfileDTOs);
        return response;
    }

    @Transactional
    public void updateSavedJobs() {
        consultancyJobStatusRepository.deleteSavedJobs();
        jobMatchConsultancyRepository.updateSavedJobRecrutier();

    }

}
