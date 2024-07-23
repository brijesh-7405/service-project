package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "notification")
public class Notification extends BaseModel{
    @Id
    @Column(name = "notification_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    @Column(name = "message")
    private String message;
    @Column(name = "title")
    private String title;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "consultancy_id")
    private Long consultancyId;
}
