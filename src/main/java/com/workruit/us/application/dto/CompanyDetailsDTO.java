/**
 *
 */
package com.workruit.us.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.workruit.us.application.models.CompanyClient;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author Santosh Bhima
 */
@Data
public class CompanyDetailsDTO {
    private String profileImageUrl;
    private String profileImageData;
    //    @NotNull(message = "About field cannot be empty.")
//    @Size(min = 150, message = "About field should contain minimum of 150 characters.")
    private String about;
    private String location;
    private String foundedDate;
    private JsonNode founderName;
    private String industryType;
    private Long numberOfEmployees;
    //private String domains;

    private Long numberOfHiredApplicants;
    private String companyName;
    private String website;
    private String facebookLink;
    private String twitterLink;
    private String linkedinLink;
    private String headquarters;
    private JsonNode productAndServices;
    private String awards;
    private String recognisation;
    private List<String> domains;
    private Set<CompanyClient> clients;
    private String overallTalentPool;
}
