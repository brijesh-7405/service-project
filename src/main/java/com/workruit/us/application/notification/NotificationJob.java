package com.workruit.us.application.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.workruit.us.application.services.FirebaseMessagingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class NotificationJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String title = context.getMergedJobDataMap().getString("title");
        String msg = context.getMergedJobDataMap().getString("msg");
        String token = context.getMergedJobDataMap().getString("token");
        Long userid = Long.valueOf(context.getMergedJobDataMap().getString("userId"));
        Long consultancyId = Long.valueOf(context.getMergedJobDataMap().getString("consultancyId"));
        try {
            FirebaseMessagingService firebaseMessagingService = (FirebaseMessagingService) context.getMergedJobDataMap().get("firebaseMessagingService"); // extract the dependency
            NotificationService notificationService = (NotificationService) context.getMergedJobDataMap().get("notificationService"); // extract the dependency
            firebaseMessagingService.prepareNotifObject(title, msg, token);
            notificationService.saveNotification(userid, consultancyId, title, msg);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}