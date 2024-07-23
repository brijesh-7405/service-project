package com.workruit.us.application.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.workruit.us.application.dto.NotificationDTO;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    private String sendNotification(NotificationDTO note, String topic) throws FirebaseMessagingException {

        Notification notification = Notification.builder().setTitle(note.getSubject()).setBody(note.getContent())
                .setImage(note.getImage()).build();

        Message message = Message.builder().setTopic(topic).setNotification(notification).putAllData(note.getData())
                .build();

        // return firebaseMessaging.send(message);
        return "";
    }

    public void prepareNotifObject(String title, String msg, String token) throws FirebaseMessagingException {
        NotificationDTO message = new NotificationDTO();
        message.setContent(msg);
        message.setSubject(title);

        sendNotificationToDevice(message, token);
    }

    private String sendNotificationToDevice(NotificationDTO note, String token) throws FirebaseMessagingException {

        Notification notification = Notification.builder().setTitle(note.getSubject()).setBody(note.getContent())
                .setImage(note.getImage()).build();

        Message message = Message.builder().setToken(token).setNotification(notification).putAllData(note.getData())
                .build();

         return firebaseMessaging.send(message);
//        return "";
    }

}
