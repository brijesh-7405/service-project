/**
 *
 */
package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;

/**
 * @author Santosh Bhima
 *
 */
@Data
@Entity
@Table(name = "alert")
public class Alert extends BaseModel {
    @Id
    @Column(name = "alert_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;
    @Column(name = "message")
    private String message;
    @Column(name = "role")
    private String role;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "consultancy_id")
    private Long consultancyId;

}
