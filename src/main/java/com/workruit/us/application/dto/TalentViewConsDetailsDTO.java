/**
 *
 */
package com.workruit.us.application.dto;

import com.workruit.us.application.models.CompanyClient;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Mahesh
 */
@Data
public class TalentViewConsDetailsDTO {
    private long consultancyId;
    private String consultancyName;
    private String profilePicUrl;
    private boolean isIntrested;
    private String website;
    private String jobFunction;
    private Date foundedIn;
    private String industryType;
    private String location;
    private String size;
    private String about;
    private Set<CompanyClient> clients;
    private String overallTalent;
    private String fbLink;
    private String twLink;
    private String liLink;
    private long intrestedCount = 0;
    private long totalCount = 0;
    private long totalPages = 0;

    // private List<String> domainSpec;
    private String domainSpec;

    private List<AppledProfilesDTO> appliedProfiles;

}
