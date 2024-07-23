/**
 *
 */
package com.workruit.us.application.services;

import com.workruit.us.application.configuration.AuthenticationException;
import com.workruit.us.application.configuration.WorkruitException;
import com.workruit.us.application.constants.CommonConstants;
import com.workruit.us.application.dto.*;
import com.workruit.us.application.enums.*;
import com.workruit.us.application.models.*;
import com.workruit.us.application.models.UserImportAsyncStatus.UserImportJobStatus;
import com.workruit.us.application.repositories.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Santosh Bhima
 */
@Service
public class ApplicantService {

    private static final int BASIC_PROFILE = 30;
    private static final Logger logger = LoggerFactory.getLogger(ApplicantService.class);
    private @Autowired ApplicantRepository applicantRepository;
    private @Autowired PasswordEncoder passwordEncoder;
    private @Autowired EducationHistoryRepository educationHistoryRepository;
    private @Autowired WorkExperienceRepository workExperienceRepository;
    private @Autowired WorkExperienceService workExperienceService;
    private @Autowired CourseService courseService;
    private @Autowired EducationHistoryService educationHistoryService;
    private @Autowired InternshipService internshipService;
    private @Autowired ProjectService projectService;
    private @Autowired ApplicantDetailsService applicantDetailsService;
    private @Autowired ReferenceService referenceService;
    private @Autowired PublicationService publicationService;
    private @Autowired CertificateService certificateService;
    private @Autowired ApplicantDetailsRepository applicantDetailsRepository;
    private @Autowired InternshipRepository internshipRepository;
    private @Autowired ProjectRepository projectRepository;
    private @Autowired CourseRepository courseRepository;
    private @Autowired ReferenceRepository referenceRepository;
    private @Autowired SocialMediaLinksRepository socialMediaLinksRepository;
    private @Autowired JobFunctionRepository jobFunctionRepository;
    private @Autowired JobSkillsRepository jobSkillsRepository;
    private @Autowired ApplicantJobFunctionRepository applicantJobFunctionRepository;
    private @Autowired ApplicantJobSkillRepository applicantJobSkillRepository;
    private @Autowired AlertRepository alertRepository;
    private @Autowired UserRepository userRepository;
    private @Autowired UserImportAsyncStatusRepository userImportAsyncStatusRepository;
    private @Autowired ConsultancyRepository consultancyRepository;
    private @Autowired PublicationRepository publicationRepository;
    private @Autowired CertificateRepository certificateRepository;
    private @Autowired AlertService alertService;
    private @Autowired JobPostService jobPostService;
    private @Autowired ImageService imageService;
    private @Autowired ConsultancyJobService consultancyJobService;
    private @Autowired ApplicantSecondaryJobFunctionRepository applicantSecondaryJobFunctionRepository;
    private @Autowired DegreesRepository degreesRepository;
    private @Autowired FileService fileService;
    @PersistenceContext
    private EntityManager entityManager;

