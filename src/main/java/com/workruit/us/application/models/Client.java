package com.workruit.us.application.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Sohil Dhamecha
 *
 */
@Data
@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long clientId;

    @NotNull
    @Column(name = "client_name")
    private String clientName;

    @Column(name = "link")
    private String link;
}
