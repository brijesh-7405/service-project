/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.SocialMediaLinks;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Santosh Bhima
 */
@Data
public class ApplicantDTO {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String location;
    private String skills;
    private String country;
    private String countryCode;
    private Date dob;
    private String telephoneNumber;
    private String ethnicity;
    private String jobMatchStatus;
    private String profileSummary;
    private String consultantName;
    private String uploadedBy;

    @NotNull
    private String email;
    @NotNull
    private String gender;

    private List<WorkExperienceDTO> workExperiences = new ArrayList<>();

    private List<CourseDTO> courses = new ArrayList<>();

    private List<EducationHistoryDTO> educationHistorys = new ArrayList<>();

    private List<ProjectDTO> projects = new ArrayList<>();

    private List<InternshipDTO> internships = new ArrayList<>();

    private List<ReferenceDTO> references = new ArrayList<>();

    private List<PublicationDTO> publications = new ArrayList<>();

    private List<CertificationDTO> certifications = new ArrayList<>();

    private List<SocialMediaLinks> socialMediaLinks = new ArrayList<>();

    private ApplicantDetailsDTO details;

    private float profileCompletionPercentage;
    private String resumeUploadId;
    private String profileImageUrl;
    private String profileImageData;
    private boolean correctionNeeded;
    private String resumeURL;
    private String resumeVideoURL;
    private String passportUploadId;
    private String uploadAdditionalDocId;
    private Long version;
    private Long consultantId;
    private List<JobQuestionAnswerDTO> jobQuestionAnswers;
}