    public Long createUploadJob(String key, long userId, long consultancyId) throws IOException {

        String data = fileService.getCSV(key.trim());
        byte[] byteArray = Base64.getDecoder().decode(data);
        InputStream inputStream = new ByteArrayInputStream(byteArray);
        long time = System.currentTimeMillis();
        final File file = new File(time + ".csv");
        OutputStream outStream = new FileOutputStream(file);
        byte[] buffer = new byte[20 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.close();

        UserImportAsyncStatus userImportAsyncStatus = new UserImportAsyncStatus();
        userImportAsyncStatus.setCreatedDate(new Date());
        userImportAsyncStatus.setUpdatedDate(new Date());
        userImportAsyncStatus.setStatus(UserImportJobStatus.CREATED);
        userImportAsyncStatus.setUserId(userId);
        userImportAsyncStatus.setCsvkey(key);
        userImportAsyncStatus.setDescription("File accepted & records are processing");
        if (userImportAsyncStatusRepository.findVersionByUserImportAsyncUserId(userId) != null) {
            userImportAsyncStatus.setVersion(userImportAsyncStatusRepository.findVersionByUserImportAsyncUserId(userId) + 1);
        } else {
            userImportAsyncStatus.setVersion(1L);
        }
        Long version = userImportAsyncStatus.getVersion();
        userImportAsyncStatus = userImportAsyncStatusRepository.save(userImportAsyncStatus);
        Long id = userImportAsyncStatus.getUserImportAsyncStatusId();
        Thread t = new Thread(() -> {
            try {
                processFile(file, id, consultancyId, version, userId);

            } catch (Exception e) {
                UserImportAsyncStatus userImportAsyncStatusNew = userImportAsyncStatusRepository.findById(id).get();
                userImportAsyncStatusNew.setStatus(UserImportJobStatus.FAILED);
                userImportAsyncStatusNew.setDescription("Failed due to " + e.getMessage());
                userImportAsyncStatusRepository.save(userImportAsyncStatusNew);
                e.printStackTrace();
            }
        });
        t.start();
        return userImportAsyncStatus.getUserImportAsyncStatusId();
    }

    public UserImportJobStatusDTO getJobStatus(Long jobId, Long userId) throws Exception {
        UserImportAsyncStatus userImportAsyncStatus = userImportAsyncStatusRepository
                .findByUserImportAsyncStatusIdAndUserId(jobId, userId);
        if (userImportAsyncStatus != null) {
            UserImportJobStatusDTO userImportJobStatusDTO = new UserImportJobStatusDTO();
            userImportJobStatusDTO.setDescription(userImportAsyncStatus.getDescription());
            userImportJobStatusDTO.setStatus(userImportAsyncStatus.getStatus());
            userImportJobStatusDTO.setTotalCount(userImportAsyncStatus.getTotalCount());
            userImportJobStatusDTO.setProcessedCount(userImportAsyncStatus.getProcessedCount());
            userImportJobStatusDTO.setEmailDuplicates(userImportAsyncStatus.getEmailDuplicates());
            userImportJobStatusDTO.setMobileDuplicates(userImportAsyncStatus.getMobileDuplicates());
            userImportJobStatusDTO.setSavedCounts(userImportAsyncStatus.getSavedCount());
            return userImportJobStatusDTO;
        } else {
            throw new Exception("No matching job found");
        }
    }

    /**
     * Applicant Basic Profile : 30 - First Name, LastName, Gender, Country, Email
     * Projects : 5 Job Skills : 10 Applicant Detail : 20 - current salary, expected
     * salary, notice period, years of exp, prefered work mode Education History :
     * 10 Social Media : 10 Reference : 5 Work Experience : 10
     *
     * @param applicantId
     * @return
     */
    public float profileCompletionStatus(Long applicantId) {
        Optional<Applicant> optional = applicantRepository.findById(applicantId);
        if (optional.isPresent()) {
            Applicant applicant = optional.get();
            float total = 0;
            if (applicant.getProfileImageUrl() != null && StringUtils.isNotBlank(applicant.getProfileImageUrl())) {
                total = total + 0.5f;
            }
            if (applicant.getFirstName() != null && StringUtils.isNotBlank(applicant.getFirstName())) {
                total = total + 2;
            }
            if (applicant.getLastName() != null && StringUtils.isNotBlank(applicant.getLastName())) {
                total = total + 2;
            }
            if (applicant.getProfileSummary() != null && StringUtils.isNotBlank(applicant.getProfileSummary())) {
                total = total + 5;
            }
            if (applicant.getDob() != null && !applicant.getDob().equals("")) {
                total = total + 2;
            }
            if (applicant.getEmail() != null && StringUtils.isNotBlank(applicant.getEmail())) {
                total = total + 3;
            }
            if (applicant.getCountryCode() != null && StringUtils.isNotBlank(applicant.getCountryCode())
                    && applicant.getPhoneNumber() != null && StringUtils.isNotBlank(applicant.getPhoneNumber())) {
                total = total + 3;
            }
            if (applicant.getLocation() != null && StringUtils.isNotBlank(applicant.getLocation())) {
                total = total + 3;
            }
            if (applicant.getGender() != null && StringUtils.isNotBlank(applicant.getGender())) {
                total = total + 1;
            }
            if (applicant.getEthnicity() != null && StringUtils.isNotBlank(applicant.getEthnicity())) {
                total = total + 1;
            }

            ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicantId);
            if (applicantDetails != null) {
                if (applicantDetails.getCareerLevel() != null && StringUtils.isNotBlank(applicantDetails.getCareerLevel())) {
                    total = total + 2;
                }
                if (applicantDetails.getYearsOfExperience() != null && Float.valueOf(applicantDetails.getYearsOfExperience()) >= 0) {
                    total = total + 3;
                }
                if (applicantDetails.getCurrentSalary() != null && StringUtils.isNotBlank(applicantDetails.getCurrentSalary())) {
                    total = total + 1;
                }
                if (applicantDetails.getSalaryRate() != null && StringUtils.isNotBlank(applicantDetails.getSalaryRate())) {
                    total = total + 1;
                }
                if (applicantDetails.getExpectedSalary() != null && StringUtils.isNotBlank(applicantDetails.getExpectedSalary())) {
                    total = total + 1;
                }
                if (applicantDetails.getExpectedSalaryRate() != null && StringUtils.isNotBlank(applicantDetails.getExpectedSalaryRate())) {
                    total = total + 1;
                }
                if (applicantDetails.getJobFunction() != null && StringUtils.isNotBlank(applicantDetails.getJobFunction())) {
                    total = total + 3;
                }
                if (applicantDetails.getSecondaryJobFunction() != null && StringUtils.isNotBlank(applicantDetails.getSecondaryJobFunction())) {
                    total = total + 1;
                }
                if (applicantDetails.getSkills() != null && StringUtils.isNotBlank(applicantDetails.getSkills())) {
                    String[] jobSkills = applicantDetails.getSkills().split(",");
                    if (jobSkills.length >= 5) {
                        total = total + 5;
                    } else {
                        total = total + (jobSkills.length);
                    }
                }
                if (applicantDetails.getPreferredWorkMode() != null && StringUtils.isNotBlank(applicantDetails.getPreferredWorkMode())) {
                    total = total + 3;
                }
                if (applicantDetails.getJobType() != null && StringUtils.isNotBlank(applicantDetails.getJobType())) {
                    total = total + 3;
                }
                if (applicantDetails.getNoticePeriod() != null && StringUtils.isNotBlank(applicantDetails.getNoticePeriod())) {
                    total = total + 3;
                }
                if (applicantDetails.getCurrentWorkStatus() != null && StringUtils.isNotBlank(applicantDetails.getCurrentWorkStatus())) {
                    total = total + 2;
                }
                if (applicantDetails.getCitizenship() != null && StringUtils.isNotBlank(applicantDetails.getCitizenship())) {
                    total = total + 3;
                }
                if (applicantDetails.isWillingToRelocate()) {
                    total = total + 1;
                    if (applicantDetails.getRelocation() != null && StringUtils.isNotBlank(applicantDetails.getRelocation())) {
                        total = total + 2;
                    }
                } else {
                    total = total + 3;
                }
            }

            List<WorkExperience> workExperiences = workExperienceRepository.findByApplicantId(applicantId);
            if (workExperiences != null && workExperiences.size() > 0) {
                if (workExperiences.size() == 1) {
                    WorkExperience workExperience = workExperiences.get(0);
                    if (workExperience.getJobTitle() != null && StringUtils.isNotBlank(workExperience.getJobTitle())) {
                        total = total + 1;
                    }
                    if (workExperience.getCompanyName() != null && StringUtils.isNotBlank(workExperience.getCompanyName())) {
                        total = total + 1;
                    }
                    if (workExperience.getLocation() != null && StringUtils.isNotBlank(workExperience.getLocation())) {
                        total = total + 1;
                    }
                    if (workExperience.getStartDate() != null && !workExperience.getStartDate().equals("")) {
                        total = total + 1;
                    }
                    if (workExperience.getEndDate() != null && !workExperience.getEndDate().equals("")) {
                        total = total + 1;
                    }
                    if (workExperience.getDescription() != null && StringUtils.isNotBlank(workExperience.getDescription())) {
                        total = total + 1;
                    }
                } else {
                    for (WorkExperience workExperience : workExperiences) {
                        if (workExperience.getJobTitle() != null && StringUtils.isNotBlank(workExperience.getJobTitle())) {
                            total = total + (1f / workExperiences.size());
                        }
                        if (workExperience.getCompanyName() != null && StringUtils.isNotBlank(workExperience.getCompanyName())) {
                            total = total + (1f / workExperiences.size());
                        }
                        if (workExperience.getLocation() != null && StringUtils.isNotBlank(workExperience.getLocation())) {
                            total = total + (1f / workExperiences.size());
                        }
                        if (workExperience.getStartDate() != null && !workExperience.getStartDate().equals("")) {
                            total = total + (1f / workExperiences.size());
                        }
                        if (workExperience.getEndDate() != null && !workExperience.getEndDate().equals("")) {
                            total = total + (1f / workExperiences.size());
                        }
                        if (workExperience.getDescription() != null && StringUtils.isNotBlank(workExperience.getDescription())) {
                            total = total + (1f / workExperiences.size());
                        }
                    }
                    if ((workExperiences.size() % 2) != 0) {
                        total = Math.round(total);
                    }
                }
            }

            List<Internship> internships = internshipRepository.findByApplicantId(applicantId);
            if (internships != null && internships.size() > 0) {
                if (internships.size() == 1) {
                    Internship internship = internships.get(0);
                    if (internship.getJobTitle() != null && StringUtils.isNotBlank(internship.getJobTitle())) {
                        total = total + 0.5f;
                    }
                    if (internship.getCompanyName() != null && StringUtils.isNotBlank(internship.getCompanyName())) {
                        total = total + 0.5f;
                    }
                    if (internship.getLocation() != null && StringUtils.isNotBlank(internship.getLocation())) {
                        total = total + 0.5f;
                    }
                    if (internship.getStartDate() != null && !internship.getStartDate().equals("")) {
                        total = total + 0.5f;
                    }
                    if (internship.getEndDate() != null && !internship.getEndDate().equals("")) {
                        total = total + 0.5f;
                    }
                    if (internship.getDescription() != null && StringUtils.isNotBlank(internship.getDescription())) {
                        total = total + 0.5f;
                    }
                } else {
                    for (Internship internship : internships) {
                        if (internship.getJobTitle() != null && StringUtils.isNotBlank(internship.getJobTitle())) {
                            total = total + (0.5f / internships.size());
                        }
                        if (internship.getCompanyName() != null && StringUtils.isNotBlank(internship.getCompanyName())) {
                            total = total + (0.5f / internships.size());
                        }
                        if (internship.getLocation() != null && StringUtils.isNotBlank(internship.getLocation())) {
                            total = total + (0.5f / internships.size());
                        }
                        if (internship.getStartDate() != null && !internship.getStartDate().equals("")) {
                            total = total + (0.5f / internships.size());
                        }
                        if (internship.getEndDate() != null && !internship.getEndDate().equals("")) {
                            total = total + (0.5f / internships.size());
                        }
                        if (internship.getDescription() != null && StringUtils.isNotBlank(internship.getDescription())) {
                            total = total + (0.5f / internships.size());
                        }
                    }
                    if ((internships.size() % 2) != 0) {
                        total = Math.round(total);
                    }
                }
            }

            List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicantId);
            if (educationHistories != null && educationHistories.size() > 0) {
                if (educationHistories.size() == 1) {
                    EducationHistory educationHistory = educationHistories.get(0);
                    if (educationHistory.getCollegeName() != null && StringUtils.isNotBlank(educationHistory.getCollegeName())) {
                        total = total + 1;
                    }
                    if (educationHistory.getDegree() != null && StringUtils.isNotBlank(educationHistory.getDegree())) {
                        total = total + 1;
                    }
                    if (educationHistory.getField() != null && StringUtils.isNotBlank(educationHistory.getField())) {
                        total = total + 1;
                    }
                    if (educationHistory.getLocation() != null && StringUtils.isNotBlank(educationHistory.getLocation())) {
                        total = total + 1;
                    }
                    if (educationHistory.getStartDate() != null && !educationHistory.getStartDate().equals("")) {
                        total = total + 1;
                    }
                    if (educationHistory.getEndDate() != null && !educationHistory.getEndDate().equals("")) {
                        total = total + 1;
                    }
                    if (educationHistory.getDescription() != null && StringUtils.isNotBlank(educationHistory.getDescription())) {
                        total = total + 1;
                    }
                } else {
                    for (EducationHistory educationHistory : educationHistories) {
                        if (educationHistory.getCollegeName() != null && StringUtils.isNotBlank(educationHistory.getCollegeName())) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getDegree() != null && StringUtils.isNotBlank(educationHistory.getDegree())) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getField() != null && StringUtils.isNotBlank(educationHistory.getField())) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getLocation() != null && StringUtils.isNotBlank(educationHistory.getLocation())) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getStartDate() != null && !educationHistory.getStartDate().equals("")) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getEndDate() != null && !educationHistory.getEndDate().equals("")) {
                            total = total + (1f / educationHistories.size());
                        }
                        if (educationHistory.getDescription() != null && StringUtils.isNotBlank(educationHistory.getDescription())) {
                            total = total + (1f / educationHistories.size());
                        }
                    }
                    if ((educationHistories.size() % 2) != 0) {
                        total = Math.round(total);
                    }
                }
            }

            List<Project> projects = projectRepository.findByApplicantId(applicantId);
            if (projects != null && projects.size() > 0) {
                if (projects.size() == 1) {
                    Project project = projects.get(0);
                    if (project.getInstitutionName() != null && StringUtils.isNotBlank(project.getInstitutionName())) {
                        total = total + 0.5f;
                    }
                    if (project.getRole() != null && StringUtils.isNotBlank(project.getRole())) {
                        total = total + 0.5f;
                    }
                    if (project.getTitle() != null && StringUtils.isNotBlank(project.getTitle())) {
                        total = total + 0.5f;
                    }
                    if (project.getLocation() != null && StringUtils.isNotBlank(project.getLocation())) {
                        total = total + 0.5f;
                    }
                    if (project.getStartDate() != null && !project.getStartDate().equals("")) {
                        total = total + 0.5f;
                    }
                    if (project.getEndDate() != null && !project.getEndDate().equals("")) {
                        total = total + 0.5f;
                    }
                    if (project.getDescription() != null && StringUtils.isNotBlank(project.getDescription())) {
                        total = total + 0.5f;
                    }
                } else {
                    for (Project project : projects) {
                        if (project.getInstitutionName() != null && StringUtils.isNotBlank(project.getInstitutionName())) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getRole() != null && StringUtils.isNotBlank(project.getRole())) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getTitle() != null && StringUtils.isNotBlank(project.getTitle())) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getLocation() != null && StringUtils.isNotBlank(project.getLocation())) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getStartDate() != null && !project.getStartDate().equals("")) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getEndDate() != null && !project.getEndDate().equals("")) {
                            total = total + (0.5f / projects.size());
                        }
                        if (project.getDescription() != null && StringUtils.isNotBlank(project.getDescription())) {
                            total = total + (0.5f / projects.size());
                        }
                    }
                    if ((projects.size() % 2) != 0) {
                        double decimalPart = total - Math.floor(total);
                        if (decimalPart > 0.49 && decimalPart < 0.5) {
                            total = (float) (Math.ceil(total * 10.0) / 10.0);
                        } else {
                            total = (float) Math.round(total);
                        }
                    }
                }
            }

            List<Course> courses = courseRepository.findByApplicantId(applicantId);
            if (courses != null && courses.size() > 0) {
                if (courses.size() == 1) {
                    Course course = courses.get(0);
                    if (course.getInstitutionName() != null && StringUtils.isNotBlank(course.getInstitutionName())) {
                        total = total + 0.5f;
                    }
                    if (course.getCourseTitle() != null && StringUtils.isNotBlank(course.getCourseTitle())) {
                        total = total + 0.5f;
                    }
                    if (course.getCourseDuration() != null && StringUtils.isNotBlank(course.getCourseDuration())) {
                        total = total + 0.5f;
                    }
                } else {
                    for (Course course : courses) {
                        if (course.getInstitutionName() != null && StringUtils.isNotBlank(course.getInstitutionName())) {
                            total = total + (0.5f / courses.size());
                        }
                        if (course.getCourseTitle() != null && StringUtils.isNotBlank(course.getCourseTitle())) {
                            total = total + (0.5f / courses.size());
                        }
                        if (course.getCourseDuration() != null && StringUtils.isNotBlank(course.getCourseDuration())) {
                            total = total + (0.5f / courses.size());
                        }
                    }
                    if ((courses.size() % 2) != 0) {
                        double decimalPart = total - Math.floor(total);
                        if (decimalPart > 0.49 && decimalPart < 0.5) {
                            total = (float) (Math.ceil(total * 10.0) / 10.0);
                        } else {
                            total = (float) Math.round(total);
                        }
                    }
                }
            }

            List<Certification> certifications = certificateRepository.findByApplicantId(applicantId);
            if (certifications != null && certifications.size() > 0) {
                if (certifications.size() == 1) {
                    Certification certification = certifications.get(0);
                    if (certification.getTitle() != null && StringUtils.isNotBlank(certification.getTitle())) {
                        total = total + 0.5f;
                    }
                    if (certification.getValidFrom() != null && !certification.getValidFrom().equals("")) {
                        total = total + 0.5f;
                    }
                    if (certification.getValidTo() != null && !certification.getValidTo().equals("")) {
                        total = total + 0.5f;
                    }
                    if (certification.getDescription() != null && StringUtils.isNotBlank(certification.getDescription())) {
                        total = total + 0.5f;
                    }
                    if (certification.getUploadCertificate() != null && StringUtils.isNotBlank(certification.getUploadCertificate())) {
                        total = total + 0.5f;
                    }
                } else {
                    for (Certification certification : certifications) {
                        if (certification.getTitle() != null && StringUtils.isNotBlank(certification.getTitle())) {
                            total = total + (0.5f / certifications.size());
                        }
                        if (certification.getValidFrom() != null && !certification.getValidFrom().equals("")) {
                            total = total + (0.5f / certifications.size());
                        }
                        if (certification.getValidTo() != null && !certification.getValidTo().equals("")) {
                            total = total + (0.5f / certifications.size());
                        }
                        if (certification.getDescription() != null && StringUtils.isNotBlank(certification.getDescription())) {
                            total = total + (0.5f / certifications.size());
                        }
                        if (certification.getUploadCertificate() != null && StringUtils.isNotBlank(certification.getUploadCertificate())) {
                            total = total + (0.5f / certifications.size());
                        }
                    }
                    if ((certifications.size() % 2) != 0) {
                        double decimalPart = total - Math.floor(total);
                        if (decimalPart > 0.49 && decimalPart < 0.5) {
                            total = (float) (Math.ceil(total * 10.0) / 10.0);
                        } else {
                            total = (float) Math.round(total);
                        }
                    }
                }
            }

            List<Publication> publications = publicationRepository.findByApplicantId(applicantId);
            if (publications != null && publications.size() > 0) {
                Publication publication = publications.get(0);
                if (publication.getTitle() != null && StringUtils.isNotBlank(publication.getTitle())
                        && publication.getDescription() != null && StringUtils.isNotBlank(publication.getDescription())) {
                    total = total + 1;
                }
            }

            List<Reference> references = referenceRepository.findByApplicantId(applicantId);
            if (references != null && references.size() > 0) {
                if (references.size() == 1) {
                    Reference reference = references.get(0);
                    if (reference.getName() != null && StringUtils.isNotBlank(reference.getName())) {
                        total = total + 0.5f;
                    }
                    if (reference.getPhoneNumber() != null && StringUtils.isNotBlank(reference.getPhoneNumber())) {
                        total = total + 0.5f;
                    }
                    if (reference.getEmail() != null && StringUtils.isNotBlank(reference.getEmail())) {
                        total = total + 0.5f;
                    }
                    if (reference.getReferenceType() != null && StringUtils.isNotBlank(reference.getReferenceType())) {
                        total = total + 0.5f;
                    }
                    if (reference.getEmployer() != null && StringUtils.isNotBlank(reference.getEmployer())) {
                        total = total + 0.5f;
                    }
                    if (reference.getTitle() != null && StringUtils.isNotBlank(reference.getTitle())) {
                        total = total + 0.5f;
                    }
                } else {
                    for (Reference reference : references) {
                        if (reference.getName() != null && StringUtils.isNotBlank(reference.getName())) {
                            total = total + (0.5f / references.size());
                        }
                        if (reference.getPhoneNumber() != null && StringUtils.isNotBlank(reference.getPhoneNumber())) {
                            total = total + (0.5f / references.size());
                        }
                        if (reference.getEmail() != null && StringUtils.isNotBlank(reference.getEmail())) {
                            total = total + (0.5f / references.size());
                        }
                        if (reference.getReferenceType() != null && StringUtils.isNotBlank(reference.getReferenceType())) {
                            total = total + (0.5f / references.size());
                        }
                        if (reference.getEmployer() != null && StringUtils.isNotBlank(reference.getEmployer())) {
                            total = total + (0.5f / references.size());
                        }
                        if (reference.getTitle() != null && StringUtils.isNotBlank(reference.getTitle())) {
                            total = total + (0.5f / references.size());
                        }
                    }
                    if ((references.size() % 2) != 0) {
                        total = Math.round(total);
                    }
                }
            }

            List<SocialMediaLinks> socialMediaLinks = socialMediaLinksRepository.findByApplicantId(applicantId);
            if (socialMediaLinks != null && socialMediaLinks.size() > 0) {
                SocialMediaLinks socialMediaLink = socialMediaLinks.get(0);
                if (socialMediaLink.getGithubLink() != null && StringUtils.isNotBlank(socialMediaLink.getGithubLink())) {
                    total = total + 0.5f;
                }
                if (socialMediaLink.getLinkedinLink() != null && StringUtils.isNotBlank(socialMediaLink.getLinkedinLink())) {
                    total = total + 0.5f;
                }
                if (socialMediaLink.getTwitterLink() != null && StringUtils.isNotBlank(socialMediaLink.getTwitterLink())) {
                    total = total + 0.5f;
                }
                if (socialMediaLink.getWebsiteLink() != null && StringUtils.isNotBlank(socialMediaLink.getWebsiteLink())) {
                    total = total + 0.5f;
                }
                if (socialMediaLink.getBlogLink() != null && StringUtils.isNotBlank(socialMediaLink.getBlogLink())) {
                    total = total + 0.5f;
                }
                if (socialMediaLink.getBehanceLink() != null && StringUtils.isNotBlank(socialMediaLink.getBehanceLink())) {
                    total = total + 0.5f;
                }
            }

            if (applicant.getLanguages() != null && StringUtils.isNotBlank(applicant.getLanguages())) {
                String[] languages = applicant.getLanguages().split(",");
                if (languages.length > 0) {
                    total = total + 1;
                }
            }
            if (applicant.getResumeURL() != null && StringUtils.isNotBlank(applicant.getResumeURL())) {
                total = total + 0.5f;
            }
            if (applicant.getResumeVideoURL() != null && StringUtils.isNotBlank(applicant.getResumeVideoURL())) {
                total = total + 0.5f;
            }
            if (applicant.getResumeUploadId() != null && StringUtils.isNotBlank(applicant.getResumeUploadId())) {
                total = total + 3;
            }
            if (applicant.getUploadId() != null && StringUtils.isNotBlank(applicant.getUploadId())) {
                total = total + 3;
            }
            if (applicant.getPassportUploadId() != null && StringUtils.isNotBlank(applicant.getPassportUploadId())) {
                total = total + 3;
            }
            if (applicant.getUploadAdditionalDocId() != null && StringUtils.isNotBlank(applicant.getUploadAdditionalDocId())) {
                total = total + 1;
            }
            return total;
        }
        return 0;
    }

    public ApplicantDTO profile(Long applicantId) throws WorkruitException {
        Optional<Applicant> optional = applicantRepository.findById(applicantId);

        ApplicantDTO applicantDTO = new ApplicantDTO();
        if (optional.isPresent()) {
            Applicant applicant = optional.get();
            applicantDTO.setCountry(applicant.getCountry());
            applicantDTO.setEmail(applicant.getEmail());
            applicantDTO.setFirstName(applicant.getFirstName());
            applicantDTO.setLastName(applicant.getLastName());
            applicantDTO.setGender(applicant.getGender());
            applicantDTO.setSkills(applicant.getSkills());
            User user = userRepository.getById(applicant.getConsultancyUserId());
            applicantDTO.setUploadedBy(user.getFirstName() + " " + user.getLastName());
            String imageUrl = applicant.getProfileImageUrl();
            applicantDTO.setProfileImageUrl(imageUrl);
            try {
                applicantDTO.setProfileImageData(imageUrl != null ? imageService.getImage(imageUrl) : CommonConstants.applicant_default_image);
            } catch (IOException e) {
                applicantDTO.setProfileImageData(null);
                e.printStackTrace();
            }
            applicantDTO.setDob(applicant.getDob());
            applicantDTO.setTelephoneNumber(applicant.getPhoneNumber());
            applicantDTO.setCountryCode(applicant.getCountryCode());
            applicantDTO.setEthnicity(applicant.getEthnicity());
            applicantDTO.setLocation(applicant.getLocation());
            applicantDTO.setProfileSummary(applicant.getProfileSummary());
            applicantDTO.setConsultantId(applicant.getConsultancyId());
            Optional<Consultancy> c = consultancyRepository.findById(applicant.getConsultancyId());
            if (c.isPresent()) {
                Consultancy con = c.get();
                applicantDTO.setConsultantName(con.getName());
            }
            applicantDTO.setWorkExperiences(workExperienceService.getApplicantWorkExperience(applicantId, applicant.getConsultancyId()));
            applicantDTO.setInternships(internshipService.getInternship(applicantId, applicant.getConsultancyId()));
            applicantDTO.setCourses(courseService.getApplicantCourse(applicantId, applicant.getConsultancyId()));
            applicantDTO.setEducationHistorys(educationHistoryService.getApplicantEducationHistory(applicantId, applicant.getConsultancyId()));
            applicantDTO.setProjects(projectService.getApplicantProjects(applicantId, applicant.getConsultancyId()));
            applicantDTO.setDetails(applicantDetailsService.getApplicantDetails(applicantId));
            applicantDTO.setPublications(publicationService.getPublications(applicantId, applicant.getConsultancyId()));
            applicantDTO.setCertifications(certificateService.getApplicantCertificate(applicantId, applicant.getConsultancyId()));
            applicantDTO.setReferences(referenceService.getReferences(applicantId, applicant.getConsultancyId()));
            applicantDTO.setProfileCompletionPercentage(profileCompletionStatus(applicantId));

            List<SocialMediaLinks> socialMediaLinks = socialMediaLinksRepository.findByApplicantId(applicantId);
            applicantDTO.setCorrectionNeeded(!isCorrectionSolved(applicant.getApplicantId()));
            applicantDTO.setSocialMediaLinks(socialMediaLinks);
            applicantDTO.setResumeURL(applicant.getResumeURL());
            applicantDTO.setResumeUploadId(applicant.getResumeUploadId());
            applicantDTO.setPassportUploadId(applicant.getPassportUploadId());
            applicantDTO.setUploadAdditionalDocId(applicant.getUploadAdditionalDocId());
            applicantDTO.setResumeVideoURL(applicant.getResumeVideoURL());
            return applicantDTO;
        }
        return applicantDTO;
    }

    @Transactional
    public void saveApplicants(MultipartFile multipartFile, long userId) throws Exception {
        InputStream inputStream = multipartFile.getInputStream();
        long time = System.currentTimeMillis();
        File file = new File(time + ".csv");
        OutputStream outStream = new FileOutputStream(file);

        byte[] buffer = new byte[20 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        outStream.close();
        List<CSVRecord> records = processFile(file, null, null, null, userId);

    }

    @Transactional
    public List<CSVRecord> processFile(File file, Long userImportId, Long consultancyId, Long version, Long userId)
            throws Exception {
        CSVParser csvParser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
        List<CSVRecord> records = csvParser.getRecords();
        int index = 0;

        List<String> existingRecords = new ArrayList<>();
        List<String> existingRecordsMobile = new ArrayList<>();
        List<String> correctionRequiredRecords = new ArrayList<>();
        for (CSVRecord record : records) {
            boolean correction = false;
            if (applicantRepository.findByEmail(record.get("Email")) != null) {
                logger.error("Applicant already exist with this email:\n " + (record.get("Email")));
                if (existingRecords.size() == 0)
                    existingRecords.add("Account already exists with this email:\n " + record.get("Email"));
                else
                    existingRecords.add("\n" + record.get("Email"));
                index++;
                continue;
            }
            if (applicantRepository.findByPhoneNumber(record.get("Number")) != null) {
                logger.error("Applicant already exist with this number:\n " + (record.get("Number")));
                if (existingRecordsMobile.size() == 0)
                    existingRecordsMobile.add("Account already exists with this number:\n " + record.get("Number"));
                else
                    existingRecordsMobile.add("\n" + record.get("Number"));
                index++;
                continue;
            }
            Applicant applicant = new Applicant();
            applicant.setConsultancyId(consultancyId);
            if (record.isMapped("First Name") && record.get("First Name") != null && StringUtils.isNotBlank(record.get("First Name"))) {
                applicant.setFirstName(record.get("First Name").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Last Name") && record.get("Last Name") != null && StringUtils.isNotBlank(record.get("Last Name"))) {
                applicant.setLastName(record.get("Last Name").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Profile Summary") && record.get("Profile Summary") != null && StringUtils.isNotBlank(record.get("Profile Summary"))) {
                applicant.setProfileSummary(record.get("Profile Summary").trim());
            } else {
                correction = true;
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            if (record.isMapped("Date of Birth") && record.get("Date of Birth").trim() != null && StringUtils.isNotBlank(record.get("Date of Birth").trim()) && isDateFormatSame(record.get("Date of Birth").trim(), simpleDateFormat)) {
                applicant.setDob(simpleDateFormat.parse(record.get("Date of Birth").trim()));
            } else {
                correction = true;
            }
            if (record.isMapped("Email") && record.get("Email") != null && StringUtils.isNotBlank(record.get("Email"))) {
                applicant.setEmail(record.get("Email").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Number") && record.get("Number") != null && StringUtils.isNotBlank(record.get("Number"))) {
                applicant.setPhoneNumber(record.get("Number").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Country Code") && record.get("Country Code") != null && StringUtils.isNotBlank(record.get("Country Code"))) {
                applicant.setCountryCode(record.get("Country Code").trim());
            } else {
                correction = true;
            }
//            if (record.isMapped("Number") && record.get("Number") != null && StringUtils.isNotBlank(record.get("Number"))) {
//                applicant.setNumber(record.get("Number").trim());
//            } else {
//                correction = true;
//            }
            if (record.isMapped("Location") && record.get("Location") != null && StringUtils.isNotBlank(record.get("Location"))) {
                applicant.setLocation(record.get("Location").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Gender") && record.get("Gender") != null && StringUtils.isNotBlank(record.get("Gender")) && CommonConstants.genderValues.contains(record.get("Gender").trim())) {
                applicant.setGender(record.get("Gender").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Ethnicity") && record.get("Ethnicity") != null && StringUtils.isNotBlank(record.get("Ethnicity")) && CommonConstants.ethinicityValues.contains(record.get("Ethnicity").trim())) {
                applicant.setEthnicity(record.get("Ethnicity").trim());
            } else {
                correction = true;
            }
            applicant.setCreatedDate(new Date());
            applicant.setUpdatedDate(new Date());
            applicant.setVersion(version);
            applicant.setPassword(passwordEncoder.encode("Applicant"));
            applicant.setStatus(1);
            applicant.setConsultancyUserId(userId);
            if (record.isMapped("Languages") && record.get("Languages") != null && StringUtils.isNotBlank(record.get("Languages"))) {
                applicant.setLanguages(record.get("Languages").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Share URL - Resume") && record.get("Share URL - Resume") != null && StringUtils.isNotBlank(record.get("Share URL - Resume"))) {
                applicant.setResumeURL(record.get("Share URL - Resume").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Share URL - Video") && record.get("Share URL - Video") != null && StringUtils.isNotBlank(record.get("Share URL - Video"))) {
                applicant.setResumeVideoURL(record.get("Share URL - Video").trim());
            } else {
                correction = true;
            }
            applicantRepository.save(applicant);

            ApplicantDetails ad = new ApplicantDetails();
            if (record.isMapped("Career Level") && record.get("Career Level") != null && StringUtils.isNotBlank(record.get("Career Level"))) {
                ad.setCareerLevel(record.get("Career Level").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Years of experience") && record.get("Years of experience") != null && StringUtils.isNotBlank(record.get("Years of experience"))) {

                ad.setYearsOfExperience(record.get("Years of experience").trim());
                //ad.setYearsOfExperience(Float.parseFloat(record.get("Years of experience").trim()));
            } else {
                correction = true;
            }
            if (record.isMapped("Current salary") && record.get("Current salary") != null && StringUtils.isNotBlank(record.get("Current salary"))) {

                String currentSalary = record.get("Current salary").trim();
                boolean validSalary = isNumber(currentSalary);
                ad.setCurrentSalary(validSalary ? currentSalary : null);
            } else {
                correction = true;
            }
            if (record.isMapped("Hide Current salary") && record.get("Hide Current salary") != null && StringUtils.isNotBlank(record.get("Hide Current salary"))) {
                ad.setHideSalary(Boolean.parseBoolean(record.get("Hide Current salary").trim()));
            } else {
                correction = true;
            }
            if (record.isMapped("Target Salary (Min - Max)") && record.get("Target Salary (Min - Max)") != null && StringUtils.isNotBlank(record.get("Target Salary (Min - Max)"))) {
                ad.setExpectedSalary(record.get("Target Salary (Min - Max)").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Current Salary Rate") && record.get("Current Salary Rate") != null && StringUtils.isNotBlank(record.get("Current Salary Rate")) && CommonConstants.salaryRateValues.contains(record.get("Current Salary Rate").trim())) {
                ad.setSalaryRate(record.get("Current Salary Rate").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Hide Target Salary") && record.get("Hide Target Salary") != null && StringUtils.isNotBlank(record.get("Hide Target Salary"))) {
                ad.setHideExpectedSalary(Boolean.parseBoolean(record.get("Hide Target Salary").trim()));
            } else {
                correction = true;
            }
            if (record.isMapped("Target Salary Rate") && record.get("Target Salary Rate") != null && StringUtils.isNotBlank(record.get("Target Salary Rate")) && CommonConstants.salaryRateValues.contains(record.get("Target Salary Rate").trim())) {
                ad.setExpectedSalaryRate(record.get("Target Salary Rate").trim());
            } else {
                correction = true;
            }

            if (record.isMapped("Primary Job Function") && record.get("Primary Job Function") != null && !record.get("Primary Job Function").equals("")) {
                // String[] jobFunctions = record.get("Primary Job Function").trim().split(",");
                String jobFunction = record.get("Primary Job Function").trim();
                String existJobFunction = "";
                //if (jobFunctions != null && jobFunctions.length > 0) {
                // for (String jobFunction : jobFunctions) {
                JobFunction jobFunctionModel = jobFunctionRepository
                        .findByJobFunctionName(jobFunction.trim());
                if (jobFunctionModel != null && jobFunctionModel.getJobFunctionName().equals(jobFunction)) {
                    ApplicantJobFunction applicantJobFunction = new ApplicantJobFunction();
                    applicantJobFunction.setApplicantId(applicant.getApplicantId());
                    applicantJobFunction.setJobFunctionId(jobFunctionModel.getJobFunctionId());
                    applicantJobFunction.setCreatedDate(new Date());
                    applicantJobFunction.setUpdatedDate(new Date());
                    applicantJobFunctionRepository.save(applicantJobFunction);
                    if (!existJobFunction.isEmpty()) {
                        existJobFunction += ",";
                    }
                    existJobFunction += jobFunction.trim();
                    ad.setJobFunctionId(Long.valueOf(jobFunctionModel.getJobFunctionId()));
                } else {
                    correction = true;
                }
                // }
                ad.setJobFunction(existJobFunction);
                // }
            } else {
                correction = true;
            }

            if (record.isMapped("Secondary Job Function") && record.get("Secondary Job Function") != null && !record.get("Secondary Job Function").equals("")) {
                String[] jobFunctions = record.get("Secondary Job Function").trim().split(",");
                String existJobFunction = "";
                String secJobFunctionIds = "";
                if (jobFunctions != null && jobFunctions.length > 0) {
                    for (String jobFunction : jobFunctions) {
                        JobFunction jobFunctionModel = jobFunctionRepository
                                .findByJobFunctionName(jobFunction.trim());
                        if (jobFunctionModel != null && jobFunctionModel.getJobFunctionName().equals(jobFunction)) {
                            ApplicantSecondaryJobFunction applicantSecondaryJobFunction = new ApplicantSecondaryJobFunction();
                            applicantSecondaryJobFunction.setApplicantId(applicant.getApplicantId());
                            applicantSecondaryJobFunction.setJobFunctionId(jobFunctionModel.getJobFunctionId());
                            applicantSecondaryJobFunction.setCreatedDate(new Date());
                            applicantSecondaryJobFunction.setUpdatedDate(new Date());
                            applicantSecondaryJobFunctionRepository.save(applicantSecondaryJobFunction);
                            if (!existJobFunction.isEmpty()) {
                                existJobFunction += ",";
                                secJobFunctionIds += ",";
                            }
                            existJobFunction += jobFunction.trim();
                            secJobFunctionIds += jobFunctionModel.getJobFunctionId();
                        } else {
                            correction = true;
                        }
                    }
                    ad.setSecondaryJobFunctionIds(secJobFunctionIds);
                    ad.setSecondaryJobFunction(existJobFunction);
                }
            } else {
                correction = true;
            }

            if (record.isMapped("Skills") && record.get("Skills") != null && !record.get("Skills").equals("")) {
                String[] jobSkills = record.get("Skills").trim().split(",");
                int count = 0;
                String existJobSkills = "";
                String skillIds = "";
                for (String jobSkill : jobSkills) {
                    JobSkills jobSkillModel = jobSkillsRepository.findBySkillName(jobSkill.trim());
                    if (jobSkillModel != null && jobSkillModel.getSkillName().equals(jobSkill.trim())) {
                        ApplicantJobSkill applicantJobSkill = new ApplicantJobSkill();
                        applicantJobSkill.setApplicantId(applicant.getApplicantId());
                        applicantJobSkill.setJobSkillId(jobSkillModel.getSkillId());
                        applicantJobSkill.setCreatedDate(new Date());
                        applicantJobSkill.setUpdatedDate(new Date());
                        applicantJobSkillRepository.save(applicantJobSkill);
                        if (!existJobSkills.isEmpty()) {
                            existJobSkills += ",";
                            skillIds += ",";
                        }
                        existJobSkills += jobSkill.trim();
                        skillIds += jobSkillModel.getSkillId();
                        count++;
                    } else {
                        correction = true;
                    }
                }
                ad.setSkillIds(skillIds);
                ad.setSkills(existJobSkills);
                if (count < 5 || count > 20) {
                    correction = true;
                }
            } else {
                correction = true;
            }

            if (record.isMapped("Preferred work mode") && record.get("Preferred work mode") != null && StringUtils.isNotBlank(record.get("Preferred work mode")) && WorkMode.getValueOf(record.get("Preferred work mode").trim()) != -1
                    && CommonConstants.workModeValues.contains(record.get("Preferred work mode").trim())) {
                ad.setPreferredWorkMode(record.get("Preferred work mode").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Job Type") && record.get("Job Type") != null && StringUtils.isNotBlank(record.get("Job Type")) && JobType.getValueOf(record.get("Job Type").trim()) != -1 && CommonConstants.jobTypeValues.contains(record.get("Job Type").trim())) {
                ad.setJobType(record.get("Job Type").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Notice Period") && record.get("Notice Period") != null && StringUtils.isNotBlank(record.get("Notice Period")) && NoticePeriod.getValueOf(record.get("Notice Period").trim()) != -1
                    && CommonConstants.noticePeriodValues.contains(record.get("Notice Period").trim())) {
                ad.setNoticePeriod(record.get("Notice Period").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Current status") && record.get("Current status") != null && StringUtils.isNotBlank(record.get("Current status"))) {
                ad.setCurrentWorkStatus(record.get("Current status").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Citizenship") && record.get("Citizenship") != null && StringUtils.isNotBlank(record.get("Citizenship")) && Citizenship.getValueOf(record.get("Citizenship").trim()) != -1
                    && CommonConstants.citizenshipValues.contains(record.get("Citizenship").trim())) {
                ad.setCitizenship(record.get("Citizenship").trim());
            } else {
                correction = true;
            }
            if (record.isMapped("Are you willing to relocate?") && record.get("Are you willing to relocate?") != null && StringUtils.isNotBlank(record.get("Are you willing to relocate?"))) {
                ad.setWillingToRelocate(record.get("Are you willing to relocate?").trim().equalsIgnoreCase("Yes"));
            } else {
                correction = true;
            }
            if (record.isMapped("Relocation") && record.get("Relocation") != null && StringUtils.isNotBlank(record.get("Relocation"))) {
                if (record.get("Are you willing to relocate?").trim().equalsIgnoreCase("Yes") && CommonConstants.relocationValues.contains(record.get("Relocation").trim())) {
                    ad.setRelocation(record.get("Relocation").trim());
                }
            } else {
                correction = true;
            }
            if (record.isMapped("Degree") && record.get("Degree") != null && StringUtils.isNotBlank(record.get("Degree"))) {
                Degrees degrees = degreesRepository.findByShortTitle(record.get("Degree").trim());
                if (degrees != null) {
                    ad.setDegreeId(Long.valueOf(degrees.getDegreeId()));
                }
            }
            ad.setApplicantId(applicant.getApplicantId());
            applicantDetailsRepository.save(ad);

            WorkExperience workExperience = new WorkExperience();
            boolean experienceFieldsExsists = false;
            workExperience.setApplicantId(applicant.getApplicantId());
            if (record.isMapped("Job Company") && record.get("Job Company") != null && StringUtils.isNotBlank(record.get("Job Company"))) {
                workExperience.setCompanyName(record.get("Job Company").trim());
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Job Title") && record.get("Job Title") != null && StringUtils.isNotBlank(record.get("Job Title"))) {
                workExperience.setJobTitle(record.get("Job Title").trim());
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Job Location") && record.get("Job Location") != null && StringUtils.isNotBlank(record.get("Job Location"))) {
                workExperience.setLocation(record.get("Job Location").trim());
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Job Start date") && record.get("Job Start date") != null && StringUtils.isNotBlank(record.get("Job Start date")) && isDateFormatSame(record.get("Job Start date"), simpleDateFormat)) {
                workExperience.setStartDate(simpleDateFormat.parse(record.get("Job Start date").trim()));
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Job End Date") && record.get("Job End Date") != null && StringUtils.isNotBlank(record.get("Job End Date")) && isDateFormatSame(record.get("Job End Date"), simpleDateFormat)) {
                workExperience.setEndDate(simpleDateFormat.parse(record.get("Job End Date").trim()));
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Job Currently working?") && record.get("Job Currently working?") != null && StringUtils.isNotBlank(record.get("Job Currently working?"))) {
                workExperience.setCurrentlyWorkingHere(Boolean.parseBoolean(record.get("Job Currently working?").trim()));
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Work Description") && record.get("Work Description") != null && StringUtils.isNotBlank(record.get("Work Description"))) {
                workExperience.setDescription(record.get("Work Description").trim());
                experienceFieldsExsists = true;
            } else {
                correction = true;
            }
            if (experienceFieldsExsists)
                workExperienceRepository.save(workExperience);

            Internship internship = new Internship();
            internship.setApplicantId(applicant.getApplicantId());
            boolean intershipFieldsExsists = false;
            if (record.isMapped("Internship Company") && record.get("Internship Company") != null && StringUtils.isNotBlank(record.get("Internship Company"))) {
                internship.setCompanyName(record.get("Internship Company").trim());
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            internship.setCreatedDate(new Date());
            internship.setUpdatedDate(new Date());
            if (record.isMapped("Internship End Date (Mon-Year)") && record.get("Internship End Date (Mon-Year)") != null && StringUtils.isNotBlank(record.get("Internship End Date (Mon-Year)")) && isDateFormatSame(record.get("Internship End Date (Mon-Year)"), simpleDateFormat)) {
                internship.setEndDate(simpleDateFormat.parse(record.get("Internship End Date (Mon-Year)").trim()));
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Internship Job Title") && record.get("Internship Job Title") != null && StringUtils.isNotBlank(record.get("Internship Job Title"))) {
                internship.setJobTitle(record.get("Internship Job Title").trim());
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Internship Location") && record.get("Internship Location") != null && StringUtils.isNotBlank(record.get("Internship Location"))) {
                internship.setLocation(record.get("Internship Location").trim());
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Internship Start Date (Mon-Year)") && record.get("Internship Start Date (Mon-Year)") != null && StringUtils.isNotBlank(record.get("Internship Start Date (Mon-Year)")) && isDateFormatSame(record.get("Internship Start Date (Mon-Year)"), simpleDateFormat)) {
                internship.setStartDate(simpleDateFormat.parse(record.get("Internship Start Date (Mon-Year)").trim()));
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Internship Description") && record.get("Internship Description") != null && StringUtils.isNotBlank(record.get("Internship Description"))) {
                internship.setDescription(record.get("Internship Description").trim());
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Internship Currently working?") && record.get("Internship Currently working?") != null && StringUtils.isNotBlank(record.get("Internship Currently working?"))) {
                internship.setCurrentlyWorkingHere(Boolean.parseBoolean(record.get("Internship Currently working?").trim()));
                intershipFieldsExsists = true;
            } else {
                correction = true;
            }
            if (intershipFieldsExsists)
                internshipRepository.save(internship);

            EducationHistory educationHistory = new EducationHistory();
            boolean educationFieldsExsists = false;
            if (record.isMapped("College Name") && record.get("College Name") != null && StringUtils.isNotBlank(record.get("College Name"))) {
                educationHistory.setCollegeName(record.get("College Name").trim());
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Degree") && record.get("Degree") != null && StringUtils.isNotBlank(record.get("Degree")) && degreesRepository.findByShortTitle(record.get("Degree").trim()) != null) {
                educationHistory.setDegree(record.get("Degree").trim());
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Field of Study") && record.get("Field of Study") != null && StringUtils.isNotBlank(record.get("Field of Study"))) {
                educationHistory.setField(record.get("Field of Study").trim());
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("College Location") && record.get("College Location") != null && StringUtils.isNotBlank(record.get("College Location"))) {
                educationHistory.setLocation(record.get("College Location").trim());
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("College Start date") && record.get("College Start date") != null && StringUtils.isNotBlank(record.get("College Start date")) && isDateFormatSame(record.get("College Start date"), simpleDateFormat)) {
                educationHistory.setStartDate(simpleDateFormat.parse(record.get("College Start date").trim()));
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("College End date") && record.get("College End date") != null && StringUtils.isNotBlank(record.get("College End date")) && isDateFormatSame(record.get("College End date"), simpleDateFormat)) {
                educationHistory.setEndDate(simpleDateFormat.parse(record.get("College End date").trim()));
            } else {
                correction = true;
            }
            if (record.isMapped("College Description") && record.get("College Description") != null && StringUtils.isNotBlank(record.get("College Description"))) {
                educationHistory.setDescription(record.get("College Description").trim());
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Currently studying?") && record.get("Currently studying?") != null && StringUtils.isNotBlank(record.get("Currently studying?"))) {
                educationHistory.setCurrentlyStudying(Boolean.parseBoolean(record.get("Currently studying?").trim()));
                educationFieldsExsists = true;
            } else {
                correction = true;
            }
            educationHistory.setApplicantId(applicant.getApplicantId());
            if (educationFieldsExsists)
                educationHistoryRepository.save(educationHistory);

            Project project = new Project();
            project.setApplicantId(applicant.getApplicantId());
            boolean projectFieldsExsists = false;
            project.setCreatedDate(new Date());
            if (record.isMapped("Project Description") && record.get("Project Description") != null && StringUtils.isNotBlank(record.get("Project Description"))) {
                project.setDescription(record.get("Project Description").trim());
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Project End Date") && record.get("Project End Date") != null && StringUtils.isNotBlank(record.get("Project End Date")) && isDateFormatSame(record.get("Project End Date"), simpleDateFormat)) {
                project.setEndDate(simpleDateFormat.parse(record.get("Project End Date").trim()));
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Name of Institution") && record.get("Name of Institution") != null && StringUtils.isNotBlank(record.get("Name of Institution"))) {
                project.setInstitutionName(record.get("Name of Institution").trim());
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Project Location") && record.get("Project Location") != null && StringUtils.isNotBlank(record.get("Project Location"))) {
                project.setLocation(record.get("Project Location").trim());
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("On going project?") && record.get("On going project?") != null && StringUtils.isNotBlank(record.get("On going project?"))) {
                project.setProjectOngoing(Boolean.parseBoolean(record.get("On going project?").trim()));
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Role") && record.get("Role") != null && StringUtils.isNotBlank(record.get("Role"))) {
                project.setRole(record.get("Role").trim());
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Project Start Date") && record.get("Project Start Date") != null && StringUtils.isNotBlank(record.get("Project Start Date")) && isDateFormatSame(record.get("Project Start Date"), simpleDateFormat)) {
                project.setStartDate(simpleDateFormat.parse(record.get("Project Start Date").trim()));
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Project Title") && record.get("Project Title") != null && StringUtils.isNotBlank(record.get("Project Title"))) {
                project.setTitle(record.get("Project Title").trim());
                projectFieldsExsists = true;
            } else {
                correction = true;
            }
            project.setUpdatedDate(new Date());
            if (projectFieldsExsists)
                projectRepository.save(project);

            Course course = new Course();
            course.setApplicantId(applicant.getApplicantId());
            boolean courseFieldsExsists = false;
            if (record.isMapped("Course Duration") && record.get("Course Duration") != null && StringUtils.isNotBlank(record.get("Course Duration"))) {
                course.setCourseDuration(record.get("Course Duration").trim());
                courseFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Course Title") && record.get("Course Title") != null && StringUtils.isNotBlank(record.get("Course Title"))) {
                course.setCourseTitle(record.get("Course Title").trim());
                courseFieldsExsists = true;
            } else {
                correction = true;
            }
            course.setCreatedDate(new Date());
            if (record.isMapped("Institute Name") && record.get("Institute Name") != null && StringUtils.isNotBlank(record.get("Institute Name"))) {
                course.setInstitutionName(record.get("Institute Name").trim());
                courseFieldsExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Still pursuing?") && record.get("Still pursuing?") != null && StringUtils.isNotBlank(record.get("Still pursuing?"))) {
                course.setStillPursuing(Boolean.parseBoolean(record.get("Still pursuing?").trim()));
                courseFieldsExsists = true;
            } else {
                correction = true;
            }
            course.setUpdatedDate(new Date());
            if (courseFieldsExsists)
                courseRepository.save(course);

            Reference reference = new Reference();
            boolean referenceFieldExsists = false;
            reference.setCreatedDate(new Date());
            if (record.isMapped("Reference Email") && record.get("Reference Email") != null && StringUtils.isNotBlank(record.get("Reference Email"))) {
                reference.setEmail(record.get("Reference Email").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Reference Employer") && record.get("Reference Employer") != null && StringUtils.isNotBlank(record.get("Reference Employer"))) {
                reference.setEmployer(record.get("Reference Employer").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Reference Name") && record.get("Reference Name") != null && StringUtils.isNotBlank(record.get("Reference Name"))) {
                reference.setName(record.get("Reference Name").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Reference Phone") && record.get("Reference Phone") != null && StringUtils.isNotBlank(record.get("Reference Phone"))) {
                reference.setPhoneNumber(record.get("Reference Phone").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Reference Type") && record.get("Reference Type") != null && StringUtils.isNotBlank(record.get("Reference Type"))) {
                reference.setReferenceType(record.get("Reference Type").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Reference Title") && record.get("Reference Title") != null && StringUtils.isNotBlank(record.get("Reference Title"))) {
                reference.setTitle(record.get("Reference Title").trim());
                referenceFieldExsists = true;
            } else {
                correction = true;
            }
            reference.setUpdatedDate(new Date());
            reference.setApplicantId(applicant.getApplicantId());
            if (referenceFieldExsists)
                referenceRepository.save(reference);

            SocialMediaLinks socialMediaLinks = new SocialMediaLinks();
            socialMediaLinks.setApplicantId(applicant.getApplicantId());
            if (record.isMapped("Github") && record.get("Github") != null && StringUtils.isNotBlank(record.get("Github"))) {
                socialMediaLinks.setGithubLink(record.get("Github").trim());
            } else {
                // correction = true;
            }
            if (record.isMapped("LinkedIn") && record.get("LinkedIn") != null && StringUtils.isNotBlank(record.get("LinkedIn"))) {
                socialMediaLinks.setLinkedinLink(record.get("LinkedIn").trim());
            } else {
                //correction = true;
            }
            if (record.isMapped("Twitter") && record.get("Twitter") != null && StringUtils.isNotBlank(record.get("Twitter"))) {
                socialMediaLinks.setTwitterLink(record.get("Twitter").trim());
            } else {
                //correction = true;
            }
            if (record.isMapped("Website URL") && record.get("Website URL") != null && StringUtils.isNotBlank(record.get("Website URL"))) {
                socialMediaLinks.setWebsiteLink(record.get("Website URL").trim());
            } else {
                // correction = true;
            }
            if (record.isMapped("Blog URL") && record.get("Blog URL") != null && StringUtils.isNotBlank(record.get("Blog URL"))) {
                socialMediaLinks.setBlogLink(record.get("Blog URL").trim());
            } else {
                //correction = true;
            }
            if (record.isMapped("Behance URL") && record.get("Behance URL") != null && StringUtils.isNotBlank(record.get("Behance URL"))) {
                socialMediaLinks.setBehanceLink(record.get("Behance URL").trim());
            } else {
                // correction = true;
            }
            socialMediaLinksRepository.save(socialMediaLinks);

            Publication publication = new Publication();
            publication.setApplicantId(applicant.getApplicantId());
            boolean publicationFieldExsists = false;
            if (record.isMapped("Publications Title") && record.get("Publications Title") != null && StringUtils.isNotBlank(record.get("Publications Title"))) {
                publication.setTitle(record.get("Publications Title").trim());
                publicationFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Publications") && record.get("Publications") != null && StringUtils.isNotBlank(record.get("Publications"))) {
                publication.setDescription(record.get("Publications").trim());
                publicationFieldExsists = true;
            } else {
                correction = true;
            }
            publication.setCreatedDate(new Date());
            publication.setUpdatedDate(new Date());
            if (publicationFieldExsists)
                publicationRepository.save(publication);

            Certification certification = new Certification();
            certification.setApplicantId(applicant.getApplicantId());
            certification.setCreatedDate(new Date());
            certification.setUpdatedDate(new Date());
            boolean certificatoinFieldExsists = false;
            if (record.isMapped("Certificate/License title") && record.get("Certificate/License title") != null && StringUtils.isNotBlank(record.get("Certificate/License title"))) {
                certification.setTitle(record.get("Certificate/License title").trim());
                certificatoinFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Description") && record.get("Description") != null && StringUtils.isNotBlank(record.get("Description"))) {
                certification.setDescription(record.get("Description").trim());
                certificatoinFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Valid from") && record.get("Valid from") != null && StringUtils.isNotBlank(record.get("Valid from")) && isDateFormatSame(record.get("Valid from"), simpleDateFormat)) {
                certification.setValidFrom(simpleDateFormat.parse(record.get("Valid from").trim()));
                certificatoinFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Valid till") && record.get("Valid till") != null && StringUtils.isNotBlank(record.get("Valid till")) && isDateFormatSame(record.get("Valid till"), simpleDateFormat)) {
                certification.setValidTo(simpleDateFormat.parse(record.get("Valid till").trim()));
                certificatoinFieldExsists = true;
            } else {
                correction = true;
            }
            if (record.isMapped("Does not expire") && record.get("Does not expire") != null && StringUtils.isNotBlank(record.get("Does not expire"))) {
                certification.setDoesNotExpire(Boolean.parseBoolean(record.get("Does not expire").trim()));
                certificatoinFieldExsists = true;
            } else {
                correction = true;
            }
            if (certificatoinFieldExsists)
                certificateRepository.save(certification);

//            if (correction) {
//                applicant.setCorrectionRequired(false);
////                if (correctionRequiredRecords.size() == 0)
////                    correctionRequiredRecords.add("Few data fields are needed correction for: " + record.get("Email"));
////                else
////                    correctionRequiredRecords.add("\n " + record.get("Email"));
//            } else {
//                applicant.setCorrectionRequired(false);
//            }


            correction = isCorrectionSolved(applicant.getApplicantId());
            applicant.setCorrectionRequired(!correction);
            applicantRepository.save(applicant);
            index++;
            if (index % 10 == 0) {
                saveUserImportStatus(index, records.size(), userImportId, existingRecords, existingRecordsMobile, correctionRequiredRecords);
            }
            if (correction) {
                jobPostService.runJobMatcherForApplicant(applicant.getApplicantId(), userId, 100, false);
            }
        }
        //records.size() - existingRecords.size()
        if (!existingRecords.isEmpty()) {
            saveUserImportStatus(index, records.size(), userImportId, existingRecords, existingRecordsMobile, correctionRequiredRecords);
        } else if (!existingRecordsMobile.isEmpty()) {
            saveUserImportStatus(index, records.size(), userImportId, existingRecords, existingRecordsMobile, correctionRequiredRecords);
        } else if (!correctionRequiredRecords.isEmpty()) {
            saveUserImportStatus(index, records.size(), userImportId, existingRecords, existingRecordsMobile, correctionRequiredRecords);
        } else {
            saveUserImportStatus(records.size(), records.size(), userImportId, existingRecords, existingRecordsMobile, correctionRequiredRecords);
        }


        return records;
    }

    private boolean isNumber(String s) {
        for (int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i)))
                return false;

        return true;
    }


//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void saveUserImportStatus(int index, int totalSize, Long userImportId) {
//        UserImportAsyncStatus userImportAsyncStatus = userImportAsyncStatusRepository.findById(userImportId).get();
//        int currentStatus = index * 100 / totalSize;
//        if (currentStatus < 100) {
//            userImportAsyncStatus.setStatus(UserImportJobStatus.INPROGRESS);
//        } else {
//            userImportAsyncStatus.setStatus(UserImportJobStatus.SUCCESS);
//        }
//        userImportAsyncStatus.setDescription(currentStatus + "% records have been processed");
//        userImportAsyncStatus.setUpdatedDate(new Date());
//        userImportAsyncStatusRepository.save(userImportAsyncStatus);
//    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveUserImportStatus(int index, int totalSize, Long userImportId, List<String> existingRecords, List<String> existingRecordsMobile, List<String> correctionRequiredRecords) {
        UserImportAsyncStatus userImportAsyncStatus = userImportAsyncStatusRepository.findById(userImportId).get();
        int currentStatus = index * 100 / totalSize;
        userImportAsyncStatus.setTotalCount(Long.valueOf(totalSize));
        userImportAsyncStatus.setProcessedCount(Long.valueOf(index));
        if (currentStatus < 100) {
            userImportAsyncStatus.setStatus(UserImportJobStatus.INPROGRESS);
        } else {
            userImportAsyncStatus.setStatus(UserImportJobStatus.SUCCESS);
        }
        if ((existingRecords != null && !existingRecords.isEmpty()) || (existingRecordsMobile != null && !existingRecordsMobile.isEmpty())) {
            String existingRecord = (existingRecords != null && !existingRecords.isEmpty()) ? String.join(", ", existingRecords) : "";
            String mobileNumberRecord = (existingRecordsMobile != null && !existingRecordsMobile.isEmpty()) ? String.join(", ", existingRecordsMobile) : "";
            userImportAsyncStatus.setDescription(currentStatus + "% records have been processed\n " + existingRecord + "\n " + mobileNumberRecord);

        } else {
            userImportAsyncStatus.setDescription(currentStatus + "% records have been processed");
        }
        userImportAsyncStatus.setEmailDuplicates(Long.valueOf(existingRecords.size()));
        userImportAsyncStatus.setMobileDuplicates(Long.valueOf(existingRecordsMobile.size()));
        userImportAsyncStatus.setSavedCount(userImportAsyncStatus.getProcessedCount() - (userImportAsyncStatus.getEmailDuplicates() + userImportAsyncStatus.getMobileDuplicates()));
        userImportAsyncStatus.setUpdatedDate(new Date());
        userImportAsyncStatusRepository.save(userImportAsyncStatus);
        userImportAsyncStatus.setStatus(UserImportJobStatus.SUCCESS);
        if (currentStatus == 100) {
            Optional<User> user = userRepository.findById(userImportAsyncStatus.getUserId());
            if (user.isPresent() && userImportAsyncStatus.getSavedCount() > 0) {
                String message = userImportAsyncStatus.getSavedCount() + " new applicant profiles uploaded by " + user.get().getFirstName() + " " + user.get().getLastName() + ".";
                alertService.saveAlertInfo(user.get().getUserId(), message, user.get().getConsultancyId());
            }
        }

    }

    public Applicant login(String username, String password, Object token) {
        Applicant user = applicantRepository.findByEmail(username);
        if (user == null) {
            return null;
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        } else {
            return null;
        }
    }


    public void saveUserFiles(Long userId, Long certificateID, String type, String key) {
        if (type != null && type.equalsIgnoreCase("resume")) {
            applicantRepository.updateResumeKey(userId, key);
        } else if (type != null && type.equalsIgnoreCase("passport")) {
            applicantRepository.updatePassportKey(userId, key);
        } else if (type != null && type.equalsIgnoreCase("additional")) {
            applicantRepository.updateAdditionalDocKey(userId, key);
        } else if (type != null && type.equalsIgnoreCase("profile-image")) {
            applicantRepository.updateProfileImageKey(userId, key);
        } else if (type != null && type.equalsIgnoreCase("certificate")) {
            certificateRepository.updateCertificateKey(certificateID, key);
        }
    }

    @Transactional
    public void updateProfileAndDetails(UpdateApplicantProfileAndDetailsDTO updateApplicantProfileAndDetailsDTO, Long applicantId, UserDetailsDTO userDetailsDTO) throws Exception {
        updateProfile(updateApplicantProfileAndDetailsDTO.getApplicantProfile(), applicantId, userDetailsDTO);
        applicantDetailsService.updateApplicantDetails(updateApplicantProfileAndDetailsDTO.getApplicantDetails(), applicantId);
        runJobMatcher(applicantId);

    }

    public void runJobMatcher(long userId) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Optional<Applicant> optional = applicantRepository.findById(userId);
                jobPostService.runJobMatcherForApplicant(userId, optional.get().getConsultancyUserId(), 100, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void updateProfile(UpdateApplicantDTO updateApplicantDTO, Long applicantId, UserDetailsDTO userDetailsDTO) throws Exception {
        Optional<User> optionalUser = userRepository.findById(userDetailsDTO.getId());
        Optional<Applicant> optionalApplicant = applicantRepository.findById(applicantId);
        if (optionalUser.isPresent() && optionalApplicant.isPresent()) {

            Applicant applicant = optionalApplicant.get();
            if (!applicant.getEmail().equalsIgnoreCase(updateApplicantDTO.getEmail())) {
                if (applicantRepository.findByEmail(updateApplicantDTO.getEmail()) != null) {
                    throw new AuthenticationException("Applicant profile with the same email already exists");
                }

            }
            if (!applicant.getPhoneNumber().equalsIgnoreCase(updateApplicantDTO.getPhoneNumber())) {
                if (applicantRepository.findByPhoneNumber(updateApplicantDTO.getPhoneNumber()) != null) {
                    throw new AuthenticationException("Applicant profile with the same phone number already exists");
                }

            }

            applicant.setFirstName(updateApplicantDTO.getFirstName());
            applicant.setLastName(updateApplicantDTO.getLastName());
            applicant.setLocation(updateApplicantDTO.getLocation());
            applicant.setSkills(updateApplicantDTO.getSkills());
            applicant.setCountry(updateApplicantDTO.getCountry());
            applicant.setDob(updateApplicantDTO.getDob());
            applicant.setPhoneNumber(updateApplicantDTO.getPhoneNumber());
            applicant.setEthnicity(updateApplicantDTO.getEthnicity());
            applicant.setProfileSummary(updateApplicantDTO.getProfileSummary());
            applicant.setEmail(updateApplicantDTO.getEmail());
            applicant.setGender(updateApplicantDTO.getGender());
            applicant.setEnabled(updateApplicantDTO.isEnabled());
            applicant.setConsultancyUserId(userDetailsDTO.getId());
            //applicant.setProfileImageUrl(updateApplicantDTO.getProfileImageUrl());
            applicant.setPassword(updateApplicantDTO.getPassword());
            //applicant.setResumeUploadId(updateApplicantDTO.getResumeUploadId());
            applicant.setLanguages(updateApplicantDTO.getLanguages());
            applicant.setResumeURL(updateApplicantDTO.getResumeURL());
            applicant.setResumeVideoURL(updateApplicantDTO.getResumeVideoURL());
            //applicant.setPassportUploadId(updateApplicantDTO.getPassportUploadId());
            //applicant.setUploadAdditionalDocId(updateApplicantDTO.getUploadAdditionalDocId());
            applicant.setUploadId(updateApplicantDTO.getUploadId());
            applicant.setConsultancyId(userDetailsDTO.getConsultancyId());
            applicant.setCountryCode(updateApplicantDTO.getCountryCode());
//            applicant.setNumber(updateApplicantDTO.getNumber());
            applicant.setUpdatedDate(new Date());
            applicantRepository.save(applicant);

        }
    }

    @Transactional
    public RecentUploadApplicantDTO recentUpload(Long userId, Long consultancyId, String firstName, boolean recentUploaded, String role, Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("applicantId").descending());
        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, 2);
        Page<Applicant> applicants = null;
        Long version = userImportAsyncStatusRepository.findVersionByUserImportAsyncUserId(userId);

        if (recentUploaded && userImportAsyncStatusRepository.findVersionByUserImportAsyncUserId(userId) != null) {
            if (firstName == null || firstName.equals("")) {
                applicants = applicantRepository.findByConsultancyUserIdAndVersion(userId, version, pageable);
            } else {
                applicants = applicantRepository.findByConsultancyUserIdAndVersionAndFirstNameContaining(userId, version, firstName, pageable);
            }
        } else {
//            List<Long> userIds = new ArrayList<Long>();
//            User user = userRepository.getById(userId);
//            if (user.getDepartmentId() == null) {
//                userIds = userRepository.findUsersIdsByConsultancyIdAndEnabled(consultancyId, true, userId, null);
//            } else {
//                userIds = userRepository.findSameDepartmentUsersIdsByConsultancyIdAndEnabled(consultancyId, true, user.getDepartmentId(), null);
//            }
//            userIds.add(userId);
            if (firstName == null || firstName.equals("")) {
                applicants = applicantRepository.findByConsultancyUserId(userIds, pageable);
            } else {
                applicants = applicantRepository.findByConsultancyUserIdAndFirstNameContaining(userIds, firstName, pageable);
            }
        }
        RecentUploadApplicantDTO recentUploadApplicantDTO = role.equals("CONSULTANCY_ADMIN") ? setCurrentWorkStatusCount(consultancyId, userIds) : setCurrentWorkStatusCount(consultancyId, userId, userIds);
        recentUploadApplicantDTO.setData(getUploadedApplicantData(applicants.getContent()));
        recentUploadApplicantDTO.setJobId(userImportAsyncStatusRepository.findIdByUserImportAsyncUserId(userId));
        recentUploadApplicantDTO.setTotalCount(applicants.getTotalElements());
        recentUploadApplicantDTO.setTotalPages(applicants.getTotalPages());

        return recentUploadApplicantDTO;
    }


    public RecentUploadApplicantDTO setCurrentWorkStatusCount(Long consultancyId, Long consultancyUserId, List<Long> userIds) {

        RecentUploadApplicantDTO recentUploadApplicantDTO = new RecentUploadApplicantDTO();
        recentUploadApplicantDTO.setTotalProfile(applicantRepository.countByConsultancyId(userIds));
        recentUploadApplicantDTO.setWorking(applicantRepository.countByCurrentWorkStatus(consultancyId, "Working", consultancyUserId));
        recentUploadApplicantDTO.setNotWorking(applicantRepository.countByCurrentWorkStatus(consultancyId, "Not working", consultancyUserId));
        recentUploadApplicantDTO.setOnBench(applicantRepository.countByCurrentWorkStatus(consultancyId, "On Bench", consultancyUserId));
        recentUploadApplicantDTO.setUnderNoticePeriod(applicantRepository.countByCurrentWorkStatus(consultancyId, "Serving notice period", consultancyUserId));
        return recentUploadApplicantDTO;
    }

    public RecentUploadApplicantDTO setCurrentWorkStatusCount(Long consultancyId, List<Long> userIds) {

        RecentUploadApplicantDTO recentUploadApplicantDTO = new RecentUploadApplicantDTO();
        recentUploadApplicantDTO.setTotalProfile(applicantRepository.countByConsultancyId(userIds));
        recentUploadApplicantDTO.setWorking(applicantRepository.countByCurrentWorkStatus(consultancyId, "Working"));
        recentUploadApplicantDTO.setNotWorking(applicantRepository.countByCurrentWorkStatus(consultancyId, "Not working"));
        recentUploadApplicantDTO.setOnBench(applicantRepository.countByCurrentWorkStatus(consultancyId, "On Bench"));
        recentUploadApplicantDTO.setUnderNoticePeriod(applicantRepository.countByCurrentWorkStatus(consultancyId, "Serving notice period"));
        return recentUploadApplicantDTO;
    }


    public List<RecentUploadApplicantDataDTO> getUploadedApplicantData(List<Applicant> applicants) {
        List<RecentUploadApplicantDataDTO> recentUploadApplicantDataDTOS = new ArrayList<RecentUploadApplicantDataDTO>();
        if (applicants != null) {
            for (Applicant applicant : applicants) {
                ApplicantDetails applicantDetails = applicantDetailsRepository.findByApplicantId(applicant.getApplicantId());
                User user = userRepository.getById(applicant.getConsultancyUserId());
                RecentUploadApplicantDataDTO recentUploadApplicantDataDTO = new RecentUploadApplicantDataDTO();
                recentUploadApplicantDataDTO.setFirstName(applicant.getFirstName());
                recentUploadApplicantDataDTO.setLastName(applicant.getLastName());
                if (applicantDetails.getJobFunction() != null) {
                    recentUploadApplicantDataDTO.setJobFunction(applicantDetails.getJobFunction() != null ? applicantDetails.getJobFunction() : "");
                }
                recentUploadApplicantDataDTO.setExperience(applicantDetails.getYearsOfExperience());
                recentUploadApplicantDataDTO.setStatus(applicant.getStatus() == 1 ? ApplicantStatus.ACTIVE : ApplicantStatus.DEACTIVATED);
                List<EducationHistory> eduHistory = educationHistoryRepository.findByApplicantId(applicant.getApplicantId());
                recentUploadApplicantDataDTO.setQualification(eduHistory != null && eduHistory.size() > 0 ? eduHistory.stream().map(e -> e.getDegree()).collect(Collectors.joining("/")) : "");
                recentUploadApplicantDataDTO.setJobType(applicantDetails.getJobType());
                recentUploadApplicantDataDTO.setLocation(applicant.getLocation());
                recentUploadApplicantDataDTO.setCorrectionNeeded(!isCorrectionSolved(applicantDetails.getApplicantId()));
                recentUploadApplicantDataDTO.setProfileCompletionStatus(profileCompletionStatus(applicant.getApplicantId()));
                recentUploadApplicantDataDTO.setApplicantId(applicant.getApplicantId());
                recentUploadApplicantDataDTO.setUploadedUserBy(user.getFirstName() + " " + user.getLastName());
                recentUploadApplicantDataDTO.setUploadedUserId(user.getUserId());
                recentUploadApplicantDataDTOS.add(recentUploadApplicantDataDTO);
            }
        }
        return recentUploadApplicantDataDTOS;
    }

    @Transactional
    public void deleteApplicant(Long applicantId, Long consultancyId, Long userId) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));
        workExperienceRepository.deleteByApplicantId(applicantId);
        courseRepository.deleteByApplicantId(applicantId);
        educationHistoryRepository.deleteByApplicantId(applicantId);
        projectRepository.deleteByApplicantId(applicantId);
        internshipRepository.deleteByApplicantId(applicantId);
        referenceRepository.deleteByApplicantId(applicantId);
        publicationRepository.deleteByApplicantId(applicantId);
        socialMediaLinksRepository.deleteByApplicantId(applicantId);
        applicantDetailsRepository.deleteByApplicantId(applicantId);
        applicantJobFunctionRepository.deleteByApplicantId(applicantId);
        applicantJobSkillRepository.deleteByApplicantId(applicantId);
        applicantRepository.deleteById(applicantId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            String message = "1 applicant profiles deleted by " + user.get().getFirstName() + " " + user.get().getLastName() + ".";
            alertService.saveAlertInfo(userId, message, consultancyId);
        }
    }

    @Transactional
    public void updateApplicantStatus(Long applicantId, Long consultancyId, int status) throws WorkruitException {
        Applicant applicant = applicantRepository.findByApplicantIdAndConsultancyId(applicantId, consultancyId)
                .orElseThrow(() -> new WorkruitException(String.format("Applicant is not found with id: %s", applicantId)));
        applicant.setStatus(status);
        applicantRepository.save(applicant);
    }

    public RecentUploadApplicantDTO filterRecentUploaded(ApplicantFilterDTO applicantFilterDTO, Long consultancyId, Long userId, boolean recentUploaded, String role, Integer pageNo, Integer pageSize) {

        List<Long> userIds = consultancyJobService.getUserIds(consultancyId, userId, role, 2);

        String selectQuery = "SELECT Distinct a.applicantId, a.firstName,a.lastName,ad.jobFunction,ad.yearsOfExperience,a.status,a.location,ad.jobType";
        String countSql = "SELECT COUNT(DISTINCT a.applicantId) ";
        StringBuilder sqlBuilder = new StringBuilder(" FROM Applicant a " +
                "LEFT JOIN ApplicantDetails ad ON a.applicantId = ad.applicantId " +
                "LEFT JOIN ApplicantJobFunction ajf ON a.applicantId = ajf.applicantId " +
                "LEFT JOIN EducationHistory eh ON eh.applicantId = a.applicantId " +
                "WHERE  a.consultancyId = :consultancyId and a.consultancyUserId IN (:consultancyUserId)");

        Map<String, Object> params = new HashMap<>();
        params.put("consultancyId", consultancyId);

        if (recentUploaded) {
            Long version = userImportAsyncStatusRepository.findVersionByUserImportAsyncUserId(userId);
            if (version != null) {
                sqlBuilder.append(" AND a.version = :version ");
                params.put("version", version);
            }
        }

        if (!recentUploaded && applicantFilterDTO.getUserIds() != null && !applicantFilterDTO.getUserIds().isEmpty()) {
            params.put("consultancyUserId", applicantFilterDTO.getUserIds());
        } else {
            params.put("consultancyUserId", userId);
        }

        if (applicantFilterDTO.getJobFunction() != null && !applicantFilterDTO.getJobFunction().isEmpty()) {
            sqlBuilder.append(" AND ajf.jobFunctionId IN (:jobFunction)");
            params.put("jobFunction", applicantFilterDTO.getJobFunction());
        }

        if (applicantFilterDTO.getLocation() != null && !applicantFilterDTO.getLocation().equals("")) {
            sqlBuilder.append(" AND a.location = :location");
            params.put("location", applicantFilterDTO.getLocation());
        }

        if (applicantFilterDTO.getJobTypes() != null && !applicantFilterDTO.getJobTypes().isEmpty()) {
            sqlBuilder.append(" AND ad.jobType IN (:jobTypes)");
            params.put("jobTypes", applicantFilterDTO.getJobTypes());
        }

        if (applicantFilterDTO.getCurrentWorkStatus() != null && !applicantFilterDTO.getCurrentWorkStatus().isEmpty()) {
            sqlBuilder.append(" AND ad.currentWorkStatus IN (:currentWorkStatus)");
            params.put("currentWorkStatus", applicantFilterDTO.getCurrentWorkStatus());
        }

        if (applicantFilterDTO.getPreferredWorkModes() != null && !applicantFilterDTO.getPreferredWorkModes().isEmpty()) {
            sqlBuilder.append(" AND ad.preferredWorkMode IN (:preferredWorkModes)");
            params.put("preferredWorkModes", applicantFilterDTO.getPreferredWorkModes());
        }

        if (applicantFilterDTO.getCareerLevel() != null && !applicantFilterDTO.getCareerLevel().isEmpty()) {
            sqlBuilder.append(" AND ad.careerLevel IN (:careerLevel)");
            params.put("careerLevel", applicantFilterDTO.getCareerLevel());
        }

        if (applicantFilterDTO.getYearsOfExp() != null) {
            sqlBuilder.append(" AND ad.yearsOfExperience >= :yearsOfExp");
            params.put("yearsOfExp", applicantFilterDTO.getYearsOfExp());
        }

        if (applicantFilterDTO.getCitizenship() != null && !applicantFilterDTO.getCitizenship().equals("")) {
            sqlBuilder.append(" AND ad.citizenship = :citizenship");
            params.put("citizenship", applicantFilterDTO.getCitizenship());
        }

        if (applicantFilterDTO.getEduQualification() != null && !applicantFilterDTO.getEduQualification().isEmpty()) {
            sqlBuilder.append(" AND eh.degree IN (:eduQualification)");
            params.put("eduQualification", applicantFilterDTO.getEduQualification());
        }
        sqlBuilder.append(" order by a.applicantId ");
        Query countQuery = entityManager.createQuery(countSql + sqlBuilder);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        long totalCount = (long) countQuery.getSingleResult();

        Query query = entityManager.createQuery(selectQuery + sqlBuilder).setFirstResult(pageNo * pageSize).setMaxResults(pageSize);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        List<Object[]> results = query.getResultList();
        List<RecentUploadApplicantDataDTO> recentUploadApplicantDataDTOS = new ArrayList<RecentUploadApplicantDataDTO>();
        for (Object[] result : results) {
            RecentUploadApplicantDataDTO recentUploadApplicantDataDTO = new RecentUploadApplicantDataDTO();
            recentUploadApplicantDataDTO.setApplicantId((Long) result[0]);
            recentUploadApplicantDataDTO.setFirstName((String) result[1]);
            recentUploadApplicantDataDTO.setLastName((String) result[2]);
            recentUploadApplicantDataDTO.setJobFunction((String) result[3]);
            recentUploadApplicantDataDTO.setExperience((String) result[4]);
            recentUploadApplicantDataDTO.setStatus(Integer.parseInt(result[5].toString()) == 1 ? ApplicantStatus.ACTIVE : ApplicantStatus.DEACTIVATED);
            recentUploadApplicantDataDTO.setLocation((String) result[6]);
            recentUploadApplicantDataDTO.setJobType((String) result[7]);
            recentUploadApplicantDataDTO.setProfileCompletionStatus(profileCompletionStatus((Long) result[0]));
            List<EducationHistory> eduHistory = educationHistoryRepository.findByApplicantId((Long) result[0]);
            recentUploadApplicantDataDTO.setQualification(eduHistory != null && eduHistory.size() > 0 ? eduHistory.stream().map(e -> e.getDegree()).collect(Collectors.joining("/")) : "");
            recentUploadApplicantDataDTOS.add(recentUploadApplicantDataDTO);
        }

        RecentUploadApplicantDTO recentUploadApplicantDTO = setCurrentWorkStatusCount(consultancyId, userId, userIds);
        recentUploadApplicantDTO.setData(recentUploadApplicantDataDTOS);
        recentUploadApplicantDTO.setTotalCount(totalCount);
        recentUploadApplicantDTO.setTotalPages((long) Math.ceil((double) totalCount / pageSize));
        return recentUploadApplicantDTO;

    }

    @Transactional
    public Long createProfileAndDetails(UpdateApplicantProfileAndDetailsDTO updateApplicantProfileAndDetailsDTO, UserDetailsDTO userDetailsDTO) throws Exception {

        if (applicantRepository.findByEmail(updateApplicantProfileAndDetailsDTO.getApplicantProfile().getEmail()) != null) {
            throw new AuthenticationException("Applicant profile with the same email already exists.");
        }
        if (updateApplicantProfileAndDetailsDTO.getApplicantProfile().getNumber() != null) {
            if (applicantRepository.findByPhoneNumber(updateApplicantProfileAndDetailsDTO.getApplicantProfile().getNumber()) != null) {
                throw new AuthenticationException("Applicant profile with the same phone number already exists.");
            }
        }
        if (updateApplicantProfileAndDetailsDTO.getApplicantProfile().getPhoneNumber() != null) {
            if (applicantRepository.findByPhoneNumber(updateApplicantProfileAndDetailsDTO.getApplicantProfile().getPhoneNumber()) != null) {
                throw new AuthenticationException("Applicant profile with the same phone number already exists.");
            }
        }
        Long applicantId = createProfile(updateApplicantProfileAndDetailsDTO.getApplicantProfile(), userDetailsDTO);


        if (applicantId != null) {
            applicantDetailsService.updateApplicantDetails(updateApplicantProfileAndDetailsDTO.getApplicantDetails(), applicantId);
//            new Thread(() -> {
//                try {
//                    Thread.sleep(1000);
//                    jobPostService.runJobMatcherForApplicant(applicantId, 100, false);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
            runJobMatcher(applicantId);
        }
        return applicantId;
    }

    public Long createProfile(UpdateApplicantDTO updateApplicantDTO, UserDetailsDTO userDetailsDTO) {
        Optional<User> optionalUser = userRepository.findById(userDetailsDTO.getId());
        if (optionalUser.isPresent()) {
            Applicant applicant = new Applicant();
            applicant.setFirstName(updateApplicantDTO.getFirstName());
            applicant.setLastName(updateApplicantDTO.getLastName());
            applicant.setLocation(updateApplicantDTO.getLocation());
            applicant.setSkills(updateApplicantDTO.getSkills());
            applicant.setCountry(updateApplicantDTO.getCountry());
            applicant.setDob(updateApplicantDTO.getDob());
            applicant.setPhoneNumber(updateApplicantDTO.getPhoneNumber());
            applicant.setEthnicity(updateApplicantDTO.getEthnicity());
            applicant.setProfileSummary(updateApplicantDTO.getProfileSummary());
            applicant.setEmail(updateApplicantDTO.getEmail());
            applicant.setGender(updateApplicantDTO.getGender());
            applicant.setEnabled(updateApplicantDTO.isEnabled());
            applicant.setProfileImageUrl(updateApplicantDTO.getProfileImageUrl());
            applicant.setPassword(updateApplicantDTO.getPassword());
            applicant.setResumeUploadId(updateApplicantDTO.getResumeUploadId());
            applicant.setLanguages(updateApplicantDTO.getLanguages());
            applicant.setResumeURL(updateApplicantDTO.getResumeURL());
            applicant.setResumeVideoURL(updateApplicantDTO.getResumeVideoURL());
            applicant.setPassportUploadId(updateApplicantDTO.getPassportUploadId());
            applicant.setUploadAdditionalDocId(updateApplicantDTO.getUploadAdditionalDocId());
            applicant.setUploadId(updateApplicantDTO.getUploadId());
            applicant.setConsultancyId(userDetailsDTO.getConsultancyId());
            applicant.setConsultancyUserId(userDetailsDTO.getId());
            applicant.setCountryCode(updateApplicantDTO.getCountryCode());
//            applicant.setNumber(updateApplicantDTO.getNumber());
            applicant.setCreatedDate(new Date());
            applicant.setStatus(1);
            applicant.setUpdatedDate(new Date());
            Applicant savedApplicant = applicantRepository.save(applicant);
            return savedApplicant.getApplicantId();
        }
        return null;
    }

    public boolean isDateFormatSame(String dateString, SimpleDateFormat simpleDateFormat) throws ParseException {
        Date parsedDate = simpleDateFormat.parse(dateString.trim());
        String formattedDate = simpleDateFormat.format(parsedDate);
        return dateString.trim().equals(formattedDate);
    }

    public boolean isCorrectionSolved(long applicantId) {
        Optional<Applicant> optionalApplicant = applicantRepository.findById(applicantId);
        if (optionalApplicant.isPresent()) {
            Applicant applicant = optionalApplicant.get();
            if (applicant.getFirstName() == null && StringUtils.isBlank(applicant.getFirstName())) {
                return false;
            }
            if (applicant.getLastName() == null && StringUtils.isBlank(applicant.getLastName())) {
                return false;
            }
            if (applicant.getProfileSummary() == null && StringUtils.isBlank(applicant.getProfileSummary())) {
                return false;
            }
            if (applicant.getDob() == null) {
                return false;
            }
            if (applicant.getDob() != null && StringUtils.isBlank(applicant.getDob().toString())) {
                return false;
            }
            if (applicant.getEmail() == null && StringUtils.isBlank(applicant.getEmail())) {
                return false;
            }
            if (applicant.getPhoneNumber() == null && StringUtils.isBlank(applicant.getPhoneNumber())) {
                return false;
            }

//            if (applicant.getCountryCode() == null && StringUtils.isBlank(applicant.getCountryCode())) {
//                return false;
//            }
//            if (applicant.getNumber() == null && StringUtils.isBlank(applicant.getNumber())) {
//                return false;
//            }
            if (applicant.getLocation() == null && StringUtils.isBlank(applicant.getLocation())) {
                return false;
            }
//            if (applicant.getGender() == null && StringUtils.isBlank(applicant.getGender())) {
//                return false;
//            }
//            if (applicant.getEthnicity() == null && StringUtils.isBlank(applicant.getEthnicity())) {
//                return false;
//            }
//            if (applicant.getLanguages() == null && StringUtils.isBlank(applicant.getLanguages())) {
//                return false;
//            }
//            if (applicant.getResumeURL() == null && StringUtils.isBlank(applicant.getResumeURL())) {
//                return false;
//            }
//            if (applicant.getResumeVideoURL() == null && StringUtils.isBlank(applicant.getResumeVideoURL())) {
//                return false;
//            }
//currentSalary
            ApplicantDetails ad = applicantDetailsRepository.findByApplicantId(applicantId);
            if (ad.getYearsOfExperience() == null && StringUtils.isBlank(ad.getYearsOfExperience())) {
                return false;
            }
            if (ad.getCareerLevel() == null && StringUtils.isBlank(ad.getCareerLevel())) {
                return false;
            }
            if (ad.getCurrentSalary() == null && StringUtils.isBlank(ad.getCurrentSalary())) {
                return false;
            }
            if (ad.getExpectedSalary() == null && StringUtils.isBlank(ad.getExpectedSalary())) {
                return false;
            }
            if (ad.getSalaryRate() == null && StringUtils.isBlank(ad.getSalaryRate())) {
                return false;
            }
            if (ad.getExpectedSalaryRate() == null && StringUtils.isBlank(ad.getExpectedSalaryRate())) {
                return false;
            }
            if (ad.getJobFunction() == null && StringUtils.isBlank(ad.getJobFunction())) {
                return false;
            } else {
                String[] jobFunctions = ad.getJobFunction().split(",");
                if (jobFunctions != null && jobFunctions.length > 0) {
                    for (String jobFunction : jobFunctions) {
                        JobFunction jobFunctionModel = jobFunctionRepository
                                .findByJobFunctionNameIgnoreCase(jobFunction.trim());
                        if (jobFunctionModel == null) {
                            return false;
                        }
                    }
                }
            }
            if (ad.getSecondaryJobFunction() != null && !StringUtils.isBlank(ad.getSecondaryJobFunction())) {

                String[] jobFunctions = ad.getSecondaryJobFunction().split(",");
                if (jobFunctions != null && jobFunctions.length > 0) {
                    for (String jobFunction : jobFunctions) {
                        JobFunction jobFunctionModel = jobFunctionRepository
                                .findByJobFunctionNameIgnoreCase(jobFunction.trim());
                        if (jobFunctionModel == null) {
                            return false;
                        }
                    }
                    if (jobFunctions.length > 2) {
                        return false;
                    }
                }
            }
            if (ad.getSkills() == null && StringUtils.isBlank(ad.getSkills())) {
                return false;
            } else {
                String[] jobSkills = ad.getSkills().split(",");
                int count = 0;
                if (jobSkills != null && jobSkills.length > 0) {
                    for (String jobSkill : jobSkills) {
                        JobSkills jobSkillModel = jobSkillsRepository.findBySkillNameIgnoreCase(jobSkill.trim());
                        if (jobSkillModel == null) {
                            return false;
                        } else {
                            count++;
                        }
                    }
                }
                if (count < 5 || count > 20) {
                    return false;
                }
            }

            if (ad.getPreferredWorkMode() == null && StringUtils.isBlank(ad.getPreferredWorkMode())) {
                return false;
            }
            if (ad.getJobType() == null && StringUtils.isBlank(ad.getJobType())) {
                return false;
            }
            if (ad.getNoticePeriod() == null && StringUtils.isBlank(ad.getNoticePeriod())) {
                return false;
            }
            if (ad.getCurrentWorkStatus() == null && StringUtils.isBlank(ad.getCurrentWorkStatus())) {
                return false;
            }
            if (ad.getCitizenship() == null && StringUtils.isBlank(ad.getCitizenship())) {
                return false;
            }
            if (ad.isWillingToRelocate() && ad.getRelocation() == null && StringUtils.isBlank(ad.getRelocation())) {
                return false;
            }
            List<WorkExperience> workExperiences = workExperienceRepository.findByApplicantId(applicantId);
            if (workExperiences != null && !workExperiences.isEmpty()) {
                for (WorkExperience we : workExperiences) {
                    if (we.getCompanyName() == null && StringUtils.isBlank(we.getCompanyName())) {
                        return false;
                    }
                    if (we.getJobTitle() == null && StringUtils.isBlank(we.getJobTitle())) {
                        return false;
                    }
                    if (we.getLocation() == null && StringUtils.isBlank(we.getLocation())) {
                        return false;
                    }
                    if (we.getStartDate() == null) {
                        return false;
                    }
                    if (!we.isCurrentlyWorkingHere() && we.getEndDate() == null) {
                        return false;
                    }
//                    if (we.getDescription() == null && StringUtils.isBlank(we.getDescription())) {
//                        return false;
//                    }
                }
            }

            List<Internship> internships = internshipRepository.findByApplicantId(applicantId);
            if (internships != null && !internships.isEmpty()) {
                for (Internship in : internships) {
                    if (in.getCompanyName() == null && StringUtils.isBlank(in.getCompanyName())) {
                        return false;
                    }

//                    if (in.getEndDate() == null) {
//                        return false;
//                    }
                    if (in.getJobTitle() == null && StringUtils.isBlank(in.getJobTitle())) {
                        return false;
                    }
//                    if (in.getLocation() == null && StringUtils.isBlank(in.getLocation())) {
//                        return false;
//                    }
//                    if (in.getStartDate() == null) {
//                        return false;
//                    }
//                    if (in.getDescription() == null && StringUtils.isBlank(in.getDescription())) {
//                        return false;
//                    }
                }
            }

            List<EducationHistory> educationHistories = educationHistoryRepository.findByApplicantId(applicantId);
            if (educationHistories != null && !educationHistories.isEmpty()) {
                for (EducationHistory eh : educationHistories) {
                    if (eh.getCollegeName() == null && StringUtils.isBlank(eh.getCollegeName())) {
                        return false;
                    }
                    if (eh.getDegree() == null && StringUtils.isBlank(eh.getDegree())) {
                        return false;
                    }
                    if (eh.getField() == null && StringUtils.isBlank(eh.getField())) {
                        return false;
                    }
                    if (eh.getLocation() == null && StringUtils.isBlank(eh.getLocation())) {
                        return false;
                    }
                    if (eh.getStartDate() == null) {
                        return false;
                    }
                    if (!eh.isCurrentlyStudying() && eh.getEndDate() == null) {
                        return false;
                    }
//                    if (eh.getDescription() == null && StringUtils.isBlank(eh.getDescription())) {
//                        return false;
//                    }
                }
            }

            List<Project> projects = projectRepository.findByApplicantId(applicantId);
            if (projects != null && !projects.isEmpty()) {
                for (Project pr : projects) {
//                    if (pr.getDescription() == null && StringUtils.isBlank(pr.getDescription())) {
//                        return false;
//                    }
                    if (!pr.isProjectOngoing() && pr.getEndDate() == null) {
                        return false;
                    }
                    if (pr.getInstitutionName() == null && StringUtils.isBlank(pr.getInstitutionName())) {
                        return false;
                    }
                    if (pr.getLocation() == null && StringUtils.isBlank(pr.getLocation())) {
                        return false;
                    }
                    if (pr.getRole() == null && StringUtils.isBlank(pr.getRole())) {
                        return false;
                    }
                    if (pr.getStartDate() == null) {
                        return false;
                    }
                    if (pr.getTitle() == null && StringUtils.isBlank(pr.getTitle())) {
                        return false;
                    }
                }
            }

            List<Course> courses = courseRepository.findByApplicantId(applicantId);
            if (courses != null && !courses.isEmpty()) {
                for (Course cr : courses) {
                    if (cr.getCourseDuration() == null && StringUtils.isBlank(cr.getCourseDuration())) {
                        return false;
                    }
                    if (cr.getCourseTitle() == null && StringUtils.isBlank(cr.getCourseTitle())) {
                        return false;
                    }
                    if (cr.getInstitutionName() == null && StringUtils.isBlank(cr.getInstitutionName())) {
                        return false;
                    }
                }
            }

            List<Reference> references = referenceRepository.findByApplicantId(applicantId);
            if (references != null && !references.isEmpty()) {
                for (Reference rf : references) {
                    if (rf.getEmail() == null && StringUtils.isBlank(rf.getEmail())) {
                        return false;
                    }
//                    if (rf.getEmployer() == null && StringUtils.isBlank(rf.getEmployer())) {
//                        return false;
//                    }
                    if (rf.getName() == null && StringUtils.isBlank(rf.getName())) {
                        return false;
                    }
                    if (rf.getPhoneNumber() == null && StringUtils.isBlank(rf.getPhoneNumber())) {
                        return false;
                    }
                    if (rf.getReferenceType() == null && StringUtils.isBlank(rf.getReferenceType())) {
                        return false;
                    }
//                    if (rf.getTitle() == null && StringUtils.isBlank(rf.getTitle())) {
//                        return false;
//                    }
                }
            }

//            List<SocialMediaLinks> socialMediaLinks = socialMediaLinksRepository.findByApplicantId(applicantId);
//            if (socialMediaLinks != null && !socialMediaLinks.isEmpty()) {
//                for (SocialMediaLinks sm : socialMediaLinks) {
//                    if (sm.getGithubLink() == null && StringUtils.isBlank(sm.getGithubLink())) {
//                        return false;
//                    }
//                    if (sm.getLinkedinLink() == null && StringUtils.isBlank(sm.getLinkedinLink())) {
//                        return false;
//                    }
//                    if (sm.getTwitterLink() == null && StringUtils.isBlank(sm.getTwitterLink())) {
//                        return false;
//                    }
//                    if (sm.getWebsiteLink() == null && StringUtils.isBlank(sm.getWebsiteLink())) {
//                        return false;
//                    }
//                    if (sm.getBlogLink() == null && StringUtils.isBlank(sm.getBlogLink())) {
//                        return false;
//                    }
//                    if (sm.getBehanceLink() == null && StringUtils.isBlank(sm.getBehanceLink())) {
//                        return false;
//                    }
//                }
//            }

//            List<Publication> publications = publicationRepository.findByApplicantId(applicantId);
//            if (publications != null && !publications.isEmpty()) {
//                for (Publication pb : publications) {
//                    if (pb.getTitle() == null && StringUtils.isBlank(pb.getTitle())) {
//                        return false;
//                    }
//                    if (pb.getDescription() == null && StringUtils.isBlank(pb.getDescription())) {
//                        return false;
//                    }
//                }
//            }

            List<Certification> certifications = certificateRepository.findByApplicantId(applicantId);
            if (certifications != null && !certifications.isEmpty()) {
                for (Certification cr : certifications) {
                    if (cr.getTitle() == null && StringUtils.isBlank(cr.getTitle())) {
                        return false;
                    }
//                    if (cr.getDescription() == null && StringUtils.isBlank(cr.getDescription())) {
//                        return false;
//                    }
                    if (cr.getValidFrom() == null) {
                        return false;
                    }
                    if (!cr.isDoesNotExpire() && cr.getValidTo() == null) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
