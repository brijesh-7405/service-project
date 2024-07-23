/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author Santosh Bhima
 */

@Data
@Entity
@Table(name = "company")
public class Company extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "name")
    private String name;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "website")
    private String website;

    @Column(name = "about")
    private String about;

    @Column(name = "location")
    private String location;

    @Column(name = "founded_date")
    private Date foundedDate;

    @Column(name = "industry_Types")
    private String industryTypes;

    @Column(name = "facebook_link")
    private String facebookLink;

    @Column(name = "twitter_link")
    private String twitterLink;

    @Column(name = "linkedin_link")
    private String linkedinLink;

    @Column(name = "products_services")
    private String productAndServices;

    @Column(name = "headquarters")
    private String headquarters;

    @Column(name = "founder_name")
    private String founderName;

    @Column(name = "company_size")
    private Long companySize;

    @Column(name = "awards")
    private String awards;

    @Column(name = "recognisation")
    private String recognisation;

    @Column(name = "domains_info")
    private String domainsInfo;

    private String domains;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Set<CompanyClient> clients;

    @Column(name = "overall_talent_pool")
    private String overallTalentPool;
}
