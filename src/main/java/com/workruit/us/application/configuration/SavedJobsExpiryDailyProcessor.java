package com.workruit.us.application.configuration;

import com.workruit.us.application.service.SaveProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class SavedJobsExpiryDailyProcessor {
    @Autowired
    SaveProfileService saveProfileService;

    @Scheduled(cron = "0 30 5 * * *")
    //@Scheduled(fixedRate = 60000)
    public void sendDailyReportMail() {
        try {
            saveProfileService.updateSavedJobs();
        } catch (Exception e) {
        }
    }

}

