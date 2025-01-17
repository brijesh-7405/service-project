package com.workruit.us.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.workruit.us.application.repositories.BenefitsRepository;

/**
 * Generated by Spring Data Generator on 25/11/2022
 */
@Component
public class BenefitsService {

    private final BenefitsRepository benefitsRepository;

    @Autowired
    public BenefitsService(BenefitsRepository benefitsRepository) {
        this.benefitsRepository = benefitsRepository;
    }

}
