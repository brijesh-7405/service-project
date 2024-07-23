package com.workruit.us.application.notification;

import com.workruit.us.application.dto.ConsultancyJobStatusResultSet;
import com.workruit.us.application.models.Notification;
import com.workruit.us.application.models.User;
import com.workruit.us.application.repositories.ConsultancyJobStatusRepository;
import com.workruit.us.application.repositories.NotificationRepository;
import com.workruit.us.application.repositories.UserRepository;
import com.workruit.us.application.services.FirebaseMessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private ConsultancyJobStatusRepository consultancyJobStatusRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public void notifyConsultancyMembersToApplyForSavedJobs() {
        try {
            List<ConsultancyJobStatusResultSet> list = consultancyJobStatusRepository.findAllBySavedStatus();
            for (ConsultancyJobStatusResultSet data : list) {
                try {
                    User user = userRepository.findById(data.getConsultancyUserId()).get();
                    if (user.getNotificationToken() != null && !user.getNotificationToken().equals("")) {
                        String notificationMessage = "You have " + data.getDaysLeft() + " days to to apply for " + data.getJobTitle() + " job.";
                        firebaseMessagingService.prepareNotifObject("Action required", notificationMessage, user.getNotificationToken());
                        saveNotification(user.getUserId(), user.getConsultancyId(), "Action required", notificationMessage);
                    }
                } catch (Exception e) {
                    log.error("An error occurred while sending notification for saved jobs : {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while sending notification for saved jobs : {}", e.getMessage());
        }
    }

    public void notifyConsultancyMembersToUploadProfiles() {
        try {
            List<User> list = userRepository.findUseNotUploadedAnyApplicant();
            for (User data : list) {
                try {
                    if (data.getNotificationToken() != null && !data.getNotificationToken().equals("")) {
                        String notificationMessage = "Upload profiles and help applicants find the right job.";
                        firebaseMessagingService.prepareNotifObject("Find the right job", notificationMessage, data.getNotificationToken());
                        saveNotification(data.getUserId(), data.getConsultancyId(), "Find the right job", notificationMessage);
                    }
                } catch (Exception e) {
                    log.error("An error occurred while sending notification for find the right job : {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while sending notification for find the right job : {}", e.getMessage());
        }
    }

    public void jobRecommendationNotificationToConsultancyMembers() {

    }


    public void saveNotification(Long userId, Long consultancyId, String title, String notificationMessage) {
        Notification notification = new Notification();
        notification.setConsultancyId(consultancyId);
        notification.setMessage(notificationMessage);
        notification.setTitle(title);
        notification.setUserId(userId);
        notification.setCreatedDate(new Date());
        notification.setUpdatedDate(new Date());
        notificationRepository.save(notification);
    }
}
