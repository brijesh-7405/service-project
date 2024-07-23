package com.workruit.us.application.notification;

import com.workruit.us.application.services.FirebaseMessagingService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@Bean
public class NotificationScheduler {
    private @Autowired NotificationService notificationService;
    private @Autowired FirebaseMessagingService firebaseMessagingService;

    public void scheduleNotification(String title, String msg, String token, Date notifyerTime, Long userid, Long consultancyId) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        //Define the jobDataMap for passing the dependency
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("notificationService", notificationService); // pass the dependency
        jobDataMap.put("firebaseMessagingService", firebaseMessagingService); // pass the dependency

        // Define the job and the trigger
        JobDetail job = JobBuilder.newJob(NotificationJob.class)
                .withIdentity(UUID.randomUUID().toString(), "NotificationGroup")
                .usingJobData("title", title) // pass arguments as job data
                .usingJobData("msg", msg)
                .usingJobData("token", token)
                .usingJobData("userId", String.valueOf(userid))
                .usingJobData("consultancyId", String.valueOf(consultancyId))
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(UUID.randomUUID().toString(), "NotificationGroup")
                .startAt(notifyerTime) // use notifyerTime argument to set trigger start time
                .build();

        // Schedule the job with the trigger
        scheduler.scheduleJob(job, trigger);
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // Run every 24 hours
    public void notifyConsultancyMembersToApplyForSavedJobs() {
        notificationService.notifyConsultancyMembersToApplyForSavedJobs();
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // Run every 24 hours
    public void notifyConsultancyMembersToUploadProfiles() {
        notificationService.notifyConsultancyMembersToUploadProfiles();
    }

    @Scheduled(fixedDelay = 12 * 60 * 60 * 1000) // run the job every 12 hours
    public void jobRecommendationNotificationToConsultancyMembers() {
        notificationService.jobRecommendationNotificationToConsultancyMembers();
    }
}
