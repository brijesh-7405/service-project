/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.CompanyClient;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/**
 * @author Santosh Bhima
 */
@Data
public class ConsultancyDetailsDTO {
    private String name;
    private String profileImageUrl;
    private String profileImageData;
    @NotNull(message = "About field cannot be empty.")
    @Size(min = 150, message = "About field should contain minimum of 150 characters.")
    private String about;
    @NotNull
    private String location;
    @NotNull
    private String foundedDate;
    @NotNull
    private String industryType;
    @NotNull
    private String numberOfEmployees;
    @NotNull
    private List<String> domains;

    @NotNull
    private Set<CompanyClient> clients;
    private Long numberOfHiredApplicants;
    private String website;
    private String facebookLink;
    private String twitterLink;
    private String linkedinLink;
    private String overallTalentPool;
}
